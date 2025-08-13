// src/stores/modules/round/state.ts
export interface RoundState {
  roundId: string | null;
  imageUrl: string | null;
  winner: string | null;
  roundResult: {
    points: number;
    distance: number;
  } | null;
}

export const state = (): RoundState => ({
  roundId: null,
  imageUrl: null,
  winner: null,
  roundResult: {
    points: 0,
    distance: 0
  }
});
