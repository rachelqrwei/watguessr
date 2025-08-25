// src/store/modules/leaderboard/state.ts
import type {LeaderboardState} from './types';

export const state: LeaderboardState = {
  leaderboardData: null,
  loading: false,
  error: null,
  currentQuery: {
    searchTerm: '',
    sortBy: 'elo',
    limit: 5,
    offset: 0
  }
}
