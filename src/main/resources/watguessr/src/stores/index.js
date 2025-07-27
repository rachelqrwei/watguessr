// store/index.js
import { createStore } from 'vuex';
import gameModule from './modules/game';
import roundModule from './modules/round';
import guessModule from './modules/guess';

export default createStore({
  modules: {
    game: gameModule,
    round: roundModule,
    guess: guessModule,
  },
});
