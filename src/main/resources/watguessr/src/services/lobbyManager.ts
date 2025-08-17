import type { LobbyDto } from '../types/index.d.ts';

const API_BASE_URL = `${import.meta.env.VITE_API_BASE_URL}/api/game`;

export interface CreateLobbyRequest {
  gameMode: string;
  lobbyName: string;
  isPrivate: boolean;
  maxPlayers: number;
  multiplayerTimer: number;
  multiplayerRoundCount: number;
  creatorId: string;
}

export interface JoinLobbyRequest {
  lobbyCode: string;
  userId: string;
}

export class LobbyManager {
  /**
   * Create a new lobby
   */
  static async createLobby(request: CreateLobbyRequest): Promise<LobbyDto> {
    const response = await fetch(`${API_BASE_URL}/lobby/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`Failed to create lobby: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Get all public lobbies
   */
  static async getPublicLobbies(): Promise<LobbyDto[]> {
    const response = await fetch(`${API_BASE_URL}/lobby/public`);

    if (!response.ok) {
      throw new Error(`Failed to get public lobbies: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Get a specific lobby by ID
   */
  static async getLobbyById(lobbyId: string): Promise<LobbyDto> {
    const response = await fetch(`${API_BASE_URL}/lobby/${lobbyId}`);

    if (!response.ok) {
      throw new Error(`Failed to get lobby: ${response.statusText}`);
    }

    return response.json();
  }

  /**
   * Join a lobby using a lobby code
   */
  static async joinLobby(request: JoinLobbyRequest): Promise<LobbyDto> {
    const response = await fetch(`${API_BASE_URL}/lobby/join`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(request),
    });

    if (!response.ok) {
      throw new Error(`Failed to join lobby: ${response.statusText}`);
    }

    return response.json();
  }
}
