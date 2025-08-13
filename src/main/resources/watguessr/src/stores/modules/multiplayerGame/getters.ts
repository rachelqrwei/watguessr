// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type { GameState } from './state';

export const getters: GetterTree<GameState, RootState> = {
  getMultiplayerGame_players: (state) => state.multiplayerGame_players,
  getMultiplayerGame_currentRound: (state) => state.multiplayerGame_currentRound,
  getMultiplayerGame_maxRounds: (state) => state.multiplayerGame_currentRound,
  getMultiplayerGame_finalWinner: (state) => state.multiplayerGame_finalWinner,
  getMultiplayerGame_shouldEnd: (state) => state.multiplayerGame_shouldEnd
};
