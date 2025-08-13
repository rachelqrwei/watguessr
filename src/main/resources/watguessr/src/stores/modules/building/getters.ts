import type { GetterTree } from 'vuex';
import type { RootState } from '../../index';
import type { BuildingState, BuildingDto } from './state';

export const getters: GetterTree<BuildingState, RootState> = {
  getBuildingsMap: (state): Record<string, BuildingDto> => state.nameToBuilding,
  getBuildingByName: (state) => (name: string): BuildingDto | undefined => state.nameToBuilding[name],
}; 