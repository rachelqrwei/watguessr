import type { UserState, User } from './state';

export const mutations = {
  SET_CURRENT_USER(state: UserState, user: User) {
    state.currentUser = user;

    // Store user data in localStorage for persistence
    if (user) {
      localStorage.setItem('user_data', JSON.stringify(user));
    }
  },

  SET_TOKEN(state: UserState, token: string) {
    state.token = token;
    state.isAuthenticated = true;
  },

  CLEAR_AUTH(state: UserState) {
    state.currentUser = null;
    state.token = null;
    state.isAuthenticated = false;
    state.logoutReason = null;

    // Remove token from localStorage
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_data');

    // Call backend logout to clear HTTP-only cookie
    fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/logout`, { method: 'POST', credentials: 'include' });
  },

  SET_LOADING(state: UserState, loading: boolean) {
    state.loading = loading;
  },

  SET_ERROR(state: UserState, error: string | null) {
    state.error = error;
  },

  SET_AUTHENTICATED(state: UserState, authenticated: boolean) {
    state.isAuthenticated = authenticated;
  },

  INITIALIZE_AUTH(state: UserState) {
    // Check localStorage for existing token on app startup
    const token = localStorage.getItem('jwt_token');
    if (token) {
      state.token = token;
      state.isAuthenticated = true;

      // Try to get user data from localStorage if available
      const userData = localStorage.getItem('user_data');
      if (userData) {
        try {
          state.currentUser = JSON.parse(userData);
        } catch (e) {
          console.warn('Failed to parse stored user data');
        }
      }
    }
  },

  OPEN_LOGIN(state: UserState, reason?: string) {
    state.showLogin = true;
    state.showSignUp = false;
    state.logoutReason = reason || null;
  },
  CLOSE_LOGIN(state: UserState) {
    state.showLogin = false;
    state.logoutReason = null;
  },
  OPEN_SIGNUP(state: UserState) {
    state.showSignUp = true;
    state.showLogin = false;
    state.logoutReason = null;
  },
  CLOSE_SIGNUP(state: UserState) {
    state.showSignUp = false;
  }
};

