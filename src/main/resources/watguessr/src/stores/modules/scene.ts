import type { Module } from 'vuex';
import type { RootState } from '../index';

export interface Scene {
  id: string;
  building: string;
  floor: number;
  imageUrl: string;
}

export interface SceneState {
  scenes: Scene[];
}

const sceneModule: Module<SceneState, RootState> = {
  namespaced: true,

  state: (): SceneState => ({
    scenes: [],
  }),

  mutations: {
    SET_SCENES(state, scenes: Scene[]) {
      state.scenes = scenes;
    },
  },

  actions: {
    async fetchScenes({ commit }) {
      try {
        const response = await fetch('/api/scenes');
        const data = await response.json();
        commit('SET_SCENES', data);
      } catch (error) {
        console.error('Failed to fetch scenes:', error);
      }
    },
  },

  getters: {
    randomScene(state): Scene | null {
      const { scenes } = state;
      if (!scenes.length) return null;
      const index = Math.floor(Math.random() * scenes.length);
      return scenes[index];
    },
  },
};

export default sceneModule;
