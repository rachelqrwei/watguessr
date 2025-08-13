// src/stores/modules/multiplayerGame/state.ts

export type PlayerStatus = 'idle' | 'loading' | 'playing' | 'ended';

export interface PlayerInfo {
  status: PlayerStatus;
  score: number;
}

export interface MultiplayerGameState {
  multiplayerGame_players: Record<string, PlayerInfo>; // key: player id, value: info object
  // gameMode: string; currentView: string; // TODO: separate shared game state?
  multiplayerGame_currentRound: number;
  multiplayerGame_maxRounds: number;
  multiplayerGame_finalWinner: string | null;
  multiplayerGame_shouldEnd: boolean;
}

export const state = (): MultiplayerGameState => ({
  multiplayerGame_players: {},
  multiplayerGame_currentRound: 1,
  multiplayerGame_maxRounds: 5,
  multiplayerGame_finalWinner: null,
  multiplayerGame_shouldEnd: false
});
