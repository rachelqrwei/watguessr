// src/stores/modules/round/actions.ts
import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { RoundState } from './state';

export const actions: ActionTree<RoundState, RootState> = {
  async startRound({ state, commit, rootGetters }, { gameId }): Promise<RoundState> {
    //reset data from prev round (if any) - IMPORTANT for clearing correct answer marker on mapbox
    commit('RESET_ROUND');
    commit('guess/RESET_GUESS', null, {root: true});

    if (!gameId) throw new Error('Game ID not found');

    // Check game mode - don't create individual rounds for ranked games
    const gameMode = rootGetters['gameInfo/getGameMode'];
    if (gameMode === 'ranked') {
      // For ranked games, the round ID should already be set by the WebSocket round-start event
      // Just return the current round ID if it exists
      if (state.roundId) {
        return state.roundId;
      } else {
        throw new Error('No round ID available for ranked game. Wait for the round-start event from the server.');
      }
    }

    const token = rootGetters['user/getToken'];
    const currentUser = rootGetters['user/getCurrentUser'];

    if (!currentUser || !currentUser.id) {
      throw new Error('User ID not found');
    }

    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/round/create?gameId=${gameId}&userId=${currentUser.id}`, {
      credentials: "include"
    });

    if (!response.ok) {
      throw new Error('Failed to create round');
    }

    const roundId: any = await response.json();
    commit('SET_ROUND_ID', roundId);

    // fetch only the image for the round and store it
    try {
      const imgResp = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/scene/image?roundId=${roundId}`);
      if (imgResp.ok) {
        const blob = await imgResp.blob();
        const imageUrl = URL.createObjectURL(blob);
        commit('SET_IMAGE_URL', imageUrl || null);
      } else {
        commit('SET_IMAGE_URL', null);
      }
    } catch (_) {
      commit('SET_IMAGE_URL', null);
    }

    return roundId;
  },

  async fetchCorrectAnswer({ state, commit, rootGetters }) {
    const roundId = state.roundId;
    if (!roundId) {
      console.error('Cannot fetch correct answer: No roundId found in store');
      return;
    }

    // Get current user ID
    const currentUser = rootGetters['user/getCurrentUser'];
    const userId = currentUser?.id;

    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/round/${roundId}/scene${userId ? `?userId=${userId}` : ''}`, {
        credentials: "include"
      });

      if (response.ok) {
        const scene = await response.json();
        const correctAnswer = {
          buildingName: scene.buildingName || 'Unknown Building',
          locationX: scene.locationX,
          locationY: scene.locationY,
          floor: scene.floor || 'Unknown Floor'
        };
        commit('SET_CORRECT_ANSWER', correctAnswer);
      } else {
        console.error('Failed to fetch correct answer');
      }
    } catch (error) {
      console.error('Error fetching correct answer:', error);
    }
  },

  async endRound({ state, commit, dispatch, rootGetters }, payload: { winner: string; roundResult: {points: number, distance: number} }) {
    //set winner of the round
    commit('SET_WINNER', payload.winner);

    //set round result from round (round-specific score)
    commit('SET_ROUND_RESULT_FROM_ROUND', payload.roundResult);

    // Get the current game mode to determine behavior
    const gameMode = rootGetters['gameInfo/getGameMode'];

    // For singleplayer, fetch correct answer immediately (traditional behavior)
    // For multiplayer/ranked, the correct answer is fetched in their respective endCurrentRound actions
    if (gameMode === 'singleplayer') {
      await dispatch('fetchCorrectAnswer');
    }

    // Clear saved map position when round ends - start fresh for next round
    commit('gameInfo/SET_MAP_CENTER', null, {root: true});
    commit('gameInfo/SET_MAP_ZOOM', null, {root: true});

    if (gameMode === 'singleplayer') {
      //end this round in the singleplayer game store
      dispatch('singleplayerGame/singleplayerGame_endCurrentRound', { winner: payload.winner, roundResult: payload.roundResult }, { root: true });
    } else if (gameMode === 'multiplayer') {
      //end this round in the multiplayer game store
      await dispatch('multiplayerGame/multiplayerGame_endCurrentRound', { winner: payload.winner, roundResult: payload.roundResult }, { root: true });
    } else if (gameMode === 'ranked') {
      //end this round in the ranked game store
      await dispatch('rankedGame/rankedGame_endCurrentRound', { winner: payload.winner, roundResult: payload.roundResult }, { root: true });
    }
  },
};
