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
  emailAddress?: string;
  elo?: number;
  streak?: number;
}
