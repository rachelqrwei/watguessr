// src/stores/modules/guess/mutations.ts
import type { MutationTree } from 'vuex';
import type { GuessState } from './state';

export const mutations: MutationTree<GuessState> = {
  SET_USER_ID(state, userId: string) {
    state.user.id = userId;
  },
  SET_TIME(state, time: number) {
    state.time = time;
  },
  SET_BUILDING_AND_LOCATIONS(state, payload: {building: string, guessX: number, guessY: number}) {
    state.building = payload.building;
    state.guessX = payload.guessX;
    state.guessY = payload.guessY;
  },
  SET_FLOOR(state, floor: string) {
    state.floor = floor;
  },
  RESET_USER_ID(state) {
    state.user.id = null;
  },
  RESET_GUESS(state) {
    //reset everything except user id
    state.time = 0;
    state.building = null;
    state.guessX = null;
    state.guessY = null;
    state.floor = null;
  },
};
