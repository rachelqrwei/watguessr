export interface GameInfoState {
  //personal
  gameMode: string;
  currentView: string;
  mapCenter: [number, number] | null;
  mapZoom: number | null;
}

export const state = (): GameInfoState => ({
  gameMode: '',
  currentView: '',
  mapCenter: null,
  mapZoom: null
});
