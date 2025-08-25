// src/store/modules/leaderboard/getters.ts
import type { LeaderboardState } from './types';

export const getters = {
  leaderboard: (state: LeaderboardState) => state.leaderboardData?.results || [],
  totalResults: (state: LeaderboardState) => state.leaderboardData?.totalCount || 0,
  isLoading: (state: LeaderboardState) => state.loading,
  hasError: (state: LeaderboardState) => state.error !== null,
  error: (state: LeaderboardState) => state.error,
  currentQuery: (state: LeaderboardState) => state.currentQuery,
  currentPage: (state: LeaderboardState) =>
    Math.floor((state.currentQuery.offset || 0) / (state.currentQuery.limit || 50)) + 1,
  hasNextPage: (state: LeaderboardState) => {
    if (!state.leaderboardData?.totalCount) return false;
    const limit = state.currentQuery.limit || 50;
    const offset = state.currentQuery.offset || 0;
    return (offset + limit) < state.leaderboardData.totalCount;
  },
  hasPreviousPage: (state: LeaderboardState) => {
    const offset = state.currentQuery.offset || 0;
    return offset > 0;
  },
  totalPages: (state: LeaderboardState) => {
    if (!state.leaderboardData?.totalCount) return 0;
    const limit = state.currentQuery.limit || 50;
    return Math.ceil(state.leaderboardData.totalCount / limit);
  },
};
