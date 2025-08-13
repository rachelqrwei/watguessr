import { createStore } from 'vuex';
import type {Module} from 'vuex';
import roundModule from './modules/round/index';
import guessModule from './modules/guess/index';
import leaderboardModule from './modules/leaderboard';
import userModule from './modules/user/index';
import buildingModule from './modules/building/index';
import type { RoundState } from './modules/round/state';
import type { singleplayerGameState } from '@/stores/modules/singleplayerGame/state';
import type { GuessState } from './modules/guess/state';
import type { UserState } from './modules/user/state';
import type { BuildingState } from '@/stores/modules/building/state';
import singleplayerGameModule from "@/stores/modules/singleplayerGame";

export interface LeaderboardState {
  leaderboardData: any;
  loading: boolean;
  error: string | null;
  currentQuery: any;
}

export interface RootState {
  round: RoundState;
  singleplayerGame: singleplayerGameState;
  guess: GuessState;
  leaderboard: LeaderboardState;
  user: UserState;
  building: BuildingState;
}

const store = createStore<RootState>({
  modules: {
    singleplayerGame: singleplayerGameModule as Module<singleplayerGameState, RootState>,
    round: roundModule as Module<RoundState, RootState>,
    guess: guessModule as Module<GuessState, RootState>,
    leaderboard: leaderboardModule as Module<LeaderboardState, RootState>,
    user: userModule as Module<UserState, RootState>,
    building: buildingModule as Module<BuildingState, RootState>,
  },
});

export default store;
