// src/stores/modules/building/mutations.ts
import type { MutationTree } from 'vuex';
import type { BuildingState, BuildingDto } from './state';

export const mutations: MutationTree<BuildingState> = {
  SET_LOADING(state, isLoading: boolean) {
    state.loading = isLoading;
  },
  SET_ERROR(state, error: string | null) {
    state.error = error;
  },
 FETCH_BUILDINGS_SUCCESS(state, buildings: BuildingDto[]) {
    const map: Record<string, BuildingDto> = {};
    for (const b of buildings) {
      if (b && b.name) {
        map[b.name] = b;
      }
    }
    state.nameToBuilding = map;
  },
}; 