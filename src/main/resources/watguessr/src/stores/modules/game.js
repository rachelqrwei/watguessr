// store/modules/user.js
export default {
  namespaced: true,
  state: () => ({
    currentLocation: {
      building: null,
      floor: null,
    },
    userGuess: {
      building: '',
      floor: '',
    },
    result: null, // 'correct', 'incorrect', or null
    score: 0,
    round: 1,
  }),
  getters: {
    getCurrentLocation: state => state.currentLocation,
    getUserGuess: state => state.userGuess,
    getResult: state => state.result,
    getScore: state => state.score,
    getRound: state => state.round,
  },
  mutations: {
    SET_LOCATION(state, { building, floor }) {
      state.currentLocation = { building, floor };
    },
    SET_USER_GUESS(state, { building, floor }) {
      state.userGuess = { building, floor };
    },
    SET_RESULT(state, result) {
      state.result = result;
    },
    INCREMENT_SCORE(state) {
      state.score += 1;
    },
    NEXT_ROUND(state) {
      state.round += 1;
      state.userGuess = { building: '', floor: '' };
      state.result = null;
    },
    RESET_GAME(state) {
      state.score = 0;
      state.round = 1;
      state.result = null;
      state.userGuess = { building: '', floor: '' };
      state.currentLocation = { building: null, floor: null };
    },
  },
  actions: {
    evaluateGuess({ commit, state }) {
      const isCorrect =
        state.userGuess.building === state.currentLocation.building &&
        state.userGuess.floor === state.currentLocation.floor;

      commit('SET_RESULT', isCorrect ? 'correct' : 'incorrect');
      if (isCorrect) commit('INCREMENT_SCORE');
    },

    startNextRound({ commit }, newLocation) {
      commit('SET_LOCATION', newLocation);
      commit('NEXT_ROUND');
    },

    resetGame({ commit }) {
      commit('RESET_GAME');
    },
  }
};
