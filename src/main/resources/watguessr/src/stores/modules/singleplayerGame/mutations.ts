// src/stores/modules/game/mutations.ts
import type { MutationTree } from 'vuex';
import type { singleplayerGameState } from './state';

export const mutations: MutationTree<singleplayerGameState> = {
  SG_SET_GAME_ID(state, gameId: string) {
    state.singleplayerGame_gameId = gameId;
  },
  SG_SET_STATUS(state, status: singleplayerGameState['singleplayerGame_status']) {
    state.singleplayerGame_status = status;
  },
  SG_INCREMENT_ROUND(state) {
    state.singleplayerGame_currentRound++;
  },
  SG_ADD_SINGLEPLAYER_PENALTY(state, payload: { userId?: string; roundResult: {points: number, distance: number} }) {
    const userKey = payload.userId || 'player';
    if (!state.singleplayerGame_scores[userKey]) {
      state.singleplayerGame_scores[userKey] = 0;
    }
    state.singleplayerGame_scores[userKey] += payload.roundResult.points;
  },
  SG_SET_FINAL_WINNER(state, finalWinner: string) {
    state.singleplayerGame_finalWinner = finalWinner;
    state.singleplayerGame_status = 'ended';
  },
  SG_SET_SHOULD_END(state, shouldEnd: boolean) {
    state.singleplayerGame_shouldEnd = shouldEnd;
  },
  SG_SET_SINGLEPLAYER_SCORE(state, remaining: number | null) {
    state.singleplayerGame_singleplayerScore = remaining;
  },
  SG_RESET_GAME(state) {
    state.singleplayerGame_gameId = null;
    state.singleplayerGame_status = 'idle';
    state.singleplayerGame_currentRound = 0;
    state.singleplayerGame_scores = {};
    state.singleplayerGame_finalWinner = null;
    state.singleplayerGame_shouldEnd = false;
    state.singleplayerGame_singleplayerScore = null;
  },
};
