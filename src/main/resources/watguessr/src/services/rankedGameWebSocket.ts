import SockJS from 'sockjs-client';
import Stomp, { Client } from 'stompjs';
import store from '../stores';

// STOMP client for multiplayer game state updates
let stompClient: Client | null = null;
let heartbeatInterval: number | null = null;
let hasLeftGame = false; // module/global or component state
let cleanupCalled = false;
let unwatchRoute: (() => void) | null = null;

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
  hasLeftGame = false; // module/global or component state
  cleanupCalled = false;

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

    // Setup immediate leaving functionality
    setupImmediateLeaving();

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
    // Get current user and game ID for leave message
    const currentUser = store.getters['user/getCurrentUser'];
    const gameId = store.getters['rankedGame/rankedGame_getGameId'];
    
    // Send leave message before disconnecting
    if (currentUser?.id && gameId) {
      console.log('Sending leave message before disconnecting from ranked game');
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
      if (stompClient) {
        stompClient.disconnect(() => {
          store.commit('guess/RESET_GUESS', null);
        });
        stompClient = null;
      }
    }
  }
}

// Setup immediate leaving functionality
function setupImmediateLeaving() {
  console.log('Setting up immediate leaving for ranked game...');
  
  // Add beforeunload event listener for immediate leaving
  window.addEventListener('beforeunload', handleBeforeUnload);

  // Store initial path to detect route changes
  (window as any).__rankedInitialPath = window.location.pathname;
  
  // Watch for route changes to leave game if navigating away
  if (typeof window !== 'undefined' && window.location) {
    // Use a more reliable route change detection
    const checkRouteChange = () => {
      const currentPath = window.location.pathname;
      const initialPath = (window as any).__rankedInitialPath;
      
      // Only log occasionally to avoid spam
      if (Math.random() < 0.1) { // 10% chance to log
        console.log('Route check:', { currentPath, initialPath });
      }
      
      // Check if we've left the game routes
      if (!currentPath.includes('play') && !currentPath.includes('game-end')) {
        // We've left the game routes, cleanup
        console.log('Left game routes, triggering cleanup...', { currentPath });
        handleImmediateLeave();
        return; // Stop checking once we've left
      }
    };

    // Check route changes periodically (since we don't have Vue router access here)
    const routeCheckInterval = setInterval(checkRouteChange, 2000); // Check every 2 seconds instead of 1
    
    // Store the interval for cleanup
    (window as any).__rankedRouteCheckInterval = routeCheckInterval;
  }
}

// Cleanup immediate leaving functionality
function cleanupImmediateLeaving() {
  console.log('Cleaning up immediate leaving functionality...');
  
  // Remove beforeunload event listener
  window.removeEventListener('beforeunload', handleBeforeUnload);

  // Clear route check interval
  if ((window as any).__rankedRouteCheckInterval) {
    clearInterval((window as any).__rankedRouteCheckInterval);
    delete (window as any).__rankedRouteCheckInterval;
  }

  // Clean up stored path
  delete (window as any).__rankedInitialPath;

  // Cleanup route watcher if exists
  if (unwatchRoute) {
    unwatchRoute();
    unwatchRoute = null;
  }
}

// Handle beforeunload event
function handleBeforeUnload() {
  console.log('Beforeunload event triggered');
  if (!cleanupCalled) {
    console.log('Page unloading - disconnecting from ranked game');
    // Don't call disconnectFromRankedGame here to avoid recursion
    // Just stop heartbeat and disconnect STOMP client directly
    if (stompClient && stompClient.connected) {
      stopHeartbeat();
      stompClient.disconnect(() => {
        console.log('STOMP client disconnected in beforeunload');
      });
      stompClient = null;
    }
    cleanupCalled = true;
  }
}

// Handle immediate leave (called when route changes or component unmounts)
function handleImmediateLeave() {
  console.log('Immediate leave triggered');
  if (!cleanupCalled) {
    console.log('Route changed - disconnecting from ranked game');
    disconnectFromRankedGame();
  }
}

// Public method for components to call when they unmount
export function cleanupRankedGameOnUnmount() {
  console.log('Component unmount cleanup called');
  handleImmediateLeave();
}

// Manual disconnect for testing (can be called from browser console)
export function manualDisconnectRankedGame() {
  console.log('Manual disconnect called');
  disconnectFromRankedGame();
}

// Check connection status for debugging
export function getRankedConnectionStatus() {
  return {
    hasStompClient: !!stompClient,
    isConnected: stompClient?.connected,
    cleanupCalled,
    hasRouteCheckInterval: !!(window as any).__rankedRouteCheckInterval,
    currentPath: window.location.pathname
  };
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
  hasLeftGame = false;
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
  hasLeftGame = false;
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

  console.log('Sending leave message for ranked game:', leaveData);
  stompClient.send('/app/ranked-game/leave', {}, JSON.stringify(leaveData));
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
  if (!hasLeftGame && playerIds.length === 1 && playerIds[0] === currentUser.id) {
    hasLeftGame = true; // prevent re-trigger
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
  hasLeftGame = false;
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
