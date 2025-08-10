// src/stores/modules/game/state.ts
export interface GameState {
  playerId: string | null,
  gameId: string | null;
  gameMode: string;
  status: 'idle' | 'loading' | 'playing' | 'ended';
  currentRound: number;
  maxRounds: number;
  scores: any;
  finalWinner: string | null;
  currentView: string;
  players: any[];
  playersCompletionStatus: any;
}

export const state = (): GameState => ({
  playerId: null,
  gameId: null,
  gameMode: '',
  status: 'idle',
  currentRound: 1,
  maxRounds: 5,
  finalWinner: null,
  currentView: 'Map',
  players: [
    { id: 'player1', name: 'Alice' },
    { id: 'player2', name: 'Bob' },
    { id: 'player3', name: 'Charlie' },
    { id: 'player4', name: 'Dana' }
  ],
  playersCompletionStatus: {
    player1: true,   // Alice completed
    player2: false,  // Bob not completed
    player3: true,   // Charlie completed
    player4: false   // Dana not completed
  },
  scores: {
  }
});
