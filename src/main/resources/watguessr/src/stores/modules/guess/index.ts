// src/stores/modules/guess/index.ts
import type { Module } from 'vuex';
import type { RootState } from '../../index';
import { state, type GuessState } from './state';
import { actions } from './actions';
import { mutations } from './mutations';
import { getters } from './getters';

export const guessModule: Module<GuessState, RootState> = {
  namespaced: true,
  state,
  actions,
  mutations,
  getters
};

export default guessModule;
