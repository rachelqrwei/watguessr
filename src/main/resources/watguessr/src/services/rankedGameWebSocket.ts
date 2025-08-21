import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';
import store from '../stores';

// STOMP client for multiplayer game state updates
let stompClient: Client | null = null;

export interface RankedGameStateDto {
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

export function connectToRankedGame(gameId: string) {
  const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws-game`);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {

    // Subscribe to game state updates for this specific game
    const stateSubscription = stompClient?.subscribe(`/topic/ranked-game/${gameId}/state`, (message) => {
      const gameState: RankedGameStateDto = JSON.parse(message.body);
      handleGameStateUpdate(gameState);
    });

    // Subscribe to round start events
    const roundStartSubscription = stompClient?.subscribe(`/topic/ranked-game/${gameId}/round-start`, (message) => {
      const roundData = JSON.parse(message.body);
      handleRoundStart(roundData);
    });

    // Subscribe to game completion events
    const completionSubscription = stompClient?.subscribe(`/topic/ranked-game/${gameId}/complete`, (message) => {
      const completionData = JSON.parse(message.body);
      handleGameComplete(completionData);
    });

    // Request current round state to catch up on missed events
    requestCurrentRoundState(gameId);

  }, (error: any) => {
    console.error('WebSocket connection error:', error);
    // Retry connection after 3 seconds
    setTimeout(() => {
      connectToRankedGame(gameId);
    }, 3000);
  });
}

export function disconnectFromRankedGame() {
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

  stompClient.send('/app/ranked-game/update-progress', {}, JSON.stringify(progressData));
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

  stompClient.send('/app/ranked-game/ready', {}, JSON.stringify(readyData));
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

  stompClient.send('/app/ranked-game/completed', {}, JSON.stringify(completedData));
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

  stompClient.send('/app/ranked-game/start-round', {}, JSON.stringify(startData));
}

// Handle incoming game state updates
function handleGameStateUpdate(gameState: RankedGameStateDto) {
  // Get current players from store to detect disconnections
  const currentPlayers = store.getters['rankedGame/rankedGame_getPlayers'] || {};

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
      store.dispatch('rankedGame/rankedGame_handlePlayerDisconnection', playerId);
    }
  });

  // Update Vuex store
  store.commit('rankedGame/RG_SET_GAME_ID', gameState.gameId);
  store.commit('rankedGame/RG_SET_PLAYERS', players);
  store.commit('rankedGame/RG_SET_CURRENT_ROUND', gameState.currentRound);

  if (gameState.finalWinner) {
    store.commit('rankedGame/RG_SET_FINAL_WINNER', gameState.finalWinner);
  }

  if (gameState.shouldEnd) {
    store.commit('rankedGame/RG_SET_SHOULD_END', true);
  }
}

// Handle round start events
function handleRoundStart(roundData: any) {
  // Start a new round in the frontend
  if (roundData.roundId) {
    // Set the new round ID in the round store
    store.commit('round/SET_ROUND_ID', roundData.roundId);

    // Fetch the scene image for the new round
    fetchSceneImage(roundData.roundId);

    // Update multiplayer game round number
    if (roundData.roundNumber) {
      store.commit('rankedGame/RG_SET_CURRENT_ROUND', roundData.roundNumber);
    }
  }

  // Reset guess state for new round
  store.commit('guess/RESET_GUESS', null);

  // Reset guess building and floor selections
  store.commit('guess/SET_BUILDING', '');
  store.commit('guess/SET_FLOOR', '');

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
    // Use the existing endpoint that returns rounds for a game
    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/round/by-game-with-guesses?gameId=${gameId}`, {
      credentials: "include"
    });

    if (response.ok) {
      const roundsData = await response.json();

      if (roundsData && roundsData.length > 0) {
        // Get the most recent round (last in the array)
        const currentRound = roundsData[roundsData.length - 1];

        if (currentRound.roundId) {
          // Set the round ID in the round store
          store.commit('round/SET_ROUND_ID', currentRound.roundId);

          // Fetch the scene image for the current round
          fetchSceneImage(currentRound.roundId);
        }
      }
    } else {
      console.warn('⚠️ Failed to fetch rounds data:', response.status);
    }
  } catch (error) {
    console.error('❌ Error requesting current round state:', error);
  }
}

// Handle game completion events
function handleGameComplete(completionData: RankedGameStateDto) {
  // Update the game state with final results
  if (completionData.finalWinner) {
    store.commit('rankedGame/RG_SET_FINAL_WINNER', completionData.finalWinner);
  }

  if (completionData.shouldEnd) {
    store.commit('rankedGame/RG_SET_SHOULD_END', true);
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

    store.commit('rankedGame/RG_SAVE_FINAL_GAME_DATA', finalGameData);
  }

  // Navigate to multiplayer game end screen
  // Use window.location for now since Vue Router context is not available here
  if (window.location.pathname !== '/multiplayer-game-end') {
    window.location.href = '/multiplayer-game-end';
  }
}
