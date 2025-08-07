// src/stores/modules/guess/index.ts
import type { Module } from 'vuex';
import type { RootState } from '../../index';
import { state, type GuessState } from './state';
import { actions } from './actions';

export const guessModule: Module<GuessState, RootState> = {
  namespaced: true,
  state,
  actions
};

export default guessModule;
