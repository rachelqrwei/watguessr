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
    
    // Store token in localStorage for persistence
    localStorage.setItem('jwt_token', token);
  },

  CLEAR_AUTH(state: UserState) {
    state.currentUser = null;
    state.token = null;
    state.isAuthenticated = false;
    
    // Remove token from localStorage
    localStorage.removeItem('jwt_token');
  },

  SET_LOADING(state: UserState, loading: boolean) {
    state.loading = loading;
  },

  SET_ERROR(state: UserState, error: string | null) {
    state.error = error;
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
  }
};

