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
    state.multiplayerGame_players[playerId].status = status;
  },
  MG_INCREMENT_ROUND(state) {
    state.multiplayerGame_currentRound++;
  },
  MG_IMPLEMENT_ROUND_RESULT(state,
                            {playerId, roundResult}: { playerId: string, roundResult: {points: number, distance: number, shouldEnd: boolean} }) {
    state.multiplayerGame_players[playerId].score += roundResult.points;
    state.multiplayerGame_shouldEnd = roundResult.shouldEnd;
  },
  MG_SET_FINAL_WINNER(state, multiplayerGame_finalWinner: string) {
    state.multiplayerGame_finalWinner = multiplayerGame_finalWinner;
  },
  MG_RESET_GAME(state, userId: string) {
    state.multiplayerGame_players = {
      [userId]: {
        score: 0,
        status: 'loading'
      }};
    state.multiplayerGame_currentRound = 1;
    state.multiplayerGame_maxRounds = 5;
    state.multiplayerGame_finalWinner = null;
    state.multiplayerGame_shouldEnd = false;
  },
};
