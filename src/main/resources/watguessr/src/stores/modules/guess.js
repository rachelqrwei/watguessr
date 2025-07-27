const guessModule = {
  namespaced: true,

  actions: {
    submitGuess({ rootState, dispatch }, { user, guess }) {
      const scene = rootState.round.scene;
      if (!scene) return;

      const correct =
        scene.building === guess.building &&
        scene.floor === guess.floor;

      if (correct) {
        dispatch('round/setWinner', user, { root: true });
      }
    },
  },
};
