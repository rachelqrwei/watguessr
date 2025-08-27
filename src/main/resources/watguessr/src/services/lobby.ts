import SockJS from "sockjs-client";
import { Client, type Frame } from "@stomp/stompjs";
import store from '../stores';

export interface User {
  id: string;
  username: string;
}

export interface LobbyUpdate {
  players: LobbyPlayer[];
}

export interface LobbyPlayer {
  userId: string;
  username: string;
  ready: boolean;
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
      console.log('Connected to lobby WebSocket');
      startHeartbeat();
      setupPageUnloadHandling();
      
      // Subscribe to public lobby updates
      if (stompClient) {
        stompClient.subscribe(`/topic/lobbies/public`, (message) => {
          console.log('Received public lobby update:', message.body);
          // Dispatch a custom event that components can listen to
          window.dispatchEvent(new CustomEvent('publicLobbyUpdate', {
            detail: { message: message.body }
          }));
        });
      }
    },
    onStompError: (frame) => {
      console.error("STOMP error:", frame);
    },
    onDisconnect: () => {
      console.log('Disconnected from lobby WebSocket');
      stopHeartbeat();
      cleanupPageUnloadHandling();
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
      // Get current user from store if available
      const currentUser = store?.getters?.['user/getCurrentUser'];
      const userId = currentUser?.id;

      if (userId) {
        stompClient.publish({
          destination: "/app/lobby/heartbeat",
          body: JSON.stringify({ 
            lobbyId: currentLobbyId,
            userId: userId 
          }),
        });
      } else {
        console.warn('No user ID available for heartbeat');
      }
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
  if (!currentLobbyId) { 
    console.log('No current lobby ID to cleanup');
    return; 
  }

  // Get current user from store if available
  const currentUser = store?.getters?.['user/getCurrentUser'];
  const userId = currentUser?.id;

  console.log('Sending lobby cleanup:', { lobbyId: currentLobbyId, userId, hasUser: !!currentUser });

  if (stompClient?.connected) {
    try {
      // Always send both lobbyId and userId when available for consistent backend handling
      const cleanupData = { 
        lobbyId: currentLobbyId,
        userId: userId || null // Send null if not available
      };

      if (userId) {
        // Send leave message with user info
        stompClient.publish({
          destination: "/app/lobby/leave",
          body: JSON.stringify({ 
            lobbyId: currentLobbyId, 
            user: { id: userId, username: currentUser.username } 
          }),
        });
        console.log('Sent leave message via WebSocket for user:', userId);
      } else {
        // Send general cleanup with just lobbyId
        stompClient.publish({
          destination: "/app/lobby/cleanup",
          body: currentLobbyId, // Send just the lobbyId string, not an object
        });
        console.log('Sent general cleanup message via WebSocket');
      }
    } catch (error) {
      console.error("Failed to send lobby cleanup via WS:", error);
      // Fall through to REST API fallback
    }
  } else {
    console.log('WebSocket not connected, using REST API fallback');
  }

  // Always try REST API fallback for reliability
  const cleanupData = userId 
    ? { lobbyId: currentLobbyId, userId: userId }
    : { lobbyId: currentLobbyId };
    
  fetch(`${import.meta.env.VITE_API_BASE_URL}/api/lobby/cleanup`, {
    method: "POST",
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(cleanupData),
  })
  .then(response => {
    if (response.ok) {
      console.log('REST API cleanup successful');
    } else {
      console.error('REST API cleanup failed:', response.status);
    }
  })
  .catch((err) => console.error("Failed to cleanup lobby via REST:", err));

  currentLobbyId = null; // prevent multiple cleanup calls
}

/**
 * Setup page unload handling
 */
function setupPageUnloadHandling() {
  // Only cleanup when user leaves / refreshes the page
  window.addEventListener("beforeunload", () => {
    console.log('Page unloading - sending cleanup');
    sendCleanup();
  });
}

/**
 * Cleanup event listeners
 */
function cleanupPageUnloadHandling() {
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
  if (!stompClient || !stompClient.connected || !currentLobbyId) {
    console.log('Cannot leave lobby:', { 
      hasStompClient: !!stompClient, 
      isConnected: stompClient?.connected, 
      currentLobbyId 
    });
    return;
  }

  console.log('Leaving lobby:', currentLobbyId, 'for user:', user);

  try {
    stompClient.publish({
      destination: "/app/lobby/leave",
      body: JSON.stringify({ lobbyId: currentLobbyId, user }),
    });
    console.log('Successfully sent leave message for user:', user.username);
  } catch (error) {
    console.error('Failed to send leave message:', error);
  }

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
  cleanupPageUnloadHandling();

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
