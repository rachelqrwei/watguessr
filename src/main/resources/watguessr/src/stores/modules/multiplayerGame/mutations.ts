// src/stores/modules/game/mutations.ts
import type { MutationTree } from 'vuex';
import type {MultiplayerGameState, PlayerInfo, PlayerStatus} from './state';

export const mutations: MutationTree<MultiplayerGameState> = {
  MG_SET_GAME_ID(state, multiplayerGame_gameId: string) {
    state.multiplayerGame_gameId = multiplayerGame_gameId
  },
  MG_SET_PLAYERS(state, multiplayerGame_players: Record<string, PlayerInfo>) {
    state.multiplayerGame_players = multiplayerGame_players;
  },
  MG_SET_STATUS(state, {playerId, status}: {playerId: string, status: PlayerStatus}) {
    if (state.multiplayerGame_players[playerId]) {
      state.multiplayerGame_players[playerId].status = status;
    } else {
      console.warn(`Player ${playerId} not found in store, cannot set status to ${status}`);
    }
  },
  MG_INCREMENT_ROUND(state) {
    state.multiplayerGame_currentRound++;
  },
  MG_IMPLEMENT_ROUND_RESULT(state,
                            {playerId, roundResult}: { playerId: string, roundResult: {points: number, distance: number} }) {
    if (state.multiplayerGame_players[playerId]) {
      state.multiplayerGame_players[playerId].score += roundResult.points;
    }
  },
  MG_SET_FINAL_WINNER(state, multiplayerGame_finalWinner: string) {
    state.multiplayerGame_finalWinner = multiplayerGame_finalWinner;
  },
  MG_SET_CURRENT_ROUND(state, currentRound: number) {
    state.multiplayerGame_currentRound = currentRound;
  },
  MG_SET_MAX_ROUNDS(state, maxRounds: number) {
    state.multiplayerGame_maxRounds = maxRounds;
  },
  MG_SET_SHOULD_END(state, shouldEnd: boolean) {
    state.multiplayerGame_shouldEnd = shouldEnd;
  },
  MG_RESET_GAME(state) {
    state.multiplayerGame_currentRound = 1;
    state.multiplayerGame_maxRounds = 5;
    state.multiplayerGame_finalWinner = null;
    state.multiplayerGame_shouldEnd = false;
  },

  MG_SET_PLAYER_DISCONNECTED(state, playerId: string) {
    if (state.multiplayerGame_players[playerId]) {
      state.multiplayerGame_players[playerId].status = 'disconnected';
      state.multiplayerGame_players[playerId].lastSeen = Date.now();
    }
  },

  MG_REMOVE_PLAYER(state, playerId: string) {
    if (state.multiplayerGame_players[playerId]) {
      delete state.multiplayerGame_players[playerId];
    }
  },

  MG_SET_GAME_ABANDONED(state) {
    state.multiplayerGame_shouldEnd = true;
    state.multiplayerGame_finalWinner = null;
  },

  MG_SET_DISCONNECTION_CHECK_INTERVAL(state, intervalId: number) {
    state.disconnectionCheckInterval = intervalId;
  },

  MG_CLEAR_DISCONNECTION_CHECK_INTERVAL(state) {
    if (state.disconnectionCheckInterval) {
      clearInterval(state.disconnectionCheckInterval);
      state.disconnectionCheckInterval = undefined;
    }
  },
};
