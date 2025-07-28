import { createStore } from 'vuex';
import type { Module } from 'vuex';
import { gameModule } from './modules/game';
import roundModule from './modules/round';
import guessModule from './modules/guess';
import leaderboardModule from './modules/leaderboard';
import type { RoundState } from './modules/round';
import type { GameState } from './modules/game';
import type { GuessState } from './modules/guess';

export interface LeaderboardState {
  leaderboardData: any;
  loading: boolean;
  error: string | null;
  currentQuery: any;
}

export interface RootState {
  round: RoundState;
  game: GameState;
  guess: GuessState;
  leaderboard: LeaderboardState;
}

const store = createStore<RootState>({
  modules: {
    game: gameModule as Module<GameState, RootState>,
    round: roundModule as Module<RoundState, RootState>,
    guess: guessModule as Module<GuessState, RootState>,
    leaderboard: leaderboardModule as Module<LeaderboardState, RootState>,
  },
});

export default store;
