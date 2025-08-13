export interface User {
  id: string;
  email: string;
  username: string;
  elo: number;
}

export interface UserState {
  users: User[];
  currentUser: User | null;
  loading: boolean;
  error: string | null;
}

export const state = (): UserState => ({
  users: [],
  currentUser: null,
  loading: false,
  error: null
});
