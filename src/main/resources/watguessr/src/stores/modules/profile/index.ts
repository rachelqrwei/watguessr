import type { Module } from 'vuex';
import { state, type ProfileState } from './state';
import { getters } from './getters';
import { mutations } from './mutations';
import type { RootState } from '@/stores';

const profileModule: Module<ProfileState, RootState> = {
  namespaced: true,
  state,
  getters,
  mutations
};

export default profileModule;
