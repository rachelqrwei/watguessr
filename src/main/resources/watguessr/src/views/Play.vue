<template>
  <div class="logo-container">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>

  <PlayStopwatch v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')" />
  <div class="selection-display">
    <span>Current round: {{getCurrentRound}}</span>
    <div>
      <p>Selected Building: {{getGuessBuilding}}</p>
      <p>Lat: {{getGuessX}}</p>
      <p>Long: {{getGuessY}}</p>
    </div>
    <label class="floor-select-label">
      Select Floor:
      <select v-model="selectedFloor" class="floor-select">
        <option disabled value="">-- Choose a floor --</option>
        <option>Basement</option>
        <option>Ground</option>
        <option>1</option>
        <option>2</option>
        <option>3</option>
      </select>
    </label>
  </div>


  <div class="game-container">
    <!-- FLOOR SELECT DROPDOWN -->
    <div v-if="getCurrentView === 'Map'">
      <button class="view-change-button" @click="changeView('Image')">
        <font-awesome-icon icon="image" />
        VIEW IMAGE
      </button>
      <PlayMapView @building-selected="handleBuildingSelected" />
    </div>

    <div v-if="getCurrentView === 'Image'">
      <button class="view-change-button" @click="changeView('Map')">
        VIEW MAP
      </button>
      <PlayImageView />
    </div>

    <div v-if="getCurrentView === 'RoundEnd'">
      <PlaySingleplayerRoundEnd :points="getRoundResult.points" :distance="getRoundResult.distance" />
      <button class="view-change-button" @click="changeView('Map')">
      BACK TO MAP
      </button>
    </div>
  </div>
  <div v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')">
    <button class="submit-button" style="color: var(--yellow);" @click="handleSubmit">SUBMIT</button>
  </div>
  <div v-else-if="getCurrentView === 'RoundEnd'">
    <button class="submit-button" style="color: white" @click="nextRoundOrEndGame">
      {{ getCurrentRound < getMaxRounds ? 'NEXT ROUND' : 'END GAME' }}
    </button>
  </div>

  <PlayScoreTracker />
</template>
<script>
import {mapGetters, mapActions, mapMutations} from 'vuex';
import PlayStopwatch from '@/views/play-components/Play.Stopwatch.vue'
import PlayMapView from '@/views/play-components/Play.Map.vue'
import PlayImageView from '@/views/play-components/Play.Image.vue'
import PlayScoreTracker from '@/views/play-components/Play.ScoreTracker.vue'
import PlaySingleplayerRoundEnd from '@/views/play-components/Play.SingleplayerRoundEnd.vue'
import { RouterLink, useRouter } from 'vue-router'

export default {
  components: {
    PlayStopwatch,
    PlayMapView,
    PlayImageView,
    PlayScoreTracker,
    PlaySingleplayerRoundEnd,
    RouterLink,
  },
  data() {
    return {
      timeLeft: 60000,
      selectedBuilding: '',
      selectedFloor: '',
      nextRoundOrEndGameButtonText: 'NEXT ROUND',
    }
  },
  computed: {
    ...mapGetters('game', [
      'getGameId',
      'getCurrentView',
      'getGameStatus',
      'getFinalWinner',
      'getCurrentRound',
      'getMaxRounds'
    ]),
    ...mapGetters('round', [
      'getScene',
      'getWinner',
      'getRoundResult'
    ]),
    ...mapGetters('guess', [
      'getGuessTime',
      'getGuessX',
      'getGuessY',
      'getGuessBuilding',
      'getGuessFloor'
    ]),

  },
  methods: {
    ...mapActions('game', [
      'createSingleplayerGame',
      'recordRoundWinner',
      'endCurrentRound'
    ]),
    ...mapActions('round', [
      "startRound"
    ]),
    ...mapActions('guess', [
      'submitGuess'
    ]),
    ...mapMutations('game', [
      'CHANGE_VIEW',
      'INCREMENT_ROUND'
    ]),

    handleSubmit() {
      this.submitGuess();
    },

    nextRoundOrEndGame() {
      if (this.getCurrentRound >= this.getMaxRounds) {
        // Go to game end screen
        this.$router.push('/singleplayer-game-end')
        return;
      }

      //if still has rounds left
      //reset every UI detail about current round
      this.selectedBuilding = null;
      this.selectedFloor = '';
      this.resetTimer();

      //start next round
      this.startRound({gameId: this.getGameId});
      this.INCREMENT_ROUND();

      //change view to map again
      this.CHANGE_VIEW("Map");
    },

    handleBuildingSelected(payload) {
      this.selectedBuilding = payload;
    },

    resetTimer() {
      this.timeLeft = 60000 // or any other time in ms
    }
  },
  mounted() {
    this.createSingleplayerGame();
  }
}
</script>
<style scoped>
.game-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 95vw;
  height: 78vh;
  border-radius: 15px;
  overflow: hidden;
}

.view-change-button {
  position: absolute;
  top: 30px;
  left: 40px;
  z-index: 10;
  background: var(--dark-grey);
  padding: 20px;
  border: none;
  border-radius: 40px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  width: 200px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  color: var(--white);
  transition: all 0.2s ease;
}

.view-change-button:hover {
  background: rgba(0, 0, 0, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

.test-end-btn {
  left: 230px;
  top: 30px;
  background: #ffcb3b;
  color: #232323;
  width: 220px;
  margin-left: 20px;
}

.test-end-btn:hover {
  color: white;
}

.logo-container {
  position: absolute;
  top: 4%;
  left: 3%;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  color: var(--white);
}

.logo-text {
  text-decoration: none;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--white);
  outline: none;
}

.submit-button {
  position: fixed;
  bottom: 7%;
  left: 50%;
  transform: translateX(-50%);
  background: var(--dark-grey);

  padding: 25px;
  font-size: 25px;
  width: 280px;
  line-height: 40px;
  text-align: center;
  font-weight: bolder;
  font-family: 'Oxanium', sans-serif;
  border-radius: 40px;
  z-index: 999;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 10px 10px rgba(0, 0, 0, 0.2);
}

.submit-button:hover {
  transform: translateX(-50%) translateY(-2px);
}

.selection-display {
  z-index: 999;
  position: absolute;
  top: 100px;
  background: black;
}
</style>
