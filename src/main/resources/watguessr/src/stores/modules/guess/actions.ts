// src/stores/modules/guess/actions.ts
import type { RootState } from '../../index';
import type { GuessState } from './state';

export const actions = {
  async submitGuess({ state, rootState, rootGetters, dispatch, commit }: {
    state: GuessState;
    rootState: RootState;
    rootGetters: any;
    dispatch: any;
    commit: any;
  }) {
    // Check if user is already submitting or has already submitted
    if (state.isSubmitting) {
      console.log('Guess submission already in progress');
      return null;
    }

    if (state.hasSubmitted) {
      console.log('Guess has already been submitted for this round');
      return null;
    }

    // Set submitting flag to prevent duplicate submissions
    commit('SET_IS_SUBMITTING', true);

    try {
      // set user id from Vuex user module
      const currentUser = rootGetters['user/getCurrentUser'];
      const currentUserId = currentUser?.id || null;
      state.user.id = currentUserId;

    // Check if we have a valid roundId
    const roundId = rootState.round.roundId;
    if (!roundId) {
      console.error('Cannot submit guess: No roundId found in store');
      throw new Error('Round ID is required to submit a guess');
    }

      //calculate points from the round
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
      const token = rootGetters['user/getToken'];
      const createResponse = await fetch(`${baseUrl}/api/guess`, {
        method: 'POST',
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(createGuessBody)
      });

      if (!createResponse.ok) {
        throw new Error('Failed to create guess');
      }

      const response = await fetch(
        `${baseUrl}/api/guess/evaluate-guess?roundId=${rootState.round.roundId}`,
        {
          method: 'POST',
          credentials: "include",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(state),
        }
      );

      if (!response.ok) {
        throw new Error('Failed to calculate points');
      }

      const roundResult = await response.json();

      // Mark as submitted before ending the round
      commit('SET_HAS_SUBMITTED', true);

      dispatch('round/endRound', { winner: state.user, roundResult: roundResult }, { root: true });

      return roundResult;
    } catch (error) {
      console.error('Error calculating points:', error);
      return null;
    } finally {
      // Always reset the submitting flag, regardless of success or failure
      commit('SET_IS_SUBMITTING', false);
    }
  },

};
