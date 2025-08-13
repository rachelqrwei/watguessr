import { createStore } from 'vuex';
import type { Module } from 'vuex';
import { gameModule } from './modules/game/index';
import roundModule from './modules/round/index';
import guessModule from './modules/guess/index';
import leaderboardModule from './modules/leaderboard/index';
import userModule from "./modules/user/index";
import type { RoundState } from './modules/round/state';
import type { GameState } from './modules/game/state';
import type { GuessState } from './modules/guess/state';
import type { UserState } from "./modules/user/state.ts";

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
  user: UserState;
}

const store = createStore<RootState>({
  modules: {
    game: gameModule as Module<GameState, RootState>,
    round: roundModule as Module<RoundState, RootState>,
    guess: guessModule as Module<GuessState, RootState>,
    leaderboard: leaderboardModule as Module<LeaderboardState, RootState>,
    user: userModule as Module<UserState, RootState>
  },
});

export default store;
