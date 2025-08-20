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
    console.log('📤 Join queue message sent:', message);
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
    console.log('📤 Leave queue message sent:', message);
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
  const socket = new SockJS('/ws-matchmaking');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('✅ Connected to Matchmaking WebSocket');
    console.log("subscribing to /topic/matchmaking/" + userId);

    // Subscribe to matchmaking updates
    stompClient!.subscribe(`/topic/matchmaking/${userId}`, (message) => {
      const data = JSON.parse(message.body);
      console.log('📨 Received matchmaking update:', data);

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
        default:
          console.log('📬 Unknown message type:', data.type);
      }
    });

  }, (error: any) => {
    console.error('❌ WebSocket connection error:', error);
    callbacks.onError?.('Connection failed. Retrying...');
    // Retry connection after 3 seconds
    setTimeout(() => {
      console.log('🔄 Retrying WebSocket connection...');
      connectToMatchmakingWebSocket(userId, callbacks);
    }, 3000);
  });
}

export function disconnectFromMatchmakingWebSocket() {
  if (stompClient) {
    if (stompClient.connected) {
      console.log('🔌 Disconnecting from Matchmaking WebSocket...');
      stompClient.disconnect(() => {
        console.log('✅ Successfully disconnected from Matchmaking WebSocket');
      });
    } else {
      console.log('⚠️ STOMP client exists but is not connected');
    }
    stompClient = null;
  } else {
    console.log('⚠️ No STOMP client to disconnect');
  }
}
