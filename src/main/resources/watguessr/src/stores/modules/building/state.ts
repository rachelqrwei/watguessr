export interface BuildingDto {
  id: string;
  name: string;
  floors: string[] | null;
  longitude: number | string | null;
  latitude: number | string | null;
}

export interface BuildingState {
  nameToBuilding: Record<string, BuildingDto>;
  loading: boolean;
  error: string | null;
}

export const state = (): BuildingState => ({
  nameToBuilding: {},
  loading: false,
  error: null
}); 