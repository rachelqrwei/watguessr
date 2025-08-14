// src/stores/modules/multiplayerGame/state.ts

export type PlayerStatus = 'idle' | 'loading' | 'playing' | 'ended';

export interface PlayerInfo {
  status: PlayerStatus;
  score: number;
}

export interface MultiplayerGameState {
  multiplayerGame_gameId: string;
  multiplayerGame_players: Record<string, PlayerInfo>; // key: player id, value: info object
  multiplayerGame_currentRound: number;
  multiplayerGame_maxRounds: number;
  multiplayerGame_finalWinner: string | null;
  multiplayerGame_shouldEnd: boolean;
}

export const state = (): MultiplayerGameState => ({
  multiplayerGame_gameId: '',
  multiplayerGame_players: {},
  multiplayerGame_currentRound: 1,
  multiplayerGame_maxRounds: 5,
  multiplayerGame_finalWinner: null,
  multiplayerGame_shouldEnd: false,
});
