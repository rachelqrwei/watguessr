// src/stores/modules/game/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { MultiplayerGameState } from './state';

export const actions: ActionTree<MultiplayerGameState, RootState> = {
  async multiplayerGame_createMultiplayerGame({ commit, dispatch }) {
    commit('MG_RESET_GAME');
    commit('MG_SET_STATUS', 'loading');

    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/create/multiplayer`);
    const gameId = await response.json();

    commit('MG_SET_GAME_ID', gameId);
    dispatch('round/startRound', { gameId }, { root: true });
    commit('MG_SET_STATUS', 'playing');
  },
  async multiplayerGame_restartGame({ state, commit, dispatch }) {
    await dispatch('multiplayerGame_createMultiplayerGame');
  },
  multiplayerGame_endCurrentRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    const currentUser = rootGetters['user/currentUser'];
    const userId = currentUser?.id;

    commit('MG_IMPLEMENT_ROUND_RESULT', { userId, roundResult: payload.roundResult });

    dispatch('multiplayerGame_checkMultiplayerState').then((shouldEnd) => {
      if (shouldEnd) {
        commit('MG_SET_STATUS', 'ended');
      }
    });
  },

  async multiplayerGame_endGame({ state, commit, rootGetters }) {
    try {
      const currentUser = rootGetters['user/currentUser'];
      const userId = currentUser?.id;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/multiplayer?gameId=${state.multiplayerGame_gameId}&userId=${userId}`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      await response.json();
      commit('MG_SET_STATUS', 'ended');
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }

  }
  // async multiplayerGame_checkMultiplayerState({ state, commit, dispatch, rootGetters }): Promise<boolean> {
  //   try {
  //     if (!state.multiplayerGame_players) return false;
  //     const currentUser = rootGetters['user/currentUser'];
  //     const userId = currentUser?.id;
  //
  //     const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/state/multiplayer?gameId=${state.multiplayerGame_gameId}&userId=${userId}`);
  //     if (!response.ok) {
  //       throw new Error(`HTTP error! status: ${response.status}`);
  //     }
  //     const dto = await response.json();
  //
  //     if (dto?.currentScore !== undefined) {
  //       commit('MG_IMPLEMENT_ROUND_RESULT', {dto.currentScore});
  //     }
  //
  //     const shouldEnd = !!(dto?.shouldEnd || dto?.isGameEnded);
  //     commit('SG_SET_SHOULD_END', shouldEnd);
  //
  //     if (shouldEnd) {
  //       await dispatch('singleplayerGame_endGame');
  //       return true;
  //     }
  //     return false;
  //   } catch (error) {
  //     console.error('Error checking singleplayer game state:', error);
  //     return false;
  //   }
  // },
};
