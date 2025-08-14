// src/stores/modules/game/mutations.ts
import type { MutationTree } from 'vuex';
import type {GameInfoState} from './state';

export const mutations: MutationTree<GameInfoState> = {
  SET_GAME_MODE(state, gameMode: string) {
    state.gameMode = gameMode;
  },
  SET_CURRENT_VIEW(state, currentView: string) {
    state.currentView = currentView;
  },
  SET_MAP_CENTER(state, center: [number, number] | null) {
    state.mapCenter = center;
  },
  SET_MAP_ZOOM(state, zoom: number | null) {
    state.mapZoom = zoom;
  },
  RESET_GAME(state) {
    state.gameMode = '';
    state.currentView = '';
    state.mapCenter = null;
    state.mapZoom = null;
  },
};
