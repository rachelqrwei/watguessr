// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type { RootState } from '../../index';
import type { singleplayerGameState } from './state';

export const getters: GetterTree<singleplayerGameState, RootState> = {
  singleplayerGame_getGameId: (state) => state.singleplayerGame_gameId,
  singleplayerGame_getGameMode: (state) => state.singleplayerGame_gameMode,
  singleplayerGame_getCurrentRound: (state) => state.singleplayerGame_currentRound,
  singleplayerGame_getGameStatus: (state) => state.singleplayerGame_status,
  singleplayerGame_getFinalWinner: (state) => state.singleplayerGame_finalWinner,
  singleplayerGame_getScores: (state) => state.singleplayerGame_scores,
  singleplayerGame_getCurrentView: (state) => state.singleplayerGame_currentView,
  singleplayerGame_getShouldEnd: (state) => state.singleplayerGame_shouldEnd,
  
  singleplayerGame_getSingleplayerDisplayedScore: (state) => {
    const mode = state.singleplayerGame_gameMode;
    if (mode !== 'Singleplayer') return null;

    return typeof state.singleplayerGame_singleplayerScore === 'number' ? state.singleplayerGame_singleplayerScore : 1000;
  },
};
