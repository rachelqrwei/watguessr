// src/stores/modules/guess/mutations.ts
import type { GuessState } from './state';

export const mutations = {
  SET_TIME(state: GuessState, time: number) {
    state.time = time;
  },
  SET_BUILDING_AND_LOCATIONS(state: GuessState, payload: {building: string, guessX: number, guessY: number}) {
    state.building = payload.building;
    state.guessX = payload.guessX;
    state.guessY = payload.guessY;
  },
  SET_BUILDING(state: GuessState, building: string) {
    state.building = building;
  },
  SET_FLOOR(state: GuessState, floor: string) {
    state.floor = floor;
  },
  SET_IS_SUBMITTING(state: GuessState, isSubmitting: boolean) {
    state.isSubmitting = isSubmitting;
  },
  SET_HAS_SUBMITTED(state: GuessState, hasSubmitted: boolean) {
    state.hasSubmitted = hasSubmitted;
  },
  RESET_GUESS(state: GuessState) {
    //reset everything except user id
    state.time = 0;
    state.building = null;
    state.guessX = null;
    state.guessY = null;
    state.floor = null;
    state.isSubmitting = false;
    state.hasSubmitted = false;
  },
};
