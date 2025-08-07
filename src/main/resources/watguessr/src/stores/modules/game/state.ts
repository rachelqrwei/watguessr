// src/stores/modules/game/state.ts
export interface GameState {
  gameId: string | null;
  gameMode: string;
  status: 'idle' | 'loading' | 'playing' | 'ended';
  currentRound: number;
  maxRounds: number;
  scores: Record<string, number>;
  winner: string | null;
  currentView: string;
}

export const state = (): GameState => ({
  gameId: null,
  gameMode: '',
  status: 'idle',
  currentRound: 1,
  maxRounds: 5,
  scores: {},
  winner: null,
  currentView: 'Map'
});
