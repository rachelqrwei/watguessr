// src/stores/modules/round/state.ts
export interface Scene {
  building: string;
  floor: string;
}

export interface RoundState {
  roundId: string | null;
  scene: Scene | null;
  winner: string | null;
  roundResult: {
    points: number | null;
    distance: number | null;
  } | null;
}

export const state = (): RoundState => ({
  roundId: null,
  scene: null,
  winner: null,
  roundResult: {
    points: 0,
    distance: 0
  }
});
