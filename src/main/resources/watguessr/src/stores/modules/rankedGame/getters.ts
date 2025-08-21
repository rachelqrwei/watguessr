// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type { RankedGameState } from './state';
import type {RootState} from "@/stores";

export const getters: GetterTree<RankedGameState, RootState> = {
  rankedGame_getGameId: (state) => state.rankedGame_gameId,
  rankedGame_getPlayers: (state) => state.rankedGame_players,
  rankedGame_getCurrentRound: (state) => state.rankedGame_currentRound,
  rankedGame_getMaxRounds: (state) => state.rankedGame_maxRounds,
  rankedGame_getFinalWinner: (state) => state.rankedGame_finalWinner,
  rankedGame_getShouldEnd: (state) => state.rankedGame_shouldEnd,
  rankedGame_getTimer: (state) => state.rankedGame_timer,
  rankedGame_getResult: (state) => state.rankedGame_result,

  // New getters for disconnection handling
  rankedGame_getConnectedPlayers: (state) => {
    return Object.entries(state.rankedGame_players)
      .filter(([_, player]) => player.status !== 'disconnected')
      .reduce((acc, [id, player]) => ({ ...acc, [id]: player }), {});
  },

  rankedGame_getDisconnectedPlayers: (state) => {
    return Object.entries(state.rankedGame_players)
      .filter(([_, player]) => player.status === 'disconnected')
      .reduce((acc, [id, player]) => ({ ...acc, [id]: player }), {});
  },

  rankedGame_hasDisconnectedPlayers: (state) => {
    return Object.values(state.rankedGame_players)
      .some(player => player.status === 'disconnected');
  },

  rankedGame_isGameAbandoned: (state) => {
    const connectedPlayers = Object.values(state.rankedGame_players)
      .filter(player => player.status !== 'disconnected');
    return connectedPlayers.length < 2;
  },

  rankedGame_getActivePlayerCount: (state) => {
    return Object.values(state.rankedGame_players)
      .filter(player => player.status !== 'disconnected').length;
  }
};
