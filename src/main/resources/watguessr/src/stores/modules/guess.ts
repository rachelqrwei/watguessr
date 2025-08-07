// src/stores/modules/guess.ts
import type {Module, ActionTree, MutationTree} from 'vuex';
import type { RootState } from '../index';
import type {GameState} from "@/stores/modules/game.ts";

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
  async submitGuess({ rootState, dispatch, commit}, guess: any) {
    const scene = rootState.round.scene;
    if (!scene) return;

    console.log(guess);

    try {
      const response = await fetch(`http://localhost:5173/api/guess/calculate-points?roundId=${rootState.round.roundId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(guess),
      });

      if (!response.ok) {
        throw new Error('Failed to calculate points');
      }

      const points = await response.json();

      dispatch('round/setWinner', {winner: guess.user, score: points}, { root: true });

      return points;
    } catch (error) {
      console.error('Error calculating points:', error);
      return null;
    }
    // const correct =
    //   scene.building.includes(guess.building) &&
    //   scene.floor.includes(guess.floor);
    //
    // if (correct) {
    //   dispatch('round/setWinner', user, { root: true });
    // }
  },
};

const guessModule: Module<GuessState, RootState> = {
  namespaced: true,

  state: (): GuessState => ({}),

  actions,
};

export default guessModule;
