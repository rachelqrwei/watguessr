import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';
import store from '../stores';

// STOMP client for multiplayer game state updates
let stompClient: Client | null = null;

export interface MultiplayerGameStateDto {
  gameId: string;
  players: Record<string, PlayerStateDto>;
  currentRound: number;
  maxRounds: number;
  timer: number;
  finalWinner: string | null;
  shouldEnd: boolean;
  gameStatus: 'loading' | 'playing' | 'round-complete' | 'game-complete';
  currentSceneId: string | null;
}

export interface PlayerStateDto {
  userId: string;
  username: string;
  score: number;
  status: 'idle' | 'loading' | 'playing' | 'ended';
  roundNumber: number;
  completionTime: number | null;
  isReady: boolean;
}

export function connectToMultiplayerGame(gameId: string) {
  const socket = new SockJS('http://localhost:5173/ws-game');
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('Connected to Multiplayer Game WebSocket');

    // Subscribe to game state updates for this specific game
    stompClient?.subscribe(`/topic/game/${gameId}/state`, (message) => {
      const gameState: MultiplayerGameStateDto = JSON.parse(message.body);
      handleGameStateUpdate(gameState);
    });

    // Subscribe to round start events
    stompClient?.subscribe(`/topic/game/${gameId}/round-start`, (message) => {
      const roundData = JSON.parse(message.body);
      handleRoundStart(roundData);
    });

    // Subscribe to game completion events
    stompClient?.subscribe(`/topic/game/${gameId}/complete`, (message) => {
      const completionData = JSON.parse(message.body);
      handleGameComplete(completionData);
    });
  }, (error: any) => {
    console.error('WebSocket connection error:', error);
    // Retry connection after 3 seconds
    setTimeout(() => {
      console.log('Retrying WebSocket connection...');
      connectToMultiplayerGame(gameId);
    }, 3000);
  });
}

export function disconnectFromMultiplayerGame() {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect(() => {
      console.log('Disconnected from Multiplayer Game WebSocket');
    });
    stompClient = null;
  }
}

// Send player progress update
export function sendPlayerProgress(gameId: string, userId: string, score: number, status: string) {
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot send player progress');
    return;
  }

  const progressData = {
    gameId: gameId,
    userId: userId,
    score: score,
    status: status
  };

  stompClient.send('/app/game/update-progress', {}, JSON.stringify(progressData));
}

// Send player ready status
export function sendPlayerReady(gameId: string, userId: string) {
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot send ready status');
    return;
  }

  const readyData = {
    gameId: gameId,
    userId: userId
  };

  stompClient.send('/app/game/ready', {}, JSON.stringify(readyData));
}

// Send round start request
export function sendStartRound(gameId: string, sceneId: string) {
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot start round');
    return;
  }

  const startData = {
    gameId: gameId,
    sceneId: sceneId
  };

  stompClient.send('/app/game/start-round', {}, JSON.stringify(startData));
}

// Handle incoming game state updates
function handleGameStateUpdate(gameState: MultiplayerGameStateDto) {
  console.log('📡 Received game state update:', gameState);

  // Get current players from store to detect disconnections
  const currentPlayers = store.getters['multiplayerGame/multiplayerGame_getPlayers'] || {};
  
  // Convert backend DTO format to frontend store format
  const players: Record<string, { status: any; score: number; username: string }> = {};

  Object.entries(gameState.players).forEach(([playerId, playerState]) => {
    players[playerId] = {
      status: playerState.status,
      score: playerState.score,
      username: playerState.username
    };
  });

  console.log('🔄 Updating players state:', players);

  // Check for disconnected players
  Object.keys(currentPlayers).forEach(playerId => {
    if (!players[playerId]) {
      console.log('🔌 Player disconnected:', playerId);
      store.dispatch('multiplayerGame/multiplayerGame_handlePlayerDisconnection', playerId);
    }
  });

  // Update Vuex store
  store.commit('multiplayerGame/MG_SET_GAME_ID', gameState.gameId);
  store.commit('multiplayerGame/MG_SET_PLAYERS', players);
  store.commit('multiplayerGame/MG_SET_CURRENT_ROUND', gameState.currentRound);
  store.commit('multiplayerGame/MG_SET_MAX_ROUNDS', gameState.maxRounds);

  if (gameState.finalWinner) {
    store.commit('multiplayerGame/MG_SET_FINAL_WINNER', gameState.finalWinner);
  }

  if (gameState.shouldEnd) {
    store.commit('multiplayerGame/MG_SET_SHOULD_END', true);
  }
}

// Handle round start events
function handleRoundStart(roundData: any) {
  console.log('🎮 Round started:', roundData);
  console.log('📋 Current view before change:', store.getters['gameInfo/getCurrentView']);
  
  // Start a new round in the frontend
  if (roundData.roundId) {
    // Set the new round ID in the round store
    store.commit('round/SET_ROUND_ID', roundData.roundId);
    
    // Fetch the scene image for the new round
    fetchSceneImage(roundData.roundId);
    
    // Update multiplayer game round number
    if (roundData.roundNumber) {
      store.commit('multiplayerGame/MG_SET_CURRENT_ROUND', roundData.roundNumber);
    }
  }
  
  // Reset guess state for new round
  store.commit('guess/RESET_GUESS', null);
  
  // Reset guess building and floor selections
  store.commit('guess/SET_BUILDING', '');
  store.commit('guess/SET_FLOOR', '');
  
  // Update UI to show new round has started
  console.log('🗺️ Setting view to Map for new round');
  
  // Force the view change with a small delay to ensure it takes effect
  setTimeout(() => {
    store.commit('gameInfo/SET_CURRENT_VIEW', 'Map');
    console.log('📋 Current view after change:', store.getters['gameInfo/getCurrentView']);
  }, 100);
}

// Helper function to fetch scene image
async function fetchSceneImage(roundId: string) {
  try {
    const response = await fetch(`http://localhost:5173/api/scene/image?roundId=${roundId}`);
    if (response.ok) {
      const imageUrl = await response.text();
      store.commit('round/SET_IMAGE_URL', imageUrl || null);
    } else {
      store.commit('round/SET_IMAGE_URL', null);
    }
  } catch (error) {
    console.error('Failed to fetch scene image:', error);
    store.commit('round/SET_IMAGE_URL', null);
  }
}

// Handle game completion events
function handleGameComplete(completionData: MultiplayerGameStateDto) {
  console.log('🏆 Game completed:', completionData);
  
  // Update the game state with final results
  if (completionData.finalWinner) {
    store.commit('multiplayerGame/MG_SET_FINAL_WINNER', completionData.finalWinner);
  }
  
  if (completionData.shouldEnd) {
    store.commit('multiplayerGame/MG_SET_SHOULD_END', true);
  }
  
  // Update players with final scores
  if (completionData.players) {
    const players: Record<string, { status: any; score: number; username: string }> = {};
    Object.entries(completionData.players).forEach(([playerId, playerState]) => {
      players[playerId] = {
        status: playerState.status,
        score: playerState.score,
        username: playerState.username
      };
    });
    store.commit('multiplayerGame/MG_SET_PLAYERS', players);
  }
  
  console.log('🎮 Navigating to multiplayer game end...');
  // Navigate to multiplayer game end screen
  window.location.href = '/multiplayer-game-end';
}
