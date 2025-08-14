// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type { MultiplayerGameState } from './state';
import type {RootState} from "@/stores";

export const getters: GetterTree<MultiplayerGameState, RootState> = {
  multiplayerGame_getPlayers: (state) => state.multiplayerGame_players,
  multiplayerGame_getCurrentRound: (state) => state.multiplayerGame_currentRound,
  multiplayerGame_getMaxRounds: (state) => state.multiplayerGame_currentRound,
  multiplayerGame_getFinalWinner: (state) => state.multiplayerGame_finalWinner,
  multiplayerGame_getShouldEnd: (state) => state.multiplayerGame_shouldEnd,
};
