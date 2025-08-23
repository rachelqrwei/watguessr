import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';
import store from '../stores';

// STOMP client for multiplayer game state updates
let stompClient: Client | null = null;
let heartbeatInterval: number | null = null;

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
  rankedGameResult?: {
    eloChanges: Record<string, number>;
    userPoints: Record<string, number>;
  };
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

  stompClient.connect({}, (frame) => {
    // Start heartbeat when connected
    startHeartbeat(gameId);

    // Subscribe to game state updates
    stompClient?.subscribe(`/topic/ranked-game/${gameId}/state`, (message) => {
      const gameState: RankedGameStateDto = JSON.parse(message.body);
      handleGameStateUpdate(gameState);
    });

    // Subscribe to round start events
    stompClient?.subscribe(`/topic/ranked-game/${gameId}/round-start`, (message) => {
      const roundData = JSON.parse(message.body);
      handleRoundStart(roundData);
    });

    // Subscribe to game completion events
    stompClient?.subscribe(`/topic/ranked-game/${gameId}/complete`, (message) => {
      const completionData = JSON.parse(message.body);
      handleGameComplete(completionData);
    });

    // Request current round state to catch up on missed events
    requestCurrentRoundState(gameId);

  }, (error: any) => {
    console.error('WebSocket connection error:', error);

    // Stop heartbeat on disconnect/error
    stopHeartbeat();

    // Retry connection after 3 seconds
    setTimeout(() => {
      connectToRankedGame(gameId);
    }, 3000);
  });
}


export function disconnectFromRankedGame() {
  if (stompClient && stompClient.connected) {
    stopHeartbeat();

    stompClient.disconnect(() => {
      store.commit('guess/RESET_GUESS', null);
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
  // Don't update store if we're on a game end route to prevent interference
  if (window.location.pathname.includes('-game-end')) {
    return;
  }

  const currentUser = store.getters['user/getCurrentUser'] || {};

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

  const playerIds = Object.keys(players);
  if (playerIds.length === 1 && playerIds[0] === currentUser.id) {
    store.dispatch('rankedGame/rankedGame_endGame', null);
    alert("⚠️ I'm the only one left, leaving game...");
    window.location.href = "/";
  }


  // Update Vuex store
  store.commit('rankedGame/RG_SET_GAME_ID', gameState.gameId);
  store.commit('rankedGame/RG_SET_PLAYERS', players);
  store.commit('rankedGame/RG_SET_CURRENT_ROUND', gameState.currentRound);
  store.commit('rankedGame/RG_SET_MAX_ROUNDS', gameState.maxRounds);

  if (gameState.finalWinner) {
    store.commit('rankedGame/RG_SET_FINAL_WINNER', gameState.finalWinner);
  }

  if (gameState.shouldEnd) {
    store.commit('rankedGame/RG_SET_SHOULD_END', true);
  }

  // Store pre-game ELOs for all players if we don't have them yet
  const currentPreGameElos = store.getters['rankedGame/rankedGame_getPreGameElos'] || {};
}

// Handle round start events
function handleRoundStart(roundData: any) {
  // Start a new round in the frontend
  if (roundData.roundId) {
   //reset round state to clear previous round data (including correct answer)
    store.commit('round/RESET_ROUND', null);

    // Set the new round ID in the round store
    store.commit('round/SET_ROUND_ID', roundData.roundId);

    // Fetch the scene image for the new round
    fetchSceneImage(roundData.roundId);

    // Update ranked game round number
    if (roundData.roundNumber) {
      store.commit('rankedGame/RG_SET_CURRENT_ROUND', roundData.roundNumber);
    }
  }

  // Reset guess state for new round
  store.commit('guess/RESET_GUESS', null);

  // Reset guess building and floor selections
  store.commit('guess/SET_BUILDING', '');
  store.commit('guess/SET_FLOOR', '');

  // Do not force the view change here. The Play view will switch to 'Image'
  // after the countdown/progress bar completes to keep RoundEnd visible during transition.
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

  // IMPORTANT: Call the backend endpoint to properly resolve the game and set winner in database
  const gameId = completionData.gameId;
  if (gameId) {
    // Call the backend endpoint to finish the game
    fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/ranked?gameId=${gameId}`, {
      method: 'POST',
      credentials: 'include'
    })
    .then(response => {
      if (response.ok) {
        return response.json();
      } else {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
    })
    .then(gameResult => {
      // Store the game result with ELO changes
      store.commit('rankedGame/RG_SET_RESULT', gameResult);
    })
    .catch(error => {
      console.error('❌ Failed to resolve game via backend:', error);
    })
    .finally(() => {
      // Handle cleanup after getting the result data
      const currentUser = store.getters['user/getCurrentUser'];
      if (currentUser?.id) {
        store.commit('rankedGame/RG_SET_STATUS', {playerId: currentUser.id, status: 'ended'});
      }

      // Reset game state and disconnect WebSocket
      store.commit('gameInfo/RESET_GAME', null, {root: true});
      store.commit('round/RESET_ROUND', null, {root: true});
      disconnectFromRankedGame();
    });
  } else {
    console.error('❌ No game ID in completion data, cannot resolve game');

    // Handle cleanup even if we can't resolve the game
    const currentUser = store.getters['user/getCurrentUser'];
    if (currentUser?.id) {
      store.commit('rankedGame/RG_SET_STATUS', {playerId: currentUser.id, status: 'ended'});
    }

    store.commit('gameInfo/RESET_GAME', null, {root: true});
    store.commit('round/RESET_ROUND', null, {root: true});
    store.commit('guess/RESET_GUESS', null);
    disconnectFromRankedGame();
  }
}


function startHeartbeat(gameId: any) {
  if (heartbeatInterval) clearInterval(heartbeatInterval);

  heartbeatInterval = window.setInterval(() => {
    if (stompClient && stompClient.connected) {
      stompClient.send(
        "/app/ranked-game/heartbeat",
        {},
        JSON.stringify({ gameId: gameId })
      );
    }
  }, 30000); // every 30 seconds
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
