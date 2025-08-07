// src/stores/modules/round/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { RoundState } from './state';

export const actions: ActionTree<RoundState, RootState> = {
  setWinner({ commit, dispatch }, payload: { winner: string; score: number }) {
    commit('SET_WINNER', payload.winner);
    commit('SET_SCORE_CHANGE', payload.score);

    // TODO: send API call to set winner of the round
    dispatch('game/recordRoundWinner', { winner: payload.winner, score: payload.score }, { root: true });
  },

  async startRound({ commit }, { gameId }): Promise<RoundState> {
    if (!gameId) throw new Error('Game ID not found');

    const response = await fetch(`http://localhost:5173/api/round/create?gameId=${gameId}`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error('Failed to create round');
    }

    const round: any = await response.json();
    commit('SET_SCENE', round?.scene);
    commit('SET_ROUND_ID', round?.id);

    return round;
  },

  resetRound({ commit }) {
    commit('RESET_ROUND');
  },
};
