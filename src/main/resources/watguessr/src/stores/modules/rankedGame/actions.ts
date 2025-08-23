// src/stores/modules/game/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { RankedGameState } from './state';
import {
  connectToRankedGame,
  disconnectFromRankedGame,
  sendPlayerCompleted,
  sendPlayerProgress,
  sendPlayerReady
} from '../../../services/rankedGameWebSocket';
import rankedGame from "@/stores/modules/rankedGame/index.ts";

export const actions: ActionTree<RankedGameState, RootState> = {
  async rankedGame_createRankedGame({ state, commit, dispatch , rootGetters}) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;
    const username = currentUser?.username || 'Player';

    // Reset the game and initialize the current player
    commit('RG_RESET_GAME', { userId, username });
    commit('gameInfo/RESET_GAME', null, {root: true});

    // Now we can safely set the status since the player exists
    commit('RG_SET_STATUS', { playerId: userId, status: 'loading' });
    commit('gameInfo/SET_GAME_MODE', 'ranked', {root: true});
    commit('gameInfo/SET_CURRENT_VIEW', 'Image', {root: true});

    // Store the current user's pre-game ELO
    if (currentUser?.elo) {
      commit('RG_SET_PRE_GAME_ELOS', { [username]: currentUser.elo });
    }
  },

  // Store opponent pre-game ELOs (called from Lobby when match is found)
  rankedGame_storeOpponentElos({ commit, rootGetters }, { opponentName, opponentElo }) {

    // Get current pre-game ELOs and add the opponent's ELO
    const currentPreGameElos = rootGetters['rankedGame/rankedGame_getPreGameElos'] || {};
    const updatedPreGameElos = {
      ...currentPreGameElos,
      [opponentName]: opponentElo
    };

    commit('RG_SET_PRE_GAME_ELOS', updatedPreGameElos);
  },
  async rankedGame_restartGame({ state, commit, dispatch }) {
    await dispatch('rankedGame_createRankedGame');
  },
  rankedGame_endCurrentRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    // Ensure player exists in the game state
    if (!state.rankedGame_players[userId]) {
      console.warn('⚠️ Player not found in ranked game state, initializing...');
      commit('RG_SET_PLAYERS', {
        ...state.rankedGame_players,
        [userId]: { score: 0, status: 'playing', username: 'Player' }
      });
    }

    commit('RG_IMPLEMENT_ROUND_RESULT', { playerId: userId, roundResult: payload.roundResult });

    // Send progress update via WebSocket
    const currentScore = state.rankedGame_players[userId]?.score || 0;
    sendPlayerProgress(state.rankedGame_gameId, userId, currentScore, 'ended');

    // Always go to RoundEnd view after submitting a guess in ranked game
    commit('gameInfo/SET_CURRENT_VIEW', 'RoundEnd', {root: true});
  },

  async rankedGame_endGame({ state, commit, rootGetters }) {
    try {
      const currentUser = rootGetters['user/getCurrentUser'];
      const userId = currentUser?.id;

      // Check if we already have result data (from WebSocket)
      const existingResult = state.rankedGame_result;
      if (existingResult && Object.keys(existingResult.eloChanges || {}).length > 0) {
        return;
      }

      // Only call backend if we don't have results from WebSocket
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/ranked?gameId=${state.rankedGame_gameId}`, {
        method: 'POST',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const rankedGameResult = await response.json();

      commit('RG_SET_RESULT', rankedGameResult);
      commit('RG_SET_STATUS', {playerId: userId, status: 'ended'});

      // Don't reset game or disconnect WebSocket here - let the WebSocket handle it
      // This prevents duplicate calls and ensures proper cleanup
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }
  },

  // Send player ready status
  rankedGame_setPlayerReady({ state, rootGetters }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    if (userId && state.rankedGame_gameId) {
      sendPlayerReady(state.rankedGame_gameId, userId);
    }
  },

  // Send player completed status
  rankedGame_setPlayerCompleted({ state, rootGetters }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;
    if (userId && state.rankedGame_gameId) {
      sendPlayerCompleted(state.rankedGame_gameId, userId);
    } else {
      console.warn('⚠️ Cannot send player completed: userId or gameId missing', { userId, gameId: state.rankedGame_gameId });
    }
  },

  // Load final game data from localStorage
  rankedGame_loadFinalGameData({ commit }) {
    commit('RG_LOAD_FINAL_GAME_DATA');
  },

  // Clear final game data
  rankedGame_clearFinalGameData({ commit }) {
    commit('RG_CLEAR_FINAL_GAME_DATA');
  },

  // Send player status update
  rankedGame_updatePlayerStatus({ state, rootGetters }, { status }: { status: string }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    if (userId && state.rankedGame_gameId) {
      const currentScore = state.rankedGame_players[userId]?.score || 0;
      sendPlayerProgress(state.rankedGame_gameId, userId, currentScore, status);
    }
  },

  // Disconnect from WebSocket when leaving
  rankedGame_disconnect() {
    disconnectFromRankedGame();
  },

  // Handle player disconnection
  rankedGame_handlePlayerDisconnection({ commit }, playerId: string) {
    commit('RG_SET_PLAYER_DISCONNECTED', playerId);
  }
};
