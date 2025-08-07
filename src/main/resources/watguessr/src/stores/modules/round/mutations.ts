// src/stores/modules/round/mutations.ts
import type { MutationTree } from 'vuex';
import type { RoundState, Scene } from './state';

export const mutations: MutationTree<RoundState> = {
  SET_ROUND_ID(state, roundId: string) {
    state.roundId = roundId;
  },
  SET_SCENE(state, scene: Scene) {
    state.scene = scene;
  },
  SET_WINNER(state, winner: string) {
    state.winner = winner;
  },
  SET_SCORE_CHANGE_FROM_ROUND(state, score: number) {
    state.scoreChange = score;
  },
  RESET_ROUND(state) {
    state.roundId = null;
    state.scene = null;
    state.winner = null;
    state.scoreChange = null;
  },
};
