import type { UserState, User } from './state';

export const actions = {

  async fetchUserById({ state, commit }: { state: UserState; commit: any }, id: string) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${id}`, {
        headers: {
          'Authorization': `Bearer ${state.token}`,
          'Content-Type': 'application/json'
        }
      });
      if (!response.ok) throw new Error('Failed to fetch user');
      const user = await response.json();
      const index = state.users.findIndex(u => u.id === id);
      if (index >= 0) {
        state.users[index] = user;
      } else {
        state.users.push(user);
      }
      return user;
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async fetchLeaderboardForUserId({ state }: { state: UserState }, userId: string) {
    if (!userId) return null;
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${userId}/leaderboard`, {
        headers: {
          'Authorization': `Bearer ${state.token}`,
        }
      });
      if (!response.ok) throw new Error(`Failed to fetch leaderboard for user: ${response.status}`);
      return await response.json();
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Failed to fetch leaderboard';
      return null;
    } finally {
      state.loading = false;
    }
  },

  async fetchUserMatchHistory(
    { state }: { state: UserState },
    payload: { userId: string; offset: number; limit: number }
  ) {
    const { userId, offset, limit } = payload;
    if (!userId) return { results: [], hasNext: false };

    state.loading = true;
    state.error = null;
    try {
      const params = new URLSearchParams();
      params.set('offset', String(offset));
      params.set('limit', String(limit));

      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${userId}/match-history?${params.toString()}`, {
        headers: {
          'Authorization': `Bearer ${state.token}`,
        }
      });

      if (!res.ok) throw new Error(`Failed to fetch match history: ${res.status}`);

      const data: { results: any[] } = await res.json();
      const results = data.results || [];
      return { results, hasNext: (results.length || 0) === limit };
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Failed to fetch match history';
      return { results: [], hasNext: false };
    } finally {
      state.loading = false;
    }
  },

  async signUpUser({ commit }: { state: UserState; commit: any }, payload: { email: string; username: string; password: string }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        const message = errorData.message || errorData.error || 'Signup failed';
        throw new Error(message);
      }

      return response.text();
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Signup failed';
      commit('SET_ERROR', message);
      throw new Error(message);
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async login({ commit }: { state: UserState; commit: any }, payload: { username: string; password: string }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      // Use the auth endpoint instead of user endpoint for login
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/login`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        const message = errorData.message || errorData.error || 'Login failed';
        throw new Error(message);
      }

      const authResponse = await response.json();

      // Extract token and user from the response
      const { token, user } = authResponse;

      // Store token and user in state
      commit('SET_TOKEN', token);
      commit('SET_CURRENT_USER', user);

      return { token, user };
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Login failed';
      commit('SET_ERROR', message);
      throw new Error(message);
    } finally {
      commit('SET_LOADING', false);
    }
  },

  logout({ commit }: { state: UserState; commit: any }) {
    commit('CLEAR_AUTH');
  },

  async updateUser({ state, commit }: { state: UserState; commit: any }, payload: { id: string; updates: Partial<User> }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${payload.id}`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${state.token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload.updates)
      });
      if (!response.ok) throw new Error('Failed to update user');
      const updatedUser = await response.json();

      const index = state.users.findIndex(u => u.id === payload.id);
      if (index >= 0) {
        state.users[index] = updatedUser;
      }
      if (state.currentUser?.id === payload.id) {
        commit('SET_CURRENT_USER', updatedUser);
      }
      return updatedUser;
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async sendOtp({ commit }: { state: UserState; commit: any }, to: string) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/auth/send-otp?to=${encodeURIComponent(to)}`;
      const response = await fetch(url, { method: 'POST' });
      if (!response.ok) throw new Error('Failed to send OTP');
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async verifyOtp({ commit }: { state: UserState; commit: any }, payload: { email: string; submittedOtp: string }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/auth/verify-otp?email=${encodeURIComponent(payload.email)}&submittedOtp=${encodeURIComponent(payload.submittedOtp)}`;
      const response = await fetch(url, { method: 'POST' });
      if (!response.ok) throw new Error('Failed to verify user');
      return 'verified';
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },
  // Initialize authentication state on app startup
  initializeAuth({ commit }: { state: UserState; commit: any }) {
    commit('INITIALIZE_AUTH');
  },
  // Get stored token (useful for other parts of the app)
  getToken({ state }: { state: UserState }) {
    return state.token;
  },
  // Check if user is authenticated
  isAuthenticated({ state }: { state: UserState }) {
    return state.isAuthenticated;
  }
};
