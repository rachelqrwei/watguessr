export interface User {
  id: string;
  createdAt: string;
  username: string;
  emailAddress: string;
  elo: number;
  streak: number;
  lastLoginAt: string;
  verified: boolean;
}

export interface UserState {
  users: User[];
  currentUser: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
  showLogin: boolean;
  showSignUp: boolean;
  showWelcome: boolean;
  isAuthInitialized: boolean;
  isAuthInitializing: boolean;
  logoutReason: string | null;
  showWelcomeModal: boolean;
}

export const state = (): UserState => ({
  users: [],
  currentUser: null,
  token: null,
  isAuthenticated: false,
  loading: false,
  error: null,
  showLogin: false,
  showSignUp: false,
  showWelcome: false,
  isAuthInitialized: false,
  isAuthInitializing: false,
  logoutReason: null,
  showWelcomeModal: false
});
