// src/stores/modules/round/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { RoundState } from './state';

export const actions: ActionTree<RoundState, RootState> = {
  async startRound({ commit }, { gameId }): Promise<RoundState> {
    //reset data from prev round (if any)
    commit('guess/RESET_GUESS', null, {root: true});
    commit('RESET_ROUND');

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

    console.log("Started round", round?.id);

    return round;
  },
  endRound({ commit, dispatch }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    //set winner of the round
    commit('SET_WINNER', payload.winner);

    //set round result from round (round-specific score)
    commit('SET_ROUND_RESULT_FROM_ROUND', payload.roundResult);

    //end this round in the game store
    dispatch('game/endCurrentRound', { winner: payload.winner, roundResult: payload.roundResult }, { root: true });
  },
};
