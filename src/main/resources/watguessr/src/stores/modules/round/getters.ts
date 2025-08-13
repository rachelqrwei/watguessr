// src/stores/modules/round/getters.ts
import type { GetterTree } from 'vuex';
import type { RootState } from '../../index';
import type { RoundState } from './state';

export const getters: GetterTree<RoundState, RootState> = {
  getRoundId: (state) => state.roundId,
  getWinner: (state) => state.winner,
  getRoundResult: (state) => state.roundResult,
  getImageUrl: (state) => state.imageUrl,
};
