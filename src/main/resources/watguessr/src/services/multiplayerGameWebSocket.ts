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
  status: 'idle' | 'loading' | 'playing' | 'ended' | 'ready' | 'completed';
  roundNumber: number;
  completionTime: number | null;
  isReady: boolean;
}

export function connectToMultiplayerGame(gameId: string) {
  const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws-game`);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('✅ WebSocket connected successfully for game:', gameId);
    console.log('✅ Creating subscriptions...');
    
    // Subscribe to game state updates for this specific game
    const stateSubscription = stompClient?.subscribe(`/topic/game/${gameId}/state`, (message) => {
      console.log('📊 Game state subscription triggered!');
      console.log('📊 Message received:', message);
      console.log('📊 Message body:', message.body);
      const gameState: MultiplayerGameStateDto = JSON.parse(message.body);
      handleGameStateUpdate(gameState);
    });
    console.log('📡 Game state subscription created for topic:', `/topic/game/${gameId}/state`);
    console.log('📡 Subscription object:', stateSubscription);

    // Subscribe to round start events
    const roundStartSubscription = stompClient?.subscribe(`/topic/game/${gameId}/round-start`, (message) => {
      console.log('🎯 Round start subscription triggered!');
      console.log('🎯 Message received:', message);
      console.log('🎯 Message body:', message.body);
      const roundData = JSON.parse(message.body);
      console.log('🎯 Parsed round data:', roundData);
      handleRoundStart(roundData);
    });
    console.log('📡 Round start subscription created for topic:', `/topic/game/${gameId}/round-start`);
    console.log('📡 Subscription object:', roundStartSubscription);

    // Subscribe to game completion events
    const completionSubscription = stompClient?.subscribe(`/topic/game/${gameId}/complete`, (message) => {
      console.log('🏆 Game completion subscription triggered!');
      console.log('🏆 Message received:', message);
      console.log('🏆 Message body:', message.body);
      const completionData = JSON.parse(message.body);
      handleGameComplete(completionData);
    });
    console.log('📡 Game completion subscription created for topic:', `/topic/game/${gameId}/complete`);
    console.log('📡 Subscription object:', completionSubscription);
    
    console.log('✅ All subscriptions created successfully');
    
    // Request current round state to catch up on missed events
    requestCurrentRoundState(gameId);
    
  }, (error: any) => {
    console.error('WebSocket connection error:', error);
    // Retry connection after 3 seconds
    setTimeout(() => {
      connectToMultiplayerGame(gameId);
    }, 3000);
  });
}

export function disconnectFromMultiplayerGame() {
  if (stompClient && stompClient.connected) {
    stompClient.disconnect(() => {
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

// Send player ready status
export function sendPlayerCompleted(gameId: string, userId: string) {
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot send completed');
    return;
  }

  const completedData = {
    gameId: gameId,
    userId: userId
  };

  stompClient.send('/app/game/completed', {}, JSON.stringify(completedData));
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

  // Check for disconnected players
  Object.keys(currentPlayers).forEach(playerId => {
    if (!players[playerId]) {
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
  console.log("YO");

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

  store.commit('gameInfo/SET_MAP_CENTER', null);
  store.commit('gameInfo/SET_MAP_ZOOM', null);

  // Force the view change with a small delay to ensure it takes effect
  setTimeout(() => {
    store.commit('gameInfo/SET_CURRENT_VIEW', 'Image');
  }, 100);
}

// Helper function to fetch scene image
async function fetchSceneImage(roundId: string) {
  try {
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/scene/image?roundId=${roundId}`);
    if (response.ok) {
      const blob = await response.blob();
      const imageUrl = URL.createObjectURL(blob);
      store.commit('round/SET_IMAGE_URL', imageUrl || null);
    } else {
      store.commit('round/SET_IMAGE_URL', null);
    }
  } catch (error) {
    console.error('Failed to fetch scene image:', error);
    store.commit('round/SET_IMAGE_URL', null);
  }
}

// Request current round state to catch up on missed events
async function requestCurrentRoundState(gameId: string) {
  try {
    console.log('🔄 Requesting current round state for game:', gameId);
    
    // Use the existing endpoint that returns rounds for a game
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/round/by-game-with-guesses?gameId=${gameId}`, {
      credentials: "include"
    });
    
    if (response.ok) {
      const roundsData = await response.json();
      console.log('🔄 Received rounds data:', roundsData);
      
      if (roundsData && roundsData.length > 0) {
        // Get the most recent round (last in the array)
        const currentRound = roundsData[roundsData.length - 1];
        console.log('🔄 Current round data:', currentRound);
        
        if (currentRound.roundId) {
          // Set the round ID in the round store
          store.commit('round/SET_ROUND_ID', currentRound.roundId);
          
          // Fetch the scene image for the current round
          fetchSceneImage(currentRound.roundId);
          
          // Update multiplayer game round number (use array length as round number)
          store.commit('multiplayerGame/MG_SET_CURRENT_ROUND', roundsData.length);
          
          console.log('✅ Successfully synced current round state. Round ID:', currentRound.roundId);
        }
      } else {
        console.log('ℹ️ No rounds found for game yet');
      }
    } else {
      console.warn('⚠️ Failed to fetch rounds data:', response.status);
    }
  } catch (error) {
    console.error('❌ Error requesting current round state:', error);
  }
}

// Handle game completion events
function handleGameComplete(completionData: MultiplayerGameStateDto) {
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

    const finalGameData = {
      players: players,
      finalWinner: completionData.finalWinner,
      gameId: completionData.gameId,
      currentRound: completionData.currentRound,
      maxRounds: completionData.maxRounds
    };

    store.commit('multiplayerGame/MG_SAVE_FINAL_GAME_DATA', finalGameData);
  }

  // Navigate to multiplayer game end screen
  // Use window.location for now since Vue Router context is not available here
  if (window.location.pathname !== '/multiplayer-game-end') {
    window.location.href = '/multiplayer-game-end';
  }
}
