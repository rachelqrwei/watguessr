// src/stores/modules/game/state.ts
export interface GameState {
  playerId: string | null,
  gameId: string | null;
  gameMode: string;
  status: 'idle' | 'loading' | 'playing' | 'ended';
  currentRound: number;
  maxRounds: number;
  scores: Record<string, number>;
  finalWinner: string | null;
  currentView: string;
}

export const state = (): GameState => ({
  playerId: null,
  gameId: null,
  gameMode: '',
  status: 'idle',
  currentRound: 1,
  maxRounds: 5,
  scores: {},
  finalWinner: null,
  currentView: 'Map'
});
