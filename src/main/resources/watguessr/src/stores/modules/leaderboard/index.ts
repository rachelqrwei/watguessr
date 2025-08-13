// src/store/modules/leaderboard/index.ts
import type { Module } from 'vuex';
import { state } from './state';
import { mutations } from './mutations';
import { actions } from './actions';
import { getters } from './getters';
import type { LeaderboardState } from './types';

const leaderboardModule: Module<LeaderboardState, any> = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};

export default leaderboardModule;
