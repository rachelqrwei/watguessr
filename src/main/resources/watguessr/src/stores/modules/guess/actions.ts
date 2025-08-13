// src/stores/modules/guess/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { GuessState } from './state';

export const actions: ActionTree<GuessState, RootState> = {
  async submitGuess({ state, rootState, rootGetters, dispatch }) {
    // set user id from Vuex user module
    const currentUser = rootGetters['user/currentUser'];
    const currentUserId = currentUser?.id || null;
    state.user.id = currentUserId;

    //calculate points from the round
    try {
      const baseUrl = import.meta.env.VITE_API_BASE_URL;
      const createGuessBody = {
        userId: state.user.id,
        time: state.time,
        guessX: state.guessX,
        guessY: state.guessY,
        building: state.building,
        floor: state.floor,
        roundId: rootState.round.roundId
      };

      const createResponse = await fetch(`${baseUrl}/api/guess`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(createGuessBody)
      });

      if (!createResponse.ok) {
        throw new Error('Failed to create guess');
      }

      const response = await fetch(
        `${baseUrl}/api/guess/evaluate-guess?roundId=${rootState.round.roundId}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(state),
        }
      );

      if (!response.ok) {
        throw new Error('Failed to calculate points');
      }

      const roundResult = await response.json();
      dispatch('round/endRound', { winner: state.user, roundResult: roundResult }, { root: true });

      return roundResult;
    } catch (error) {
      console.error('Error calculating points:', error);
      return null;
    }
  },
};
