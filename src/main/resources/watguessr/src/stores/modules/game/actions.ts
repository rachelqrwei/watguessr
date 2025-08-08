// src/stores/modules/game/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { GameState } from './state';

export const actions: ActionTree<GameState, RootState> = {
  async createSingleplayerGame({ commit, dispatch }) {
    commit('RESET_GAME');
    commit('SET_STATUS', 'loading');
    commit('SET_GAME_MODE', 'Singleplayer');

    const response = await fetch('http://localhost:5173/api/game/create/singleplayer');
    const gameId = await response.json();

    commit('SET_GAME_ID', gameId);
    dispatch('round/startRound', { gameId }, { root: true });
    commit('SET_STATUS', 'playing');
  },

  endCurrentRound({ state, commit, dispatch }, payload: { username: string; roundResult: {points: number, distance: number} }) {
    //add score to user side (in-game score)
    commit('ADD_SCORE', payload);

    //change the game view to "round end"
    commit('CHANGE_VIEW', 'RoundEnd');

    if (state.currentRound >= state.maxRounds) {
      console.log("yo")
      //if current round is the last round
      //multiplayer: check who the winner is by comparing scores
      // const winner = Object.entries(state.scores).sort((a, b) => b[1] - a[1])[0][0];
      // // TODO: send API call to set final winner
      // commit('SET_FINAL_WINNER', winner);

      //singleplayer: just end the game (no need to find final winner)
      dispatch('endGame');
    } else {
      //if we still have rounds left
      //increment round number
    }
  },

  async endGame({ state, commit }) {
    //send api request to the backend to finish the singleplayer game
    //TODO: add multiplayer game end logic
    try {
      const response = await fetch(`http://localhost:5173/api/game/finish/singleplayer?gameId=${state.gameId}`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }

  }
};
