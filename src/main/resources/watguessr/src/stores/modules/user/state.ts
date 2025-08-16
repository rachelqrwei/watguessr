export interface User {
  id: string;
  createdAt: string;
  username: string;
  emailAddress: string;
  elo: number;
  streak: number;
  lastLoginAt: string;
}

export interface UserState {
  users: User[];
  currentUser: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

export const state = (): UserState => ({
  users: [],
  currentUser: null,
  token: null,
  isAuthenticated: false,
  loading: false,
  error: null
});
