import type { UserState } from './state';

export const getters = {
  currentUser: (state) => state.currentUser,
  getUserById: (state: UserState) => (id: string) =>
    state.users.find(user => user.id === id),

  isAuthenticated: (state: UserState) =>
    state.currentUser !== null,

  topUsers: (state: UserState) =>
    [...state.users].sort((a, b) => b.elo - a.elo).slice(0, 10),

  userName: (state: UserState) =>
    state.currentUser?.username || 'Guest'
};
