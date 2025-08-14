import { createStore } from 'vuex';
import type {Module} from 'vuex';
import roundModule from './modules/round/index';
import guessModule from './modules/guess/index';
import leaderboardModule from './modules/leaderboard';
import userModule from './modules/user/index';
import buildingModule from './modules/building/index';
import gameInfoModule from './modules/gameInfo/index';
import singleplayerGameModule from "@/stores/modules/singleplayerGame";
import multiplayerGameModule from "@/stores/modules/multiplayerGame";

import type { RoundState } from './modules/round/state';
import type { singleplayerGameState } from '@/stores/modules/singleplayerGame/state';
import type { GuessState } from './modules/guess/state';
import type { UserState } from './modules/user/state';
import type { BuildingState } from '@/stores/modules/building/state';
import type { MultiplayerGameState } from "@/stores/modules/multiplayerGame/state.ts";
import type { GameInfoState } from "@/stores/modules/gameInfo/state.ts";

export interface LeaderboardState {
  leaderboardData: any;
  loading: boolean;
  error: string | null;
  currentQuery: any;
}

export interface RootState {
  round: RoundState;
  guess: GuessState;
  leaderboard: LeaderboardState;
  user: UserState;
  building: BuildingState;
  gameInfo: GameInfoState;
  singleplayerGame: singleplayerGameState;
  multiplayerGame: MultiplayerGameState;
}

const store = createStore<RootState>({
  modules: {
    round: roundModule as Module<RoundState, RootState>,
    guess: guessModule as Module<GuessState, RootState>,
    leaderboard: leaderboardModule as Module<LeaderboardState, RootState>,
    user: userModule as Module<UserState, RootState>,
    building: buildingModule as Module<BuildingState, RootState>,
    gameInfo: gameInfoModule as Module<GameInfoState, RootState>,
    singleplayerGame: singleplayerGameModule as Module<singleplayerGameState, RootState>,
    multiplayerGame: multiplayerGameModule as Module<MultiplayerGameState, RootState>
  },
});

export default store;
