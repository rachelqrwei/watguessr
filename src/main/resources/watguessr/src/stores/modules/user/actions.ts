import type { UserState, User } from './state';

export const actions = {

  async fetchUserById({ state, commit }: { state: UserState; commit: any }, id: string) {
    if (!id || id === 'undefined') return null;
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${id}`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
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

  async fetchUserSettings(_ctx: { state: UserState }, userId: string) {
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${userId}/settings`, {
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });
      if (!response.ok) throw new Error(`Failed to fetch user settings: ${response.status}`);
      return await response.json();
    } catch (err) {
      console.error('Failed to fetch user settings', err);
      throw err;
    }
  },

  async fetchLeaderboardForUserId({ state }: { state: UserState }, userId: string) {
    if (!userId || userId === 'undefined') return null;
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${userId}/leaderboard`, {
        credentials: "include"
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
    if (!userId || userId === 'undefined') return { results: [], hasNext: false };

    state.loading = true;
    state.error = null;
    try {
      const params = new URLSearchParams();
      params.set('offset', String(offset));
      params.set('limit', String(limit));

      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${userId}/match-history?${params.toString()}`, {
        credentials: "include"
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
        credentials: "include",
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
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/login`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        const message = errorData.message || errorData.error || 'Login failed';
        throw new Error(message);
      }

      const authResponse = await response.json();
      const user = authResponse;

      commit('SET_CURRENT_USER', user);
      commit('SET_AUTHENTICATED', true);

      return user;
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
        credentials: "include",
        headers: { "Content-Type": "application/json" },
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
  async initializeAuth({ commit }: { state: UserState; commit: any }) {
    try {
      const res = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/auth/me`, {
        method: "GET",
        credentials: "include"
      });

      if (res.status === 401) {
        // User is not authenticated
        commit('SET_AUTHENTICATED', false);
        commit('SET_CURRENT_USER', null);
      } else if (res.ok) {
        // User is authenticated, parse the response
        const userData = await res.json();
        commit('SET_CURRENT_USER', userData);
        commit('SET_AUTHENTICATED', true);
      } else {
        // Other error status
        commit('SET_AUTHENTICATED', false);
        commit('SET_CURRENT_USER', null);
      }
    } catch (err) {
      console.error('Failed to initialize auth', err);
      commit('SET_AUTHENTICATED', false);
      commit('SET_CURRENT_USER', null);
    }
  },

  getToken({ state }: { state: UserState }) {
    return state.token;
  },

  isAuthenticated({ state }: { state: UserState }) {
    return state.isAuthenticated;
  },

  async changePassword({ commit }: { state: UserState; commit: any }, payload: { emailAddress: string; newPassword: string }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const { emailAddress, newPassword } = payload;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/change-password?emailAddress=${encodeURIComponent(emailAddress)}&newPassword=${encodeURIComponent(newPassword)}`, {
        method: "PUT",
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error('Failed to change password');
      }

      return await response.text();
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async changeUsername({ commit }: { state: UserState; commit: any }, payload: { emailAddress: string; newUsername: string }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const { emailAddress, newUsername } = payload;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/change-username?emailAddress=${encodeURIComponent(emailAddress)}&newUsername=${encodeURIComponent(newUsername)}`, {
        method: "PUT",
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error('Failed to change username');
      }

      return await response.text();
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async deleteUser({ commit }: { state: UserState; commit: any }, payload: { emailAddress: string }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const { emailAddress } = payload;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/delete-user?emailAddress=${encodeURIComponent(emailAddress)}`, {
        method: "DELETE",
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error('Failed to delete user');
      }

      return await response.text();
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Unknown error');
      return null;
    } finally {
      commit('SET_LOADING', false);
    }
  },

  async sendOtpAndRedirect({ commit }: { state: UserState; commit: any }, payload: { email: string; username: string }) {
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/auth/send-otp?to=${encodeURIComponent(payload.email)}`;
      const response = await fetch(url, { method: 'POST' });
      if (!response.ok) throw new Error('Failed to send OTP');

      const otpUrl = `${window.location.origin}/?email=${encodeURIComponent(payload.email)}&username=${encodeURIComponent(payload.username)}&action=send-otp`;
      window.location.href = otpUrl;

      return { success: true };
    } catch (err) {
      commit('SET_ERROR', err instanceof Error ? err.message : 'Failed to send OTP');
      throw err;
    }
  },
};
