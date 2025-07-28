export interface LeaderboardUser {
  id: string
  username: string
  elo: number
  streak: number
  gamesPlayed: number
  gamesWon: number
  gamesLost: number
}

export interface LeaderboardRequest {
  searchTerm?: string
  sortBy?: string
  limit?: number
  offset?: number
}

export interface QueryResults {
  results: LeaderboardUser[]
}

interface LeaderboardState {
  leaderboardData: QueryResults | null
  loading: boolean
  error: string | null
  currentQuery: LeaderboardRequest
}

const leaderboardModule = {
  namespaced: true,

  state: (): LeaderboardState => ({
    leaderboardData: null,
    loading: false,
    error: null,
    currentQuery: {
      searchTerm: '',
      sortBy: 'elo',
      limit: 20,
      offset: 0
    }
  }),

  mutations: {
    SET_LEADERBOARD_DATA(state: LeaderboardState, data: QueryResults) {
      state.leaderboardData = data;
    },
    SET_LOADING(state: LeaderboardState, loading: boolean) {
      state.loading = loading;
    },
    SET_ERROR(state: LeaderboardState, error: string | null) {
      state.error = error;
    },
    SET_CURRENT_QUERY(state: LeaderboardState, query: LeaderboardRequest) {
      state.currentQuery = { ...state.currentQuery, ...query };
    },
    RESET_QUERY(state: LeaderboardState) {
      state.currentQuery = {
        searchTerm: '',
        sortBy: 'elo',
        limit: 20,
        offset: 0
      };
    },
    CLEAR_ERROR(state: LeaderboardState) {
      state.error = null;
    }
  },

  actions: {
    async fetchLeaderboard({ commit, state }: any, query: LeaderboardRequest = {}) {
      commit('SET_LOADING', true);
      commit('SET_ERROR', null);
      
      try {
        // Merge the provided query with current state
        const mergedQuery = { ...state.currentQuery, ...query };
        
        const params = new URLSearchParams();
        
        if (mergedQuery.searchTerm) params.append('searchTerm', mergedQuery.searchTerm);
        if (mergedQuery.sortBy) params.append('sortBy', mergedQuery.sortBy);
        if (mergedQuery.limit) params.append('limit', mergedQuery.limit.toString());
        if (mergedQuery.offset) params.append('offset', mergedQuery.offset.toString());

        const response = await fetch(`/api/user/leaderboard?${params.toString()}`);
        
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

    updateQuery({ commit }: any, updates: Partial<LeaderboardRequest>) {
      commit('SET_CURRENT_QUERY', updates);
    },

    resetQuery({ commit }: any) {
      commit('RESET_QUERY');
    },

    clearError({ commit }: any) {
      commit('CLEAR_ERROR');
    },

    nextPage({ state, commit, dispatch }: any) {
      const limit = state.currentQuery.limit || 20;
      const newOffset = (state.currentQuery.offset || 0) + limit;
      const newQuery = { ...state.currentQuery, offset: newOffset };
      commit('SET_CURRENT_QUERY', { offset: newOffset });
      dispatch('fetchLeaderboard', newQuery);
    },

    previousPage({ state, commit, dispatch }: any) {
      const limit = state.currentQuery.limit || 20;
      const newOffset = Math.max(0, (state.currentQuery.offset || 0) - limit);
      const newQuery = { ...state.currentQuery, offset: newOffset };
      commit('SET_CURRENT_QUERY', { offset: newOffset });
      dispatch('fetchLeaderboard', newQuery);
    },

    goToPage({ state, commit, dispatch }: any, page: number) {
      const limit = state.currentQuery.limit || 20;
      const newOffset = (page - 1) * limit;
      const newQuery = { ...state.currentQuery, offset: newOffset };
      commit('SET_CURRENT_QUERY', { offset: newOffset });
      dispatch('fetchLeaderboard', newQuery);
    }
  },

  getters: {
    leaderboard: (state: LeaderboardState) => state.leaderboardData?.results || [],
    totalResults: (state: LeaderboardState) => state.leaderboardData?.results.length || 0,
    isLoading: (state: LeaderboardState) => state.loading,
    hasError: (state: LeaderboardState) => state.error !== null,
    error: (state: LeaderboardState) => state.error,
    currentQuery: (state: LeaderboardState) => state.currentQuery,
    currentPage: (state: LeaderboardState) => Math.floor((state.currentQuery.offset || 0) / (state.currentQuery.limit || 20)) + 1
  }
};

export default leaderboardModule; 