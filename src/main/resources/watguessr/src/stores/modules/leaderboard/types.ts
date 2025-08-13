// src/store/modules/leaderboard/types.ts

export interface LeaderboardUser {
  id: string;
  username: string;
  elo: number;
  streak: number;
  gamesPlayed: number;
  gamesWon: number;
  gamesLost: number;
}

export interface LeaderboardRequest {
  searchTerm?: string;
  sortBy?: string;
  limit?: number;
  offset?: number;
}

export interface QueryResults {
  results: LeaderboardUser[];
}

export interface LeaderboardState {
  leaderboardData: QueryResults | null;
  loading: boolean;
  error: string | null;
  currentQuery: LeaderboardRequest;
}
