// src/stores/modules/game.ts
import type { Module, ActionTree, MutationTree, GetterTree } from 'vuex';
import type { RootState } from '../index';

// Define GameState
export interface GameState {
  gameId: string | null;
  gameMode: string;
  status: 'idle' | 'loading' | 'playing' | 'ended';
  currentRound: number;
  maxRounds: number;
  scores: Record<string, number>;
  winner: string | null;
}

// Initial State
const state: GameState = {
  gameId: null,
  gameMode: '',
  status: 'idle',
  currentRound: 1,
  maxRounds: 5,
  scores: {},
  winner: null,
};

// Define mutations with proper typing
const mutations: MutationTree<GameState> = {
  SET_GAME_ID(state, gameId: string) {
    state.gameId = gameId;
  },
  SET_GAME_MODE(state, mode: string) {
    state.gameMode = mode;
  },
  SET_STATUS(state, status: GameState['status']) {
    state.status = status;
  },
  INCREMENT_ROUND(state) {
    state.currentRound++;
  },
  ADD_SCORE(state, username: string) {
    if (!state.scores[username]) {
      state.scores[username] = 0;
    }
    state.scores[username]++;
  },
  SET_FINAL_WINNER(state, winner: string) {
    state.winner = winner;
    state.status = 'ended';
  },
  RESET_GAME(state) {
    state.gameId = null;
    state.status = 'idle';
    state.currentRound = 1;
    state.scores = {};
    state.winner = null;
  },
};

// Define actions with proper typing
const actions: ActionTree<GameState, RootState> = {
  createSingleplayerGame({ commit }) {
    commit('RESET_GAME');
    commit('SET_STATUS', 'loading');
    commit('SET_GAME_MODE', 'Singleplayer');

    const gameId = Date.now().toString();
    commit('SET_GAME_ID', gameId);
    commit('SET_STATUS', 'playing');
  },

  recordRoundWinner({ state, commit, dispatch }, username: string) {
    commit('ADD_SCORE', username);

    if (state.currentRound >= state.maxRounds) {
      const winner = Object.entries(state.scores).sort((a, b) => b[1] - a[1])[0][0];
      commit('SET_FINAL_WINNER', winner);
    } else {
      commit('INCREMENT_ROUND');
      dispatch('round/resetRound', null, { root: true });

      const newScene = generateNextScene(); // Replace with actual scene logic
      dispatch('round/startRound', newScene, { root: true });
    }
  },
};

// Define getters with proper typing
const getters: GetterTree<GameState, RootState> = {
  gameId: (state) => state.gameId,
  currentRound: (state) => state.currentRound,
  gameStatus: (state) => state.status,
  finalWinner: (state) => state.winner,
  scores: (state) => state.scores,
};

export const gameModule: Module<GameState, RootState> = {
  namespaced: true,
  state,
  mutations,
  actions,
  getters,
};

// Dummy placeholder for now
function generateNextScene() {
  return {}; // Replace with actual scene generation logic
}
