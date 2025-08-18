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

export function testMatchmakingWebSocket() {
  if (stompClient && stompClient.connected) {
    const testMessage = {
      type: 'test',
      message: 'Hello from frontend!',
      timestamp: new Date().toISOString()
    };
    stompClient.send('/app/matchmaking/test', {}, JSON.stringify(testMessage));
    console.log('🧪 Test message sent:', testMessage);
  } else {
    console.warn('⚠️ WebSocket not connected, cannot send test message');
  }
}

export function connectToMatchmakingWebSocket() {
  const socket = new SockJS('/ws-matchmaking');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('✅ Connected to Matchmaking WebSocket');

    // Subscribe to matchmaking updates
    stompClient!.subscribe('/topic/matchmaking/updates', (message) => {
      const data = JSON.parse(message.body);
      console.log('📨 Received matchmaking update:', data);

      // Handle different types of matchmaking messages
      switch (data.type) {
        case 'test_response':
          console.log('🧪 Test response received:', data);
          break;
        case 'queue_joined':
          console.log('🎯 Successfully joined ranked queue');
          break;
        case 'match_found':
          console.log('🎮 Match found!', data);
          break;
        default:
          console.log('📬 Unknown message type:', data.type);
      }
    });

  }, (error: any) => {
    console.error('❌ WebSocket connection error:', error);
    // Retry connection after 3 seconds
    setTimeout(() => {
      console.log('🔄 Retrying WebSocket connection...');
      connectToMatchmakingWebSocket();
    }, 3000);
  });
}

export function disconnectFromMatchmakingWebSocket() {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect(() => {
      console.log('🔌 Disconnected from Matchmaking WebSocket');
    });
  }
  stompClient = null;
}

