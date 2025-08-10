import type { Module } from 'vuex';
import type { RootState } from '../../index';
import { state, type RoundState } from './state';
import { getters } from './getters';
import { mutations } from './mutations';
import { actions } from './actions';

const roundModule: Module<RoundState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
};

export default roundModule;
