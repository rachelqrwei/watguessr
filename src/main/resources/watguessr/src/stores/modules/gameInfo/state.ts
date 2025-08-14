export interface GameInfoState {
  //personal
  gameMode: string;
  currentView: string;
}

export const state = (): GameInfoState => ({
  gameMode: '',
  currentView: ''
});
