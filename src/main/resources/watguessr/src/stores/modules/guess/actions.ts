// src/stores/modules/guess/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { GuessState } from './state';

export const actions: ActionTree<GuessState, RootState> = {
  async submitGuess({ state, rootState, rootGetters, dispatch }) {
    // set user id from Vuex user module
    const currentUser = rootGetters['user/getCurrentUser'];
    const currentUserId = currentUser?.id || null;
    state.user.id = currentUserId;

    // check if we have a valid roundId
    const roundId = rootState.round.roundId;
    if (!roundId) {
      console.error('Cannot submit guess: No roundId found in store');
      throw new Error('Round ID is required to submit a guess');
    }

    try {
      const baseUrl = import.meta.env.VITE_API_BASE_URL;
      const createGuessBody = {
        userId: state.user.id,
        time: state.time,
        guessX: state.guessX,
        guessY: state.guessY,
        building: state.building,
        floor: state.floor,
        roundId: roundId
      };

      const response = await fetch(`${baseUrl}/api/guess`, {
        method: 'POST',
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(createGuessBody)
      });

      if (!response.ok) {
        throw new Error('Failed to submit guess');
      }

      const roundResult = await response.json();
      dispatch('round/endRound', { winner: state.user, roundResult: roundResult }, { root: true });

      return roundResult;
    } catch (error) {
      console.error('Error submitting guess:');
      return null;
    }
  },

};
