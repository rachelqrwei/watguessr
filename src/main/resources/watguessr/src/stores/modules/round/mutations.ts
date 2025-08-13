// src/stores/modules/round/mutations.ts
import type { MutationTree } from 'vuex';
import type { RoundState } from './state';

export const mutations: MutationTree<RoundState> = {
  SET_ROUND_ID(state, roundId: string) {
    state.roundId = roundId;
  },
  SET_IMAGE_URL(state, imageUrl: string | null) {
    state.imageUrl = imageUrl;
  },
  SET_WINNER(state, winner: string) {
    state.winner = winner;
  },
  SET_ROUND_RESULT_FROM_ROUND(state, roundResult: {points: number, distance: number}) {
    state.roundResult = {
      points: roundResult.points,
      distance: roundResult.distance
    };
  },
  RESET_ROUND(state) {
    state.roundId = null;
    state.imageUrl = null;
    state.winner = null;
    state.roundResult = {
      points: 0,
      distance: 0
    };
  },
};
