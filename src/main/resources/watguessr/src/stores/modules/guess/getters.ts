// src/stores/modules/guess/getters.ts
import type { GetterTree } from 'vuex';
import type { RootState } from '../../index';
import type { GuessState } from './state';

export const getters: GetterTree<GuessState, RootState> = {
  getGuessTime: (state) => state.time,
  getGuessBuilding: (state) => state.building,
  getGuessX: state => state.guessX,
  getGuessY: state => state.guessY,
  getGuessFloor: state => state.floor,
  getUserId: state => state.user?.id || null,
};
