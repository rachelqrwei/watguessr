// src/stores/modules/guess/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { GuessState } from './state';

export const actions: ActionTree<GuessState, RootState> = {
  async submitGuess({ rootState, dispatch }, guess: any) {
    console.log("guess submitted");

    //answer scene of the round
    const scene = rootState.round.scene;
    if (!scene) return;

    //calculate points from the round
    try {
      const response = await fetch(
        `http://localhost:5173/api/guess/calculate-points?roundId=${rootState.round.roundId}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(guess),
        }
      );

      if (!response.ok) {
        throw new Error('Failed to calculate points');
      }

      const points = await response.json();

      //end round
      dispatch('round/endRound', { winner: guess.user, score: points }, { root: true });

      return points;
    } catch (error) {
      console.error('Error calculating points:', error);
      return null;
    }
  },
};
