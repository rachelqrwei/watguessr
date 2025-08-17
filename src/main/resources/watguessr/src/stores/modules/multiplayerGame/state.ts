// src/stores/modules/multiplayerGame/state.ts

export type PlayerStatus = 'idle' | 'loading' | 'playing' | 'ended' | 'ready' | 'completed' | 'disconnected';

export interface PlayerInfo {
  status: PlayerStatus;
  score: number;
  username: string;
  lastSeen?: number; // timestamp when player was last seen
}

export interface MultiplayerGameState {
  multiplayerGame_gameId: string;
  multiplayerGame_players: Record<string, PlayerInfo>; // key: player id, value: info object
  multiplayerGame_currentRound: number;
  multiplayerGame_maxRounds: number;
  multiplayerGame_timer: number;
  multiplayerGame_finalWinner: string | null;
  multiplayerGame_shouldEnd: boolean;
  disconnectionCheckInterval?: number; // interval ID for disconnection checking
}

export const state = (): MultiplayerGameState => ({
  multiplayerGame_gameId: '',
  multiplayerGame_players: {},
  multiplayerGame_currentRound: 1,
  multiplayerGame_maxRounds: 5,
  multiplayerGame_timer: 30000,
  multiplayerGame_finalWinner: null,
  multiplayerGame_shouldEnd: false,
});
