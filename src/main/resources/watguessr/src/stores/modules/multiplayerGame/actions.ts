// src/stores/modules/game/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { MultiplayerGameState } from './state';
import {
  connectToMultiplayerGame,
  disconnectFromMultiplayerGame,
  sendPlayerCompleted,
  sendPlayerProgress,
  sendPlayerReady
} from '../../../services/multiplayerGameWebSocket';

export const actions: ActionTree<MultiplayerGameState, RootState> = {
  async multiplayerGame_createMultiplayerGame({ state, commit, dispatch , rootGetters}) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;
    const username = currentUser?.username || 'Player';

    // Reset the game and initialize the current player
    commit('MG_RESET_GAME', { userId, username });
    commit('gameInfo/RESET_GAME', null, {root: true});

    // Now we can safely set the status since the player exists
    commit('MG_SET_STATUS', { playerId: userId, status: 'loading' });
    commit('gameInfo/SET_GAME_MODE', 'multiplayer', {root: true});
    commit('gameInfo/SET_CURRENT_VIEW', 'Map', {root: true});

    commit('MG_SET_STATUS', { playerId: userId, status: 'loading' });
  },
  async multiplayerGame_restartGame({ state, commit, dispatch }) {
    await dispatch('multiplayerGame_createMultiplayerGame');
  },
  multiplayerGame_endCurrentRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    // Ensure player exists in the game state
    if (!state.multiplayerGame_players[userId]) {
      console.warn('⚠️ Player not found in multiplayer game state, initializing...');
      commit('MG_SET_PLAYERS', {
        ...state.multiplayerGame_players,
        [userId]: { score: 0, status: 'playing', username: 'Player' }
      });
    }

    commit('MG_IMPLEMENT_ROUND_RESULT', { playerId: userId, roundResult: payload.roundResult });

    // Send progress update via WebSocket
    const currentScore = state.multiplayerGame_players[userId]?.score || 0;
    console.log('📊 Updated score for player:', userId, 'new score:', currentScore);
    sendPlayerProgress(state.multiplayerGame_gameId, userId, currentScore, 'ended');

    // Always go to RoundEnd view after submitting a guess in multiplayer
    commit('gameInfo/SET_CURRENT_VIEW', 'RoundEnd', {root: true});
  },

  async multiplayerGame_endGame({ state, commit, rootGetters }) {
    try {
      const currentUser = rootGetters['user/getCurrentUser'];
      const userId = currentUser?.id;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/multiplayer?gameId=${state.multiplayerGame_gameId}&userId=${userId}`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      await response.json();
      commit('MG_SET_STATUS', {playerId: userId, status: 'ended'});
      commit('MG_RESET_GAME');
      commit('gameInfo/RESET_GAME', null, {root: true});
      commit('round/RESET_ROUND', null, {root: true});

      // Disconnect from WebSocket
      disconnectFromMultiplayerGame();
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }
  },

  // Send player ready status
  multiplayerGame_setPlayerReady({ state, rootGetters }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    if (userId && state.multiplayerGame_gameId) {
      sendPlayerReady(state.multiplayerGame_gameId, userId);
    }
  },

  // Send player completed status
  multiplayerGame_setPlayerCompleted({ state, rootGetters }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    console.log('🎮 Setting player completed status:', { userId, gameId: state.multiplayerGame_gameId });

    if (userId && state.multiplayerGame_gameId) {
      console.log('📤 Sending player completed via WebSocket');
      sendPlayerCompleted(state.multiplayerGame_gameId, userId);
    } else {
      console.warn('⚠️ Cannot send player completed: userId or gameId missing', { userId, gameId: state.multiplayerGame_gameId });
    }
  },

  // Load final game data from localStorage
  multiplayerGame_loadFinalGameData({ commit }) {
    commit('MG_LOAD_FINAL_GAME_DATA');
  },

  // Clear final game data
  multiplayerGame_clearFinalGameData({ commit }) {
    commit('MG_CLEAR_FINAL_GAME_DATA');
  },

  // Send player status update
  multiplayerGame_updatePlayerStatus({ state, rootGetters }, { status }: { status: string }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    if (userId && state.multiplayerGame_gameId) {
      const currentScore = state.multiplayerGame_players[userId]?.score || 0;
      sendPlayerProgress(state.multiplayerGame_gameId, userId, currentScore, status);
    }
  },

  // Disconnect from WebSocket when leaving
  multiplayerGame_disconnect() {
    disconnectFromMultiplayerGame();
  },

  // Handle player disconnection (called when another player disconnects)
  multiplayerGame_handlePlayerDisconnection({ state, commit, dispatch }, playerId: string) {
    console.log('🔌 Handling player disconnection:', playerId);
    
    // Mark the player as disconnected
    commit('MG_SET_PLAYER_DISCONNECTED', playerId);
    
    // Check if game should be abandoned (less than 2 active players)
    const activePlayerCount = Object.values(state.multiplayerGame_players)
      .filter(player => player.status !== 'disconnected').length;
    
    if (activePlayerCount < 2) {
      console.log('🚨 Game abandoned - not enough players');
      commit('MG_SET_GAME_ABANDONED');
      
      // Redirect to home after a delay
      setTimeout(() => {
        window.location.href = '/';
      }, 5000);
    }
  },

  // Start disconnection monitoring for the current player
  multiplayerGame_startDisconnectionMonitoring({ state, commit, rootGetters, dispatch }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;
    
    if (!userId || !state.multiplayerGame_gameId) return;
    
    // Set up page visibility change handler
    const handleVisibilityChange = () => {
      if (document.hidden) {
        console.log('📱 Page hidden - player may have switched tabs/apps');
        // Send a "away" status to indicate player is not actively playing
        dispatch('multiplayerGame_updatePlayerStatus', { status: 'away' });
      } else {
        console.log('📱 Page visible - player returned');
        // Send "playing" status when player returns
        dispatch('multiplayerGame_updatePlayerStatus', { status: 'playing' });
      }
    };
    
    // Set up beforeunload handler
    const handleBeforeUnload = () => {
      console.log('🚪 Page unloading - player leaving');
      // Send disconnect status
      dispatch('multiplayerGame_updatePlayerStatus', { status: 'disconnected' });
    };
    
    // Add event listeners
    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('beforeunload', handleBeforeUnload);
    
    // Store references for cleanup
    state._disconnectionHandlers = {
      visibilityChange: handleVisibilityChange,
      beforeUnload: handleBeforeUnload
    };
  },

  // Stop disconnection monitoring
  multiplayerGame_stopDisconnectionMonitoring({ state }) {
    if (state._disconnectionHandlers) {
      document.removeEventListener('visibilitychange', state._disconnectionHandlers.visibilityChange);
      window.removeEventListener('beforeunload', state._disconnectionHandlers.beforeUnload);
      state._disconnectionHandlers = undefined;
    }
  },

  // Handle game abandonment when not enough players remain
  multiplayerGame_handleGameAbandoned({ commit, dispatch }) {
    console.log('🚨 Game abandoned - redirecting to home');
    
    // Reset the game state
    commit('MG_RESET_GAME');
    commit('gameInfo/RESET_GAME', null, {root: true});
    commit('round/RESET_ROUND', null, {root: true});
    
    // Disconnect from WebSocket
    dispatch('multiplayerGame_disconnect');
    
    // Redirect to home page
    window.location.href = '/';
  }
};
