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
    commit('gameInfo/SET_CURRENT_VIEW', 'Image', {root: true});

    commit('MG_SET_STATUS', { playerId: userId, status: 'loading' });
  },
  async multiplayerGame_restartGame({ state, commit, dispatch }) {
    await dispatch('multiplayerGame_createMultiplayerGame');
  },
  async multiplayerGame_endCurrentRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
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

    // fetch correct answer immediately after user submits guess
    // this ensures the RoundEnd view has the correct answer for proper map bounds
    await dispatch('round/fetchCorrectAnswer', null, { root: true });

    // Send progress update via WebSocket
    const currentScore = state.multiplayerGame_players[userId]?.score || 0;
    sendPlayerProgress(state.multiplayerGame_gameId, userId, currentScore, 'ended');

    // Always go to RoundEnd view after submitting a guess in multiplayer
    commit('gameInfo/SET_CURRENT_VIEW', 'RoundEnd', {root: true});
  },

  async multiplayerGame_endGame({ state, commit, rootGetters }) {
    try {
      const currentUser = rootGetters['user/getCurrentUser'];
      const userId = currentUser?.id;
      const token = rootGetters['user/getToken'];

      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/multiplayer?gameId=${state.multiplayerGame_gameId}`, {
        method: 'POST',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      await response.json();
      commit('MG_SET_STATUS', {playerId: userId, status: 'ended'});
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
    if (userId && state.multiplayerGame_gameId) {
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
  }
};
