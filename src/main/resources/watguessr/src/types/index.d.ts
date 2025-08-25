// types/vuex.d.ts or src/types/vuex.d.ts
declare module 'vuex' {
  export * from 'vuex/types/index.d.ts';
}

export interface LobbyDto {
  id: string;
  lobbyName: string;
  gameMode: string;
  isPrivate: boolean;
  lobbyCode?: string;
  maxPlayers: number;
  currentPlayers: number;
  multiplayerTimer: number;
  multiplayerRoundCount: number;
  createdAt: string;
  players: UserDto[];
}

export interface UserDto {
  id: string;
  username: string;
  elo?: number;
  streak?: number;
}

export interface LeaderboardUser {
  id: string
  username: string
  elo: number
  streak: number
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
  rankedGamesPlayed: number
  rankedGamesWon: number
  rankedGamesLost: number
  createdAt: string
}

export interface QueryResults<T> {
  results: T[]
  totalCount: number
}
