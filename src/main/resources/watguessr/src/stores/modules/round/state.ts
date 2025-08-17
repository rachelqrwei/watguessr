// src/stores/modules/round/state.ts
export interface RoundState {
  roundId: string | null;
  imageUrl: string | null;
  winner: string | null;
  roundResult: {
    points: number;
    distance: number;
  } | null;
  correctAnswer: {
    buildingName: string | null;
    locationX: number | null;
    locationY: number | null;
    floor: string | null;
  } | null;
}

export const state = (): RoundState => ({
  roundId: null,
  imageUrl: null,
  winner: null,
  roundResult: {
    points: 0,
    distance: 0
  },
  correctAnswer: {
    buildingName: null,
    locationX: null,
    locationY: null,
    floor: null
  }
});
