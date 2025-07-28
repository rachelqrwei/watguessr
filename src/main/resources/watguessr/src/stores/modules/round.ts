// src/stores/modules/round.ts
import type {Module, ActionTree, MutationTree, GetterTree} from 'vuex';
import type { RootState } from '../index';

export interface Scene {
  building: string;
  floor: string;
}

export interface RoundState {
  roundId: string | null;
  scene: Scene | null;
  winner: string | null;
}

const getters: GetterTree<RoundState, RootState> = {
  roundId: (state) => state.roundId,
  scene: (state) => state.scene,
  winner: (state) => state.winner
}

// Define mutations with proper typing
const mutations: MutationTree<RoundState> = {
  SET_ROUND_ID(state: RoundState, roundId: string) {
    state.roundId = roundId;
  },
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

  async startRound({ rootState }, {gameId}): Promise<string> {
    if (!gameId) throw new Error('Game ID not found');

    const response = await fetch(`http://localhost:5173/api/round/create?gameId=${gameId}`, {
      method: 'GET',
    });

    if (!response.ok) {
      throw new Error('Failed to create round');
    }

    const roundId: string = await response.json();
    return roundId;
  },

  resetRound({ commit }) {
    commit('RESET_ROUND');
  },
};

const roundModule: Module<RoundState, RootState> = {
  namespaced: true,

  state: (): RoundState => ({
    roundId: null,
    scene: null,
    winner: null,
  }),
  mutations,
  actions,
  getters
};

export default roundModule;
