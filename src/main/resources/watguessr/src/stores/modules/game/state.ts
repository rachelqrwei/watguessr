// src/stores/modules/game/state.ts
export interface singleplayerGameState {
  singleplayerGame_playerId: string | null,
  singleplayerGame_gameId: string | null;
  singleplayerGame_gameMode: string;
  singleplayerGame_status: 'idle' | 'loading' | 'playing' | 'ended';
  singleplayerGame_currentRound: number;
  singleplayerGame_scores: Record<string, number>;
  singleplayerGame_finalWinner: string | null;
  singleplayerGame_currentView: string;
  singleplayerGame_shouldEnd: boolean;
  singleplayerGame_singleplayerScore?: number | null;
}

export const state = (): singleplayerGameState => ({
  singleplayerGame_playerId: null,
  singleplayerGame_gameId: null,
  singleplayerGame_gameMode: '',
  singleplayerGame_status: 'idle',
  singleplayerGame_currentRound: 1,
  singleplayerGame_scores: {},
  singleplayerGame_finalWinner: null,
  singleplayerGame_currentView: 'Map',
  singleplayerGame_shouldEnd: false,
  singleplayerGame_singleplayerScore: null,
});
