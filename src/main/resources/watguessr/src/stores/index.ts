
// src/stores/index.ts
import { createStore } from 'vuex';
import type { Module } from 'vuex';
import { gameModule } from './modules/game';
import roundModule from './modules/round';
import guessModule from './modules/guess';
import type { RoundState } from './modules/round';
import type { GameState } from './modules/game';
import type { GuessState } from './modules/guess';

export interface RootState {
  round: RoundState;
  game: GameState;
  guess: GuessState;
}

const store = createStore<RootState>({
  modules: {
    game: gameModule as Module<GameState, RootState>,
    round: roundModule as Module<RoundState, RootState>,
    guess: guessModule as Module<GuessState, RootState>,
  },
});

export default store;
