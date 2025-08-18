// src/store/modules/leaderboard/getters.ts
import type { LeaderboardState } from './types';

export const getters = {
  leaderboard: (state: LeaderboardState) => state.leaderboardData?.results || [],
  totalResults: (state: LeaderboardState) => state.leaderboardData?.results.length || 0,
  isLoading: (state: LeaderboardState) => state.loading,
  hasError: (state: LeaderboardState) => state.error !== null,
  error: (state: LeaderboardState) => state.error,
  currentQuery: (state: LeaderboardState) => state.currentQuery,
  currentPage: (state: LeaderboardState) =>
    Math.floor((state.currentQuery.offset || 0) / (state.currentQuery.limit || 20)) + 1,
  hasNextPage: (state: LeaderboardState) => {
    if (!state.leaderboardData?.results) return false;
    const limit = state.currentQuery.limit || 20;
    const offset = state.currentQuery.offset || 0;
    return state.leaderboardData.results.length === limit;
  },
};
