// src/stores/modules/guess/state.ts
export interface GuessState {
  user: {
    id: string | null
  },
  time: number | null,
  building: string | null,
  guessX: number | null,
  guessY: number | null,
  floor: string | null
}

export const state = (): GuessState => ({
  user: {
    id: null
  },
  time: null,
  building: null,
  guessX: null,
  guessY: null,
  floor: null
});
