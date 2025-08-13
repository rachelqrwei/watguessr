// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type { RootState } from '../../index';
import type { GameState } from './state';

export const getters: GetterTree<GameState, RootState> = {
  getGameId: (state) => state.gameId,
  getGameMode: (state) => state.gameMode,
  getCurrentRound: (state) => state.currentRound,
  getMaxRounds: (state) => state.maxRounds,
  getGameStatus: (state) => state.status,
  getFinalWinner: (state) => state.finalWinner,
  getScores: (state) => state.scores,
  getCurrentView: (state) => state.currentView
};
