// src/stores/modules/round/state.ts
export interface Scene {
  building: string;
  floor: string;
}

export interface RoundState {
  roundId: string | null;
  scene: Scene | null;
  winner: string | null;
  scoreChange: number | null;
}

export const state = (): RoundState => ({
  roundId: null,
  scene: null,
  winner: null,
  scoreChange: null
});
