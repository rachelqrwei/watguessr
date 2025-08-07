// src/stores/modules/game/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { GameState } from './state';

export const actions: ActionTree<GameState, RootState> = {
  async createSingleplayerGame({ commit, dispatch }) {
    commit('RESET_GAME');
    commit('SET_STATUS', 'loading');
    commit('SET_GAME_MODE', 'Singleplayer');

    const response = await fetch('http://localhost:5173/api/game/create/singleplayer');
    const gameId = await response.json();

    commit('SET_GAME_ID', gameId);
    dispatch('round/startRound', { gameId }, { root: true });
    commit('SET_STATUS', 'playing');
  },

  recordRoundWinner({ state, commit, dispatch }, payload: { username: string; score: number }) {
    commit('ADD_SCORE', payload);
    commit('CHANGE_VIEW', 'RoundEnd');

    if (state.currentRound >= state.maxRounds) {
      const winner = Object.entries(state.scores).sort((a, b) => b[1] - a[1])[0][0];
      // TODO: send API call to set final winner
      commit('SET_FINAL_WINNER', winner);
    } else {
      dispatch('round/resetRound', null, { root: true });
      const newScene = generateNextScene(); // Replace with actual logic
      dispatch('round/startRound', newScene, { root: true });
      commit('INCREMENT_ROUND');
    }
  },

  async endGame({ commit }, gameId: string) {
    commit('RESET_GAME');
    try {
      const response = await fetch(`http://localhost:5173/api/game/finish/singleplayer?gameId=${gameId}`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }
  }
};

// Dummy placeholder
function generateNextScene() {
  // TODO: replace with real logic
  return {};
}
