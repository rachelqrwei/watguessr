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
  MG_SET_MAX_ROUNDS(state, maxRounds: number){
      state.multiplayerGame_maxRounds = maxRounds
  },
  MG_SET_TIMER(state, timer: number) {
    state.multiplayerGame_timer = timer;
  },
  MG_SET_SHOULD_END(state, shouldEnd: boolean) {
    state.multiplayerGame_shouldEnd = shouldEnd;
  },

  // Save final game data for persistence
  MG_SAVE_FINAL_GAME_DATA(state, finalGameData: {
    players: Record<string, PlayerInfo>;
    finalWinner: string | null;
    gameId: string;
    currentRound: number;
    maxRounds: number;
  }) {
    state.multiplayerGame_players = finalGameData.players;
    state.multiplayerGame_finalWinner = finalGameData.finalWinner;
    state.multiplayerGame_gameId = finalGameData.gameId;
    state.multiplayerGame_currentRound = finalGameData.currentRound;
    state.multiplayerGame_maxRounds = finalGameData.maxRounds;
    state.multiplayerGame_shouldEnd = true;

    // Store in localStorage for persistence across page navigation
    try {
      localStorage.setItem('multiplayerGameFinalData', JSON.stringify(finalGameData));
    } catch (error) {
      console.error('Failed to save final game data to localStorage:', error);
    }
  },

  // Load final game data from localStorage
  MG_LOAD_FINAL_GAME_DATA(state) {
    try {
      const stored = localStorage.getItem('multiplayerGameFinalData');
      if (stored) {
        const finalGameData = JSON.parse(stored);
        state.multiplayerGame_players = finalGameData.players;
        state.multiplayerGame_finalWinner = finalGameData.finalWinner;
        state.multiplayerGame_gameId = finalGameData.gameId;
        state.multiplayerGame_currentRound = finalGameData.currentRound;
        state.multiplayerGame_maxRounds = finalGameData.maxRounds;
        state.multiplayerGame_shouldEnd = true;
      }
    } catch (error) {
      console.error('Failed to load final game data from localStorage:', error);
    }
  },

  // Clear final game data
  MG_CLEAR_FINAL_GAME_DATA(state) {
    try {
      localStorage.removeItem('multiplayerGameFinalData');
    } catch (error) {
      console.error('Failed to clear final game data from localStorage:', error);
    }
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
