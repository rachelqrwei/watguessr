// src/stores/modules/building/index.ts
import type { Module } from 'vuex';
import type { RootState } from '../../index';
import { state, type BuildingState } from './state';
import { actions } from './actions';
import { mutations } from './mutations';
import { getters } from './getters';

const buildingModule: Module<BuildingState, RootState> = {
  namespaced: true,
  state,
  actions,
  mutations,
  getters,
};

export default buildingModule; 