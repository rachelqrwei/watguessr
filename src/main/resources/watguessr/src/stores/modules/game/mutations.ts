// src/stores/modules/game/mutations.ts
import type { MutationTree } from 'vuex';
import type { GameState } from './state';

export const mutations: MutationTree<GameState> = {
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
  CHANGE_VIEW(state, nextView: string) {
    state.currentView = nextView;
  },
  ADD_SCORE(state, payload: { username: string; roundResult: {points: number, distance: number} }) {
    if (!state.scores[payload.username]) {
      state.scores[payload.username] = 0;
    }
    state.scores[payload.username] += payload.roundResult.points;
  },
  SET_FINAL_WINNER(state, finalWinner: string) {
    state.finalWinner = finalWinner;
    state.status = 'ended';
  },
  RESET_GAME(state) {
    state.gameId = null;
    state.status = 'idle';
    state.currentRound = 1;
    state.scores = {};
    state.finalWinner = null;
  },
};
