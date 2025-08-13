import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { singleplayerGameState } from './state';

export const actions: ActionTree<singleplayerGameState, RootState> = {
  async singleplayerGame_createSingleplayerGame({ commit, dispatch }) {
    commit('SG_RESET_GAME');
    commit('SG_SET_STATUS', 'loading');
    commit('SG_SET_GAME_MODE', 'Singleplayer');
    commit('SG_CHANGE_VIEW', 'Map'); //TODO: change to image on start after testing

    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/create/singleplayer`);
    const gameId = await response.json();


    commit('SG_SET_GAME_ID', gameId);
    dispatch('round/startRound', { gameId }, { root: true });
    commit('SG_SET_STATUS', 'playing');
  },

  async singleplayerGame_restartGame({ state, commit, dispatch }) {
    const mode = state.singleplayerGame_gameMode || 'Singleplayer';
    if (mode === 'Singleplayer') {
      await dispatch('singleplayerGame_createSingleplayerGame');
    }
  },

  singleplayerGame_endCurrentRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    const currentUser = rootGetters['user/currentUser'];
    const userId = currentUser?.id;

    commit('SG_ADD_SINGLEPLAYER_PENALTY', { userId, roundResult: payload.roundResult });

    dispatch('singleplayerGame_checkSingleplayerState').then((shouldEnd) => {
      if (shouldEnd) {
        commit('SG_SET_STATUS', 'ended');
      } else {
        commit('SG_CHANGE_VIEW', 'RoundEnd');
      }
    });
  },

  async singleplayerGame_checkSingleplayerState({ state, commit, dispatch, rootGetters }): Promise<boolean> {
    try {
      if (!state.singleplayerGame_gameId) return false;
      const currentUser = rootGetters['user/currentUser'];
      const userId = currentUser?.id;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/state/singleplayer?gameId=${state.singleplayerGame_gameId}&userId=${userId}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const dto = await response.json();

      if (dto?.currentScore !== undefined) {
        commit('SG_SET_SINGLEPLAYER_SCORE', dto.currentScore);
      }

      const shouldEnd = !!(dto?.shouldEnd || dto?.isGameEnded);
      commit('SG_SET_SHOULD_END', shouldEnd);

      if (shouldEnd) {
        await dispatch('singleplayerGame_endGame');
        return true;
      }
      return false;
    } catch (error) {
      console.error('Error checking singleplayer game state:', error);
      return false;
    }
  },

  async singleplayerGame_endGame({ state, commit, rootGetters }) {
    try {
      const currentUser = rootGetters['user/currentUser'];
      const userId = currentUser?.id;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/singleplayer?gameId=${state.singleplayerGame_gameId}&userId=${userId}`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      await response.json();
      commit('SG_SET_STATUS', 'ended');
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }

  }
};
