// src/stores/modules/guess.ts
import type { Module, ActionTree } from 'vuex';
import type { RootState } from '../index';

interface GuessPayload {
  user: string;
  guess: {
    building: string;
    floor: string;
  };
}

export interface GuessState {} // No local state yet

// Define actions with proper typing
const actions: ActionTree<GuessState, RootState> = {
  submitGuess({ rootState, dispatch }, { user, guess }: GuessPayload) {
    const scene = rootState.round.scene;
    if (!scene) return;

    const correct =
      scene.building === guess.building &&
      scene.floor === guess.floor;

    if (correct) {
      dispatch('round/setWinner', user, { root: true });
    }
  },
};

const guessModule: Module<GuessState, RootState> = {
  namespaced: true,

  state: (): GuessState => ({}),

  actions,
};

export default guessModule;
