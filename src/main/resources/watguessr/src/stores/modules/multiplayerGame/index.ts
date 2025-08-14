// src/stores/modules/game/index.ts
import type { Module } from 'vuex';
import type { RootState } from '../../index';
import { state, type MultiplayerGameState } from './state';
import { getters } from './getters';
import { mutations } from './mutations';
import { actions } from './actions';

export const multiplayerGameModule: Module<MultiplayerGameState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};

export default multiplayerGameModule;
