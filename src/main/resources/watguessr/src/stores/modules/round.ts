// src/stores/modules/round.ts
import type { Module, ActionTree, MutationTree } from 'vuex';
import type { RootState } from '../index';

export interface Scene {
  building: string;
  floor: string;
}

export interface RoundState {
  scene: Scene | null;
  winner: string | null;
}

// Define mutations with proper typing
const mutations: MutationTree<RoundState> = {
  SET_SCENE(state: RoundState, scene: Scene) {
    state.scene = scene;
  },
  SET_WINNER(state: RoundState, winner: string) {
    state.winner = winner;
  },
  RESET_ROUND(state: RoundState) {
    state.scene = null;
    state.winner = null;
  },
};

// Define actions with proper typing
const actions: ActionTree<RoundState, RootState> = {
  setWinner({ commit, dispatch }, winner: string) {
    commit('SET_WINNER', winner);
    dispatch('game/recordRoundWinner', winner, { root: true });
  },

  startRound({ commit }, scene: Scene) {
    commit('SET_SCENE', scene);
  },

  resetRound({ commit }) {
    commit('RESET_ROUND');
  },
};

const roundModule: Module<RoundState, RootState> = {
  namespaced: true,

  state: (): RoundState => ({
    scene: null,
    winner: null,
  }),

  mutations,
  actions,
};

export default roundModule;
