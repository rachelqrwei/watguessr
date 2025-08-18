import type { ActionTree } from 'vuex';
import type { RootState } from '../../index';
import type { BuildingState, BuildingDto } from './state';

export const actions: ActionTree<BuildingState, RootState> = {
  async fetchAllBuildings({ commit, rootGetters }) {
    commit('SET_LOADING', true);
    commit('SET_ERROR', null);

    try {
      const baseUrl = import.meta.env.VITE_API_BASE_URL;

      const token = rootGetters['user/getToken'];

      const resp = await fetch(`${baseUrl}/api/building/all`, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
      }});
      if (!resp.ok) {
        throw new Error(`Failed to fetch buildings: ${resp.status}`);
      }
      const data: BuildingDto[] = await resp.json();
      commit('FETCH_BUILDINGS_SUCCESS', data);
    } catch (err: any) {
      commit('SET_ERROR', err?.message || 'Unknown error fetching buildings');
    } finally {
      commit('SET_LOADING', false);
    }
  },
};
