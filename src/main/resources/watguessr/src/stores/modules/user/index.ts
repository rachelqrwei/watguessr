import type { Module } from 'vuex';
import {state, type UserState} from './state';
import { getters } from './getters';
import { actions } from './actions';
import { mutations } from './mutations';
import type {RootState} from "@/stores";

const userModule: Module<UserState, RootState> = {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
};

export default userModule;
