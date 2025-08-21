// src/stores/modules/game/mutations.ts
import type { MutationTree } from 'vuex';
import type {RankedGameState, PlayerInfo, PlayerStatus} from './state';

export const mutations: MutationTree<RankedGameState> = {
  RG_SET_GAME_ID(state, rankedGame_gameId: string) {
    state.rankedGame_gameId = rankedGame_gameId
  },
  RG_SET_PLAYERS(state, rankedGame_players: Record<string, PlayerInfo>) {
    state.rankedGame_players = rankedGame_players;
  },
  RG_SET_STATUS(state, {playerId, status}: {playerId: string, status: PlayerStatus}) {
    if (state.rankedGame_players[playerId]) {
      state.rankedGame_players[playerId].status = status;
    } else {
      console.warn(`Player ${playerId} not found in store, cannot set status to ${status}`);
    }
  },
  RG_INCREMENT_ROUND(state) {
    state.rankedGame_currentRound++;
  },
  RG_IMPLEMENT_ROUND_RESULT(state,
                            {playerId, roundResult}: { playerId: string, roundResult: {points: number, distance: number} }) {
    if (state.rankedGame_players[playerId]) {
      state.rankedGame_players[playerId].score += roundResult.points;
    }
  },
  RG_SET_FINAL_WINNER(state, rankedGame_finalWinner: string) {
    state.rankedGame_finalWinner = rankedGame_finalWinner;
  },
  RG_SET_CURRENT_ROUND(state, currentRound: number) {
    state.rankedGame_currentRound = currentRound;
  },
  RG_SET_MAX_ROUNDS(state, maxRounds: number){
      state.rankedGame_maxRounds = maxRounds
  },
  RG_SET_TIMER(state, timer: number) {
    state.rankedGame_timer = timer;
  },
  RG_SET_SHOULD_END(state, shouldEnd: boolean) {
    state.rankedGame_shouldEnd = shouldEnd;
  },

  // Save final game data for persistence
  RG_SAVE_FINAL_GAME_DATA(state, finalGameData: {
    players: Record<string, PlayerInfo>;
    finalWinner: string | null;
    gameId: string;
    currentRound: number;
    maxRounds: number;
  }) {
    state.rankedGame_players = finalGameData.players;
    state.rankedGame_finalWinner = finalGameData.finalWinner;
    state.rankedGame_gameId = finalGameData.gameId;
    state.rankedGame_currentRound = finalGameData.currentRound;
    state.rankedGame_maxRounds = finalGameData.maxRounds;
    state.rankedGame_shouldEnd = true;

    // Store in localStorage for persistence across page navigation
    try {
      localStorage.setItem('rankedGameFinalData', JSON.stringify(finalGameData));
    } catch (error) {
      console.error('Failed to save final game data to localStorage:', error);
    }
  },

  // Load final game data from localStorage
  RG_LOAD_FINAL_GAME_DATA(state) {
    try {
      const stored = localStorage.getItem('rankedGameFinalData');
      if (stored) {
        const finalGameData = JSON.parse(stored);
        state.rankedGame_players = finalGameData.players;
        state.rankedGame_finalWinner = finalGameData.finalWinner;
        state.rankedGame_gameId = finalGameData.gameId;
        state.rankedGame_currentRound = finalGameData.currentRound;
        state.rankedGame_maxRounds = finalGameData.maxRounds;
        state.rankedGame_shouldEnd = true;
      }
    } catch (error) {
      console.error('Failed to load final game data from localStorage:', error);
    }
  },

  // Clear final game data
  RG_CLEAR_FINAL_GAME_DATA(state) {
    try {
      localStorage.removeItem('rankedGameFinalData');
    } catch (error) {
      console.error('Failed to clear final game data from localStorage:', error);
    }
  },
  RG_RESET_GAME(state, payload?: { userId: string; username: string }) {
    state.rankedGame_currentRound = 1;
    state.rankedGame_maxRounds = 5;
    state.rankedGame_finalWinner = null;
    state.rankedGame_shouldEnd = false;
    
    // Initialize player if payload is provided
    if (payload) {
      state.rankedGame_players = {
        [payload.userId]: { 
          score: 0, 
          status: 'loading', 
          username: payload.username 
        }
      };
    } else {
      state.rankedGame_players = {};
    }
  },

  RG_SET_PLAYER_DISCONNECTED(state, playerId: string) {
    if (state.rankedGame_players[playerId]) {
      state.rankedGame_players[playerId].status = 'disconnected';
      state.rankedGame_players[playerId].lastSeen = Date.now();
    }
  },

  RG_REMOVE_PLAYER(state, playerId: string) {
    if (state.rankedGame_players[playerId]) {
      delete state.rankedGame_players[playerId];
    }
  },

  RG_SET_GAME_ABANDONED(state) {
    state.rankedGame_shouldEnd = true;
    state.rankedGame_finalWinner = null;
  },

  RG_SET_DISCONNECTION_CHECK_INTERVAL(state, intervalId: number) {
    state.disconnectionCheckInterval = intervalId;
  },

  RG_CLEAR_DISCONNECTION_CHECK_INTERVAL(state) {
    if (state.disconnectionCheckInterval) {
      clearInterval(state.disconnectionCheckInterval);
      state.disconnectionCheckInterval = undefined;
    }
  },
};
