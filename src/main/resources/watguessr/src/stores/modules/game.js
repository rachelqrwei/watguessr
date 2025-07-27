const gameModule = {
  namespaced: true,

  state: {
    gameId: null,
    gameMode: '',
    status: 'idle', // 'loading' | 'playing' | 'ended'
    currentRound: 1,
    maxRounds: 5,
    scores: {}, // { [username]: wins }
    winner: null, // final game winner
  },

  mutations: {
    SET_GAME_ID(state, gameId) {
      state.gameId = gameId;
    },
    SET_GAME_MODE(state, mode) {
      state.gameMode = mode;
    },
    SET_STATUS(state, status) {
      state.status = status;
    },
    INCREMENT_ROUND(state) {
      state.currentRound++;
    },
    ADD_SCORE(state, username) {
      if (!state.scores[username]) {
        state.scores[username] = 0;
      }
      state.scores[username]++;
    },
    SET_FINAL_WINNER(state, winner) {
      state.winner = winner;
      state.status = 'ended';
    },
    RESET_GAME(state) {
      state.gameId = null;
      state.status = 'idle';
      state.currentRound = 1;
      state.scores = {};
      state.winner = null;
    },
  },

  actions: {
    createSingleplayerGame({ commit }) {
      commit('RESET_GAME');
      commit('SET_STATUS', 'loading');
      commit('SET_GAME_MODE', 'Singleplayer');

      // Assume an API or local ID generation
      const gameId = Date.now().toString();
      commit('SET_GAME_ID', gameId);
      commit('SET_STATUS', 'playing');
    },

    recordRoundWinner({ state, commit, dispatch }, username) {
      commit('ADD_SCORE', username);

      if (state.currentRound >= state.maxRounds) {
        // Game is over, determine final winner
        const winner = Object.entries(state.scores).sort((a, b) => b[1] - a[1])[0][0];
        commit('SET_FINAL_WINNER', winner);
      } else {
        commit('INCREMENT_ROUND');
        dispatch('round/resetRound', null, { root: true });

        // Dispatch logic to load next scene
        const newScene = generateNextScene(); // Replace with real logic
        dispatch('round/startRound', newScene, { root: true });
      }
    },
  },

  getters: {
    gameId: (state) => state.gameId,
    currentRound: (state) => state.currentRound,
    gameStatus: (state) => state.status,
    finalWinner: (state) => state.winner,
    scores: (state) => state.scores,
  },
};
