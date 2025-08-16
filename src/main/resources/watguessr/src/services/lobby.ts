import SockJS from "sockjs-client";
import { Client, type Frame } from "@stomp/stompjs";

export interface User {
  id: string;
  username: string;
}

export interface LobbyUpdate {
  users: User[];
}

export interface GameStartInfo {
  players: User[];
}

let stompClient: Client | null = null;
let onLobbyUpdateCallback: ((update: LobbyUpdate) => void) | null = null;
let onGameStartCallback: ((info: GameStartInfo) => void) | null = null;
let currentLobbyId: string | null = null;

/**
 * Connect to the lobby
 */
export function connectLobby(
  onLobbyUpdate: (update: LobbyUpdate) => void,
  onGameStart: (info: GameStartInfo) => void
): void {
  onLobbyUpdateCallback = onLobbyUpdate;
  onGameStartCallback = onGameStart;

  const socket = new SockJS("http://localhost:5173/ws-game"); // proxied to backend
  stompClient = new Client({
    webSocketFactory: () => socket as any, // SockJS factory
    debug: (msg) => console.log(msg),
    reconnectDelay: 5000,
    onConnect: (frame: Frame) => {
      console.log("Connected to lobby:", frame);
    },
    onStompError: (frame) => {
      console.error("STOMP error:", frame);
    },
  });

  stompClient.activate();
}

/**
 * Join a specific lobby
 */
export function joinLobby(user: User, lobbyId: string): void {
  if (!stompClient) return;

  currentLobbyId = lobbyId;

  // Subscribe to lobby updates for this specific lobby
  stompClient.subscribe(`/topic/lobby/${lobbyId}`, (message) => {
    const update: LobbyUpdate = JSON.parse(message.body);
    onLobbyUpdateCallback?.(update);
  });

  // Subscribe to game start events for this specific lobby
  stompClient.subscribe(`/topic/lobby/${lobbyId}/start`, (message) => {
    const info: GameStartInfo = JSON.parse(message.body);
    onGameStartCallback?.(info);
  });

  // Send join message
  stompClient.publish({
    destination: "/app/lobby/join",
    body: JSON.stringify({ lobbyId, user }),
  });
}

/**
 * Leave the current lobby
 */
export function leaveLobby(user: User): void {
  if (!stompClient || !currentLobbyId) return;

  stompClient.publish({
    destination: "/app/lobby/leave",
    body: JSON.stringify({ lobbyId: currentLobbyId, user }),
  });

  currentLobbyId = null;
}

/**
 * Start the multiplayer game
 */
export function startGame(lobbyId: string): void {
  if (!stompClient) return;

  stompClient.publish({
    destination: "/app/lobby/start",
    body: JSON.stringify({ lobbyId }),
  });
}

/**
 * Disconnect from lobby
 */
export function disconnectLobby(): void {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    onLobbyUpdateCallback = null;
    onGameStartCallback = null;
    currentLobbyId = null;
    console.log("Disconnected from lobby");
  }
}
