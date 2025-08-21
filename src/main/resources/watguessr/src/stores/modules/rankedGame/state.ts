// src/stores/modules/rankedGame/state.ts

export type PlayerStatus = 'idle' | 'loading' | 'playing' | 'ended' | 'ready' | 'completed' | 'disconnected';

export interface PlayerInfo {
  status: PlayerStatus;
  score: number;
  username: string;
  lastSeen?: number; // timestamp when player was last seen
}

export interface RankedGameResult {
  userPoints: Record<string, number>;
  eloChanges: Record<string, number>;
}

export interface RankedGameState {
  rankedGame_gameId: string;
  rankedGame_players: Record<string, PlayerInfo>; // key: player id, value: info object
  rankedGame_currentRound: number;
  rankedGame_maxRounds: number;
  rankedGame_timer: number;
  rankedGame_finalWinner: string | null;
  rankedGame_shouldEnd: boolean;
  rankedGame_result: RankedGameResult;
  disconnectionCheckInterval?: number; // interval ID for disconnection checking
}

export const state = (): RankedGameState => ({
  rankedGame_gameId: '',
  rankedGame_players: {},
  rankedGame_currentRound: 1,
  rankedGame_maxRounds: 5,
  rankedGame_timer: 30000,
  rankedGame_finalWinner: null,
  rankedGame_shouldEnd: false,
  rankedGame_result: {
    userPoints: {},
    eloChanges: {}
  },
});
