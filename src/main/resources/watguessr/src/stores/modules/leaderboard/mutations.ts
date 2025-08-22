// src/store/modules/leaderboard/mutations.ts
import type { LeaderboardState, QueryResults, LeaderboardRequest } from './types';

export const mutations = {
  SET_LEADERBOARD_DATA(state: LeaderboardState, data: QueryResults) {
    state.leaderboardData = data;
  },
  SET_LOADING(state: LeaderboardState, loading: boolean) {
    state.loading = loading;
  },
  SET_ERROR(state: LeaderboardState, error: string | null) {
    state.error = error;
  },
  SET_CURRENT_QUERY(state: LeaderboardState, query: LeaderboardRequest) {
    state.currentQuery = { ...state.currentQuery, ...query };
  },
  RESET_QUERY(state: LeaderboardState) {
    state.currentQuery = {
      searchTerm: '',
      sortBy: 'elo',
      limit: 50,
      offset: 0,
    };
  },
  CLEAR_ERROR(state: LeaderboardState) {
    state.error = null;
  },
};
