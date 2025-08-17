// src/stores/modules/game/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { MultiplayerGameState } from './state';
import { connectToMultiplayerGame, disconnectFromMultiplayerGame, sendPlayerProgress, sendPlayerReady } from '../../../services/multiplayerGameWebSocket';

export const actions: ActionTree<MultiplayerGameState, RootState> = {
  async multiplayerGame_createMultiplayerGame({ state, commit, dispatch , rootGetters}) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;
    const username = currentUser?.username || 'Player';

    // Reset the game and initialize the current player
    commit('MG_RESET_GAME', { userId, username });
    
    // Now we can safely set the status since the player exists
    commit('MG_SET_STATUS', { playerId: userId, status: 'loading' });
    commit('gameInfo/SET_GAME_MODE', 'multiplayer', {root: true});
    commit('gameInfo/SET_CURRENT_VIEW', 'Map', {root: true});

    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/create/multiplayer?roundCount=${state.multiplayerGame_maxRounds}&timer=${state.multiplayerGame_timer}`);
    const gameId = await response.json();

    commit('MG_SET_GAME_ID', gameId);

    // Connect to WebSocket for real-time updates
    connectToMultiplayerGame(gameId);

    dispatch('round/startRound', { gameId }, { root: true });
    commit('MG_SET_STATUS', { playerId: userId, status: 'playing' });
  },
  async multiplayerGame_restartGame({ state, commit, dispatch }) {
    await dispatch('multiplayerGame_createMultiplayerGame');
  },
  multiplayerGame_endCurrentRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    console.log('🎯 Multiplayer round ended:', { userId, roundResult: payload.roundResult });
    console.log('🎮 Current multiplayer players:', state.multiplayerGame_players);

    // Ensure player exists in the game state
    if (!state.multiplayerGame_players[userId]) {
      console.warn('⚠️ Player not found in multiplayer game state, initializing...');
      commit('MG_SET_PLAYERS', {
        ...state.multiplayerGame_players,
        [userId]: { score: 0, status: 'playing', username: 'Player' }
      });
    }

    commit('MG_IMPLEMENT_ROUND_RESULT', { playerId: userId, roundResult: payload.roundResult });

    // Send progress update via WebSocket
    const currentScore = state.multiplayerGame_players[userId]?.score || 0;
    console.log('📊 Updated score for player:', userId, 'new score:', currentScore);
    sendPlayerProgress(state.multiplayerGame_gameId, userId, currentScore, 'ended');

    // Always go to RoundEnd view after submitting a guess in multiplayer
    commit('gameInfo/SET_CURRENT_VIEW', 'RoundEnd', {root: true});
  },

  async multiplayerGame_endGame({ state, commit, rootGetters }) {
    try {
      const currentUser = rootGetters['user/getCurrentUser'];
      const userId = currentUser?.id;
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/game/finish/multiplayer?gameId=${state.multiplayerGame_gameId}&userId=${userId}`, {
        method: 'POST'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      await response.json();
      commit('MG_SET_STATUS', {playerId: userId, status: 'ended'});

      // Disconnect from WebSocket
      disconnectFromMultiplayerGame();
    } catch (error) {
      console.error('Error finishing game:', error);
      throw error;
    }
  },

  // Send player ready status
  multiplayerGame_setPlayerReady({ state, rootGetters }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    if (userId && state.multiplayerGame_gameId) {
      sendPlayerReady(state.multiplayerGame_gameId, userId);
    }
  },

  // Send player status update
  multiplayerGame_updatePlayerStatus({ state, rootGetters }, { status }: { status: string }) {
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    if (userId && state.multiplayerGame_gameId) {
      const currentScore = state.multiplayerGame_players[userId]?.score || 0;
      sendPlayerProgress(state.multiplayerGame_gameId, userId, currentScore, status);
    }
  },

  // Disconnect from WebSocket when leaving
  multiplayerGame_disconnect() {
    disconnectFromMultiplayerGame();
  }
  // async multiplayerGame_checkMultiplayerState({ state, commit, dispatch, rootGetters }): Promise<boolean> {
  //   try {
  //     if (!state.multiplayerGame_players) return false;
  //     const currentUser = rootGetters['user/getCurrentUser'];
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
