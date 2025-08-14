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

/**
 * Connect to the lobby
 */
export function connectLobby(
  onLobbyUpdate: (update: LobbyUpdate) => void,
  onGameStart: (info: GameStartInfo) => void
): void {
  onLobbyUpdateCallback = onLobbyUpdate;
  onGameStartCallback = onGameStart;

  const socket = new SockJS("http://localhost:8080/ws-game"); // your backend URL
  stompClient = new Client({
    webSocketFactory: () => socket as any, // SockJS factory
    debug: (msg) => console.log(msg),
    reconnectDelay: 5000,
    onConnect: (frame: Frame) => {
      console.log("Connected to lobby:", frame);

      // Subscribe to lobby updates
      stompClient?.subscribe("/topic/lobby", (message) => {
        const update: LobbyUpdate = JSON.parse(message.body);
        onLobbyUpdateCallback?.(update);
      });

      // Subscribe to game start events
      stompClient?.subscribe("/topic/lobby/start", (message) => {
        const info: GameStartInfo = JSON.parse(message.body);
        onGameStartCallback?.(info);
      });
    },
    onStompError: (frame) => {
      console.error("STOMP error:", frame);
    },
  });

  stompClient.activate();
}

/**
 * Join a lobby
 */
export function joinLobby(user: User): void {
  if (!stompClient) return;
  stompClient.publish({
    destination: "/app/lobby/join",
    body: JSON.stringify(user),
  });
}

/**
 * Leave a lobby
 */
export function leaveLobby(user: User): void {
  if (!stompClient) return;
  stompClient.publish({
    destination: "/app/lobby/leave",
    body: JSON.stringify(user),
  });
}

/**
 * Start the multiplayer game
 */
export function startGame(): void {
  if (!stompClient) return;
  stompClient.publish({
    destination: "/app/lobby/start",
    body: JSON.stringify({}),
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
    console.log("Disconnected from lobby");
  }
}
