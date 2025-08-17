// src/store/modules/leaderboard/actions.ts
import type { LeaderboardRequest, LeaderboardState, QueryResults } from './types';

export const actions = {
  async fetchLeaderboard(
    { commit, state, rootGetters }: { commit: Function; state: LeaderboardState; rootGetters: any },
    query: LeaderboardRequest = {}
  ) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const token = rootGetters['user/getToken'];
      if (!token) {
        throw new Error('Authentication required. Please log in.');
      }
      const mergedQuery = { ...state.currentQuery, ...query };
      const params = new URLSearchParams();

      if (mergedQuery.searchTerm) params.append('searchTerm', mergedQuery.searchTerm);
      if (mergedQuery.sortBy) params.append('sortBy', mergedQuery.sortBy);
      if (mergedQuery.limit !== undefined) params.append('limit', mergedQuery.limit.toString());
      if (mergedQuery.offset !== undefined) params.append('offset', mergedQuery.offset.toString());

      const response = await fetch('/api/user/leaderboard?sortBy=elo&limit=20&offset=0', {
        headers: { Authorization: `Bearer ${token}` }
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data: QueryResults = await response.json();
      commit('SET_LEADERBOARD_DATA', data);
      commit('SET_CURRENT_QUERY', mergedQuery);
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch leaderboard';
      commit('SET_ERROR', errorMessage);
      console.error('Error fetching leaderboard:', err);
    } finally {
      commit('SET_LOADING', false);
    }
  },

  updateQuery({ commit }: { commit: Function }, updates: Partial<LeaderboardRequest>) {
    commit('SET_CURRENT_QUERY', updates);
  },

  resetQuery({ commit }: { commit: Function }) {
    commit('RESET_QUERY');
  },

  clearError({ commit }: { commit: Function }) {
    commit('CLEAR_ERROR');
  },

  nextPage({ state, commit, dispatch }: { state: LeaderboardState; commit: Function; dispatch: Function }) {
    const limit = state.currentQuery.limit || 20;
    const newOffset = (state.currentQuery.offset || 0) + limit;
    commit('SET_CURRENT_QUERY', { offset: newOffset });
    dispatch('fetchLeaderboard', { offset: newOffset });
  },

  previousPage({ state, commit, dispatch }: { state: LeaderboardState; commit: Function; dispatch: Function }) {
    const limit = state.currentQuery.limit || 20;
    const newOffset = Math.max(0, (state.currentQuery.offset || 0) - limit);
    commit('SET_CURRENT_QUERY', { offset: newOffset });
    dispatch('fetchLeaderboard', { offset: newOffset });
  },

  goToPage({ commit, dispatch, state }: { commit: Function; dispatch: Function; state: LeaderboardState }, page: number) {
    const limit = state.currentQuery.limit || 20;
    const newOffset = (page - 1) * limit;
    commit('SET_CURRENT_QUERY', { offset: newOffset });
    dispatch('fetchLeaderboard', { offset: newOffset });
  },
};
