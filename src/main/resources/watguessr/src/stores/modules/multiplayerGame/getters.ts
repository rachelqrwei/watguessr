// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type { MultiplayerGameState } from './state';
import type {RootState} from "@/stores";

export const getters: GetterTree<MultiplayerGameState, RootState> = {
  multiplayerGame_getGameId: (state) => state.multiplayerGame_gameId,
  multiplayerGame_getPlayers: (state) => state.multiplayerGame_players,
  multiplayerGame_getCurrentRound: (state) => state.multiplayerGame_currentRound,
  multiplayerGame_getMaxRounds: (state) => state.multiplayerGame_maxRounds,
  multiplayerGame_getFinalWinner: (state) => state.multiplayerGame_finalWinner,
  multiplayerGame_getShouldEnd: (state) => state.multiplayerGame_shouldEnd,
  multiplayerGame_getTimer: (state) => state.multiplayerGame_timer,
  
  // New getters for disconnection handling
  multiplayerGame_getConnectedPlayers: (state) => {
    return Object.entries(state.multiplayerGame_players)
      .filter(([_, player]) => player.status !== 'disconnected')
      .reduce((acc, [id, player]) => ({ ...acc, [id]: player }), {});
  },
  
  multiplayerGame_getDisconnectedPlayers: (state) => {
    return Object.entries(state.multiplayerGame_players)
      .filter(([_, player]) => player.status === 'disconnected')
      .reduce((acc, [id, player]) => ({ ...acc, [id]: player }), {});
  },
  
  multiplayerGame_hasDisconnectedPlayers: (state) => {
    return Object.values(state.multiplayerGame_players)
      .some(player => player.status === 'disconnected');
  },
  
  multiplayerGame_isGameAbandoned: (state) => {
    const connectedPlayers = Object.values(state.multiplayerGame_players)
      .filter(player => player.status !== 'disconnected');
    return connectedPlayers.length < 2;
  },
  
  multiplayerGame_getActivePlayerCount: (state) => {
    return Object.values(state.multiplayerGame_players)
      .filter(player => player.status !== 'disconnected').length;
  }
};
