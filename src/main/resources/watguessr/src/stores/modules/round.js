const roundModule = {
  namespaced: true,

  state: {
    scene: null,
    winner: null,
  },

  mutations: {
    SET_SCENE(state, scene) {
      state.scene = scene;
    },
    SET_WINNER(state, winner) {
      state.winner = winner;
    },
    RESET_ROUND(state) {
      state.scene = null;
      state.winner = null;
    },
  },

  actions: {
    setWinner({ commit, dispatch }, winner) {
      commit('SET_WINNER', winner);
      dispatch('game/recordRoundWinner', winner, { root: true });
    },

    startRound({ commit }, scene) {
      commit('SET_SCENE', scene);
    },

    resetRound({ commit }) {
      commit('RESET_ROUND');
    },
  },
};
