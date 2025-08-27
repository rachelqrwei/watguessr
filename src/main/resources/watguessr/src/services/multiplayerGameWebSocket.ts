import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';
import store from '../stores';

// STOMP client for multiplayer game state updates
let stompClient: Client | null = null;
let heartbeatInterval: number | null = null;
let hasLeftGame = false; // module/global or component state

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
  hasLeftGame = false; // module/global or component state

  stompClient.connect({}, () => {
    startHeartbeat(gameId);

    // Subscribe to game state updates for this specific game
    const stateSubscription = stompClient?.subscribe(`/topic/multiplayer-game/${gameId}/state`, (message) => {
      const gameState: MultiplayerGameStateDto = JSON.parse(message.body);
      handleGameStateUpdate(gameState);
    });

    // Subscribe to round start events
    const roundStartSubscription = stompClient?.subscribe(`/topic/multiplayer-game/${gameId}/round-start`, (message) => {
      const roundData = JSON.parse(message.body);
      handleRoundStart(roundData);
    });

    // Subscribe to game completion events
    const completionSubscription = stompClient?.subscribe(`/topic/multiplayer-game/${gameId}/complete`, (message) => {
      const completionData = JSON.parse(message.body);
      handleGameComplete(completionData);
    });

    // Request current round state to catch up on missed events
    requestCurrentRoundState(gameId);

  }, (error: any) => {
    console.error('WebSocket connection error:', error);

    // Stop heartbeat on disconnect/error
    stopHeartbeat();

    // Clean up existing connection before retry
    if (stompClient) {
      stompClient.disconnect();
      stompClient = null;
    }

    // Retry connection after 3 seconds
    setTimeout(() => {
      connectToMultiplayerGame(gameId);
    }, 3000);
  });
}

export function disconnectFromMultiplayerGame() {
  if (stompClient && stompClient.connected) {
    // Get current user and game ID for leave message
    const currentUser = store.getters['user/getCurrentUser'];
    const gameId = store.getters['multiplayerGame/multiplayerGame_getGameId'];
    
    // Send leave message before disconnecting
    if (currentUser?.id && gameId) {
      console.log('Sending leave message before disconnecting from multiplayer game');
      sendLeaveGame(gameId, currentUser.id);
      
      // Give a small delay for the leave message to be sent
      setTimeout(() => {
        stopHeartbeat();
        if (stompClient) {
          stompClient.disconnect(() => {
            store.commit('guess/RESET_GUESS', null);
          });
          stompClient = null;
        }
      }, 100);
    } else {
      // Fallback if we don't have user or game info
      stopHeartbeat();
      stompClient.disconnect(() => {
        store.commit('guess/RESET_GUESS', null);
      });
      stompClient = null;
    }
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

  stompClient.send('/app/multiplayer-game/update-progress', {}, JSON.stringify(progressData));
}

// Send player ready status
export function sendPlayerReady(gameId: string, userId: string) {
  hasLeftGame = false;
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot send ready status');
    return;
  }

  const readyData = {
    gameId: gameId,
    userId: userId
  };

  stompClient.send('/app/multiplayer-game/ready', {}, JSON.stringify(readyData));
}

// Send player ready status
export function sendPlayerCompleted(gameId: string, userId: string) {
  hasLeftGame = false;
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot send completed');
    return;
  }

  const completedData = {
    gameId: gameId,
    userId: userId
  };

  stompClient.send('/app/multiplayer-game/completed', {}, JSON.stringify(completedData));
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

  stompClient.send('/app/multiplayer-game/start-round', {}, JSON.stringify(startData));
}

// Send leave game request
export function sendLeaveGame(gameId: string, userId: string) {
  if (!stompClient || !stompClient.connected) {
    console.warn('WebSocket not connected, cannot send leave message');
    return;
  }

  const leaveData = {
    gameId: gameId,
    userId: userId
  };

  console.log('Sending leave message for multiplayer game:', leaveData);
  stompClient.send('/app/multiplayer-game/leave', {}, JSON.stringify(leaveData));
}

// Handle incoming game state updates
function handleGameStateUpdate(gameState: MultiplayerGameStateDto) {
  // Don't update store if we're on a game end route to prevent interference
  if (window.location.pathname.includes('-game-end')) {
    return;
  }

  // Get current players from store to detect disconnections
  const currentUser = store.getters['user/getCurrentUser'] || {};
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

  // ✅ If only one player left and it's me, redirect without finishing the game
  const playerIds = Object.keys(players);
  if (!hasLeftGame && playerIds.length === 1 && playerIds[0] === currentUser.id) {
    hasLeftGame = true; // prevent re-trigger
    alert("⚠️ I'm the only one left, leaving game...");
    window.location.href = "/";
  }

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

  // Emit custom event for round-end components to listen to
  // Only emit minimal data needed for round-end updates
  const roundEndData = {
    gameId: gameState.gameId,
    currentRound: gameState.currentRound,
    playersWithEndedStatus: Object.keys(gameState.players).filter(
      playerId => gameState.players[playerId].status === 'ended'
    ).length
  };
  
  const gameStateEvent = new CustomEvent('multiplayerGameStateUpdate', {
    detail: roundEndData
  });
  window.dispatchEvent(gameStateEvent);
}

// Handle round start events
function handleRoundStart(roundData: any) {
  hasLeftGame = false;
  // Start a new round in the frontend
  if (roundData.roundId) {
    // IMPORTANT: Reset round state to clear previous round data (including correct answer)
    store.commit('round/RESET_ROUND', null);

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

  // Don't reset countdown state here - it should be reset after the countdown completes
  // The countdown will be shown when all players are ready, and then reset after completion
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

          // Update multiplayer game round number (use array length as round number)
          store.commit('multiplayerGame/MG_SET_CURRENT_ROUND', roundsData.length);

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
function handleGameComplete(completionData: MultiplayerGameStateDto) {
  // Update the game state with final results
  if (completionData.finalWinner) {
    store.commit('multiplayerGame/MG_SET_FINAL_WINNER', completionData.finalWinner);
  }

  if (completionData.shouldEnd) {
    store.commit('multiplayerGame/MG_SET_SHOULD_END', true);
  }

  store.commit('guess/RESET_GUESS', null);

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

  // Persist winner in the backend for multiplayer
  const gameId = completionData.gameId;
  if (gameId) {
    fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/multiplayer?gameId=${gameId}`, {
      method: 'POST',
      credentials: 'include'
    })
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(() => {
        const currentUser = store.getters['user/getCurrentUser'];
        if (currentUser?.id) {
          store.commit('multiplayerGame/MG_SET_STATUS', { playerId: currentUser.id, status: 'ended' });
        }
      })
      .catch(error => {
        console.error('❌ Failed to resolve multiplayer game via backend:', error);
      });
  }
}

function startHeartbeat(gameId: any) {
  if (heartbeatInterval) clearInterval(heartbeatInterval);

  heartbeatInterval = window.setInterval(() => {
    if (stompClient && stompClient.connected) {
      stompClient.send(
        "/app/multiplayer-game/heartbeat",
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
