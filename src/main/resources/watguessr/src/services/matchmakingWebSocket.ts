import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';
import store from '../stores';

// STOMP client for multiplayer game state updates
let stompClient: Client | null = null;

export function joinRankedQueue(userId: string) {
  if (stompClient && stompClient.connected) {
    const message = {
      userId: userId
    };
    stompClient.send('/app/matchmaking/join', {}, JSON.stringify(message));
  } else {
    console.warn('⚠️ WebSocket not connected, cannot join queue');
  }
}

export function leaveRankedQueue(userId: string) {
  if (stompClient && stompClient.connected) {
    const message = {
      userId: userId
    };
    stompClient.send('/app/matchmaking/leave', {}, JSON.stringify(message));
  } else {
    console.warn('⚠️ WebSocket not connected, cannot leave queue');
  }
}

export function connectToMatchmakingWebSocket(userId: string, callbacks: {
  onQueueJoined?: () => void;
  onQueueLeft?: () => void;
  onMatchFound?: (matchInfo: any) => void;
  onQueueTimeout?: (message: string) => void;
  onError?: (error: string) => void;
} = {}) {
  const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws-matchmaking`);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    // Subscribe to matchmaking updates
    stompClient!.subscribe(`/topic/matchmaking/${userId}`, (message) => {
      const data = JSON.parse(message.body);

      // Handle different types of matchmaking messages
      switch (data.type) {
        case 'in_queue':
          callbacks.onQueueJoined?.();
          break;
        case 'left_queue':
          // callbacks.onQueueLeft?.();
          break;
        case 'match_found':
          callbacks.onMatchFound?.(data.data);
          break;
        case 'queue_timeout':
          callbacks.onQueueTimeout?.(data.data?.message || 'Queue timeout');
          break;
      }
    });

  }, (error: any) => {
    console.error('❌ WebSocket connection error:', error);
    callbacks.onError?.('Connection failed. Retrying...');
    // Retry connection after 3 seconds
    setTimeout(() => {
      connectToMatchmakingWebSocket(userId, callbacks);
    }, 3000);
  });
}

export function disconnectFromMatchmakingWebSocket() {
  if (stompClient) {
    if (stompClient.connected) {
      stompClient.disconnect(() => {
      });
    }
    stompClient = null;
  }
}
