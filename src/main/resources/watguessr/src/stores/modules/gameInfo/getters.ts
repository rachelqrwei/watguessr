// src/stores/modules/game/getters.ts
import type { GetterTree } from 'vuex';
import type {GameInfoState} from './state';
import type {RootState} from "../../index.ts";

export const getters: GetterTree<GameInfoState, RootState> = {
  getGameMode: (state) => state.gameMode,
  getCurrentView: (state) => state.currentView,
  getMapCenter: (state) => state.mapCenter,
  getMapZoom: (state) => state.mapZoom
};
