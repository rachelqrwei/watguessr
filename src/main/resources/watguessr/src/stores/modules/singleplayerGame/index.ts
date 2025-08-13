// src/stores/modules/game/index.ts
import type { Module } from 'vuex';
import type { RootState } from '../../index';
import { state, type singleplayerGameState } from './state';
import { getters } from './getters';
import { mutations } from './mutations';
import { actions } from './actions';

export const singleplayerGameModule: Module<singleplayerGameState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};

export default singleplayerGameModule;
