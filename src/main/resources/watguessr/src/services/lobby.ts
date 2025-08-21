import SockJS from "sockjs-client";
import { Client, type Frame } from "@stomp/stompjs";

export interface User {
  id: string;
  username: string;
}

export interface LobbyPlayer {
  userId: string;
  username: string;
  ready: boolean;
}

export interface LobbyUpdate {
  players: LobbyPlayer[];
}

export interface GameStartInfo {
  gameId: string;
  players: User[];
}

let stompClient: Client | null = null;
let onLobbyUpdateCallback: ((update: LobbyUpdate) => void) | null = null;
let onGameStartCallback: ((info: GameStartInfo) => void) | null = null;
let currentLobbyId: string | null = null;
let heartbeatInterval: NodeJS.Timeout | null = null;
let pageVisibilityHandler: (() => void) | null = null;

/**
 * Connect to the lobby WebSocket
 */
export function connectLobby(
  onLobbyUpdate: (update: LobbyUpdate) => void,
  onGameStart: (info: GameStartInfo) => void
): void {
  onLobbyUpdateCallback = onLobbyUpdate;
  onGameStartCallback = onGameStart;

  const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws-game`);
  stompClient = new Client({
    webSocketFactory: () => socket as any,
    debug: (msg) => console.log(msg),
    reconnectDelay: 5000,
    onConnect: (frame: Frame) => {
      startHeartbeat();
      setupPageVisibilityHandling();
    },
    onStompError: (frame) => {
      console.error("STOMP error:", frame);
    },
    onDisconnect: () => {
      stopHeartbeat();
      cleanupPageVisibilityHandling();
    },
  });

  stompClient.activate();
}

/**
 * Start heartbeat
 */
function startHeartbeat() {
  if (heartbeatInterval) clearInterval(heartbeatInterval);

  heartbeatInterval = setInterval(() => {
    if (stompClient?.connected && currentLobbyId) {
      stompClient.publish({
        destination: "/app/lobby/heartbeat",
        body: JSON.stringify({ lobbyId: currentLobbyId }),
      });
    }
  }, 30000);
}

/**
 * Stop heartbeat
 */
function stopHeartbeat() {
  if (heartbeatInterval) {
    clearInterval(heartbeatInterval);
    heartbeatInterval = null;
  }
}

/**
 * Send lobby cleanup
 */
function sendCleanup() {
  if (!currentLobbyId) return;

  if (stompClient?.connected) {
    try {
      stompClient.publish({
        destination: "/app/lobby/cleanup",
        body: JSON.stringify({ lobbyId: currentLobbyId }),
      });
    } catch (error) {
      console.error("Failed to send lobby cleanup via WS:", error);
    }
  } else {
    // Fallback: send cleanup via REST API if WebSocket not connected
    fetch(`${import.meta.env.VITE_API_BASE_URL}/api/lobbies/${currentLobbyId}/cleanup`, {
      method: "POST",
    }).catch((err) => console.error("Failed to cleanup lobby via REST:", err));
  }

  currentLobbyId = null; // prevent multiple cleanup calls
}

/**
 * Setup page visibility & unload handling
 */
function setupPageVisibilityHandling() {
  // Remove aggressive cleanup on tab switch
  document.addEventListener("visibilitychange", pageVisibilityHandler);

  // Only cleanup when user leaves / refreshes
  window.addEventListener("beforeunload", () => {
    sendCleanup();
  });
}

/**
 * Cleanup event listeners
 */
function cleanupPageVisibilityHandling() {
  if (pageVisibilityHandler) {
    document.removeEventListener("visibilitychange", pageVisibilityHandler);
    pageVisibilityHandler = null;
  }
  window.removeEventListener("beforeunload", sendCleanup);
}

/**
 * Join a specific lobby
 */
export function joinLobby(user: User, lobbyId: string): void {
  if (!stompClient) return;

  currentLobbyId = lobbyId;

  const subscribeAndJoin = () => {
    if (!stompClient) return;

    stompClient.subscribe(`/topic/lobby/${lobbyId}`, (message) => {
      const update: LobbyUpdate = JSON.parse(message.body);
      onLobbyUpdateCallback?.(update);
    });

    stompClient.subscribe(`/topic/lobby/${lobbyId}/start`, (message) => {
      const info: GameStartInfo = JSON.parse(message.body);
      onGameStartCallback?.(info);
    });

    stompClient.publish({
      destination: "/app/lobby/join",
      body: JSON.stringify({ lobbyId, user }),
    });
  };

  if (stompClient.connected) {
    subscribeAndJoin();
  } else {
    const checkConnection = () => {
      if (stompClient?.connected) subscribeAndJoin();
      else setTimeout(checkConnection, 100);
    };
    checkConnection();
  }
}

/**
 * Leave the current lobby
 */
export function leaveLobby(user: User): void {
  if (!stompClient || !stompClient.connected || !currentLobbyId) return;

  stompClient.publish({
    destination: "/app/lobby/leave",
    body: JSON.stringify({ lobbyId: currentLobbyId, user }),
  });

  currentLobbyId = null;
}

/**
 * Set player ready status
 */
export function setPlayerReady(userId: string, ready: boolean): void {
  if (!stompClient || !stompClient.connected || !currentLobbyId) return;

  stompClient.publish({
    destination: "/app/lobby/ready",
    body: JSON.stringify({ lobbyId: currentLobbyId, userId, ready }),
  });
}

/**
 * Start the game
 */
export function startGame(lobbyId: string, gameMode: string, roundCount: number, timer: number): void {
  if (!stompClient || !stompClient.connected) return;

  stompClient.publish({
    destination: "/app/lobby/start",
    body: JSON.stringify({ lobbyId, gameMode, roundCount, timer }),
  });
}

/**
 * Disconnect from lobby (cleanup)
 */
export function disconnectLobby(): void {
  sendCleanup();
  stopHeartbeat();
  cleanupPageVisibilityHandling();

  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }

  onLobbyUpdateCallback = null;
  onGameStartCallback = null;
  currentLobbyId = null;
}

/**
 * Force leave a lobby
 */
export function forceLeaveLobby(lobbyId: string, user: User): void {
  if (stompClient?.connected) {
    stompClient.publish({
      destination: "/app/lobby/force-leave",
      body: JSON.stringify({ lobbyId, user }),
    });
  }
}
