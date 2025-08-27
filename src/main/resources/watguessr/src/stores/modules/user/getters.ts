import type { UserState } from './state';

export const getters = {
  getCurrentUser: (state: UserState) => state.currentUser,
  getUsers: (state: UserState) => state.users,
  getLoading: (state: UserState) => state.loading,
  getError: (state: UserState) => state.error,
  getToken: (state: UserState) => state.token,
  isAuthenticated: (state: UserState) => state.isAuthenticated,
  getUserById: (state: UserState) => (id: string) =>
    state.users.find(user => user.id === id),
  showLogin: (state: UserState) => state.showLogin,
  showSignUp: (state: UserState) => state.showSignUp,
  showWelcome: (state: UserState) => state.showWelcome,
  logoutReason: (state: UserState) => state.logoutReason
};
