import type { UserState, User } from './state';

export const actions = {
  async fetchUsers({ state }: { state: UserState }) {
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/user`);
      if (!response.ok) throw new Error('Failed to fetch users');
      state.users = await response.json();
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Unknown error';
    } finally {
      state.loading = false;
    }
  },

  async fetchUserById({ state }: { state: UserState }, id: string) {
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/user/${id}`);
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
      state.error = err instanceof Error ? err.message : 'Unknown error';
      return null;
    } finally {
      state.loading = false;
    }
  },

  async signUpUser({ state }: { state: UserState }, payload: { email: string; username: string; password: string }) {
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/signup`, {
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
      state.error = message;
      throw new Error(message);
    } finally {
      state.loading = false;
    }
  },

  async login({ state }: { state: UserState }, payload: { username: string; password: string }) {
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/login`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorData = await response.json();
        const message = errorData.message || errorData.error || 'Login failed';
        throw new Error(message);
      }

      const user = await response.json();
      state.currentUser = user;
      return user;
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Login failed';
      throw new Error(state.error);
    } finally {
      state.loading = false;
    }
  },

  logout({ state }: { state: UserState }) {
    state.currentUser = null;
  },

  async updateUser({ state }: { state: UserState }, payload: { id: string; updates: Partial<User> }) {
    state.loading = true;
    state.error = null;
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${payload.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload.updates)
      });

      if (!response.ok) throw new Error('Failed to update user');
      const updatedUser = await response.json();

      const index = state.users.findIndex(u => u.id === payload.id);
      if (index >= 0) {
        state.users[index] = updatedUser;
      }
      if (state.currentUser?.id === payload.id) {
        state.currentUser = updatedUser;
      }
      return updatedUser;
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Unknown error';
      return null;
    } finally {
      state.loading = false;
    }
  },

  async sendOtp({ state }: { state: UserState }, to: string) {
    state.loading = true;
    state.error = null;
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/user/send-otp?to=${encodeURIComponent(to)}`;
      const response = await fetch(url, { method: 'POST' });
      if (!response.ok) throw new Error('Failed to send OTP');
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Unknown error';
    } finally {
      state.loading = false;
    }
  },

  async verifyOtp({ state }: { state: UserState }, payload: { email: string; submittedOtp: string }) {
    state.loading = true;
    state.error = null;
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/user/verify-otp?email=${encodeURIComponent(payload.email)}&submittedOtp=${encodeURIComponent(payload.submittedOtp)}`;
      const response = await fetch(url, { method: 'POST' });
      if (!response.ok) throw new Error('Failed to verify user');
      return 'verified';
    } catch (err) {
      state.error = err instanceof Error ? err.message : 'Unknown error';
      return null;
    } finally {
      state.loading = false;
    }
  }
};
