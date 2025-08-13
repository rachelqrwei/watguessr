import { createStore } from 'vuex';
import type { Module } from 'vuex';
import { singleplayerModule } from './modules/game/index';
import roundModule from './modules/round/index';
import guessModule from './modules/guess/index';
import leaderboardModule from './modules/leaderboard';
import userModule from './modules/user/index';
import buildingModule from './modules/building/index';
import type { RoundState } from './modules/round/state';
import type { singleplayerGameState } from './modules/game/state';
import type { GuessState } from './modules/guess/state';
import type { UserState } from './modules/user/state';
import type { BuildingState } from '@/stores/modules/building/state';

export interface LeaderboardState {
  leaderboardData: any;
  loading: boolean;
  error: string | null;
  currentQuery: any;
}

export interface RootState {
  round: RoundState;
  singleplayer: singleplayerGameState;
  guess: GuessState;
  leaderboard: LeaderboardState;
  user: UserState;
  building: BuildingState;
}

const store = createStore<RootState>({
  modules: {
    singleplayer: singleplayerModule as Module<singleplayerGameState, RootState>,
    round: roundModule as Module<RoundState, RootState>,
    guess: guessModule as Module<GuessState, RootState>,
    leaderboard: leaderboardModule as Module<LeaderboardState, RootState>,
    user: userModule as Module<UserState, RootState>,
    building: buildingModule as Module<BuildingState, RootState>,
  },
});

export default store;
