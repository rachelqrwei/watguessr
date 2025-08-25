// src/stores/modules/guess/getters.ts
import type { RootState } from '../../index';
import type { GuessState } from './state';

export const getters = {
  getGuessUser: (state: GuessState) => state.user,
  getGuessTime: (state: GuessState) => state.time,
  getGuessBuilding: (state: GuessState) => state.building,
  getGuessX: (state: GuessState) => state.guessX,
  getGuessY: (state: GuessState) => state.guessY,
  getGuessFloor: (state: GuessState) => state.floor,
  getIsSubmitting: (state: GuessState) => state.isSubmitting,
  getHasSubmitted: (state: GuessState) => state.hasSubmitted,
};
