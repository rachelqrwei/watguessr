<template>
  <div class="play-background" aria-hidden="true"></div>
  <div class="logo-container">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>

  <PlayStopwatch v-if="(singleplayerGame_getCurrentView === 'Map' || singleplayerGame_getCurrentView === 'Image')" />
  <div class="selection-display">
    <span>Current round: {{singleplayerGame_getCurrentRound}}</span>
    <div>
      <p>Selected Building: {{getGuessBuilding}}</p>
      <p>Lat: {{getGuessX}}</p>
      <p>Long: {{getGuessY}}</p>
    </div>
    <label class="floor-select-label">
      Select Floor:
      <select v-model="selectedFloor" class="floor-select">
        <option disabled value="">-- Choose a floor --</option>
        <option v-for="floor in availableFloors" :key="floor" :value="floor">{{ floor }}</option>
      </select>
    </label>
  </div>


  <div class="game-container">
    <!-- FLOOR SELECT DROPDOWN -->
    <div v-if="singleplayerGame_getCurrentView === 'Map'" class="view-pane">
      <button class="view-change-button" @click="SG_CHANGE_VIEW('Image')">
        <font-awesome-icon icon="image" />
        VIEW IMAGE
      </button>
      <PlayMapView @building-selected="handleBuildingSelected" />
    </div>

    <div v-if="singleplayerGame_getCurrentView === 'Image'" class="view-pane">
      <button class="view-change-button" @click="SG_CHANGE_VIEW('Map')">
        VIEW MAP
      </button>
      <PlayImageView />
    </div>

    <div v-if="singleplayerGame_getCurrentView === 'RoundEnd'">
      <PlaySingleplayerRoundEnd :points="getRoundResult.points" :distance="getRoundResult.distance" />
      <button class="view-change-button" @click="SG_CHANGE_VIEW('Map')">
      BACK TO MAP
      </button>
    </div>
  </div>

  <div v-if="errorMessage" class="error-banner">
    {{ errorMessage }}
  </div>

  <div v-if="(singleplayerGame_getCurrentView === 'Map' || singleplayerGame_getCurrentView === 'Image')">
    <button class="submit-button submit-button--yellow" @click="handleSubmit">SUBMIT</button>
  </div>
  <div v-else-if="singleplayerGame_getCurrentView === 'RoundEnd'">
    <button class="submit-button submit-button--white" @click="nextRoundOrEndGame">
      {{ singleplayerGame_getShouldEnd ? 'END GAME' : 'NEXT ROUND' }}
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
      errorMessage: ''
    }
  },
  computed: {
    ...mapGetters('singleplayer', [
      'singleplayerGame_getGameId',
      'singleplayerGame_getCurrentView',
      'singleplayerGame_getGameStatus',
      'singleplayerGame_getFinalWinner',
      'singleplayerGame_getCurrentRound',
      'singleplayerGame_getShouldEnd',
    ]),
    ...mapGetters('round', [
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
    ...mapGetters('building', [
      'getBuildingsMap'
    ]),

    availableFloors() {
      const buildingName = this.getGuessBuilding;
      if (!buildingName) return [];
      const b = this.getBuildingsMap?.[buildingName];
      const floors = b?.floors || [];
      return Array.isArray(floors) ? floors.map(f => String(f)) : [];
    }

  },
  watch: {
    selectedFloor(newVal) {
      if (newVal) {
        this.SET_FLOOR(newVal);
      }
    },
    singleplayerGame_getGameStatus(newStatus) {
      if (newStatus === 'ended') {
        if (this.singleplayerGame_getCurrentView !== 'RoundEnd') {
          this.SG_CHANGE_VIEW('RoundEnd');
        }
      }
    },
    getGuessBuilding(newVal, oldVal) {
      if (newVal !== oldVal) {
        const floors = this.availableFloors;
        this.selectedFloor = (floors && floors.length > 0) ? floors[0] : '';
      }
    }
  },
  methods: {
    ...mapActions('singleplayer', [
      'singleplayerGame_createSingleplayerGame',
      'singleplayerGame_endCurrentRound',
      'singleplayerGame_checkSingleplayerState'
    ]),
    ...mapActions('round', [
      "startRound"
    ]),
    ...mapActions('guess', [
      'submitGuess'
    ]),
    ...mapActions('building', [
      'fetchAllBuildings'
    ]),
    ...mapMutations('singleplayer', [
      'SG_CHANGE_VIEW',
      'SG_INCREMENT_ROUND'
    ]),
    ...mapMutations('guess', [
      'SET_FLOOR'
    ]),

    handleSubmit() {
      if (!this.getGuessBuilding) {
        this.errorMessage = "Please select a building.";
        return;
      }
      if (!this.selectedFloor) {
        this.errorMessage = "Please select a floor.";
        return;
      }
      this.errorMessage = ""; // clear any previous error

      // ensure floor committed to store before submit
      this.SET_FLOOR(this.selectedFloor);

      this.submitGuess();
    },

    async nextRoundOrEndGame() {
      // if the game should end already, navigate directly
      if (this.singleplayerGame_getShouldEnd) {
        this.$router.push('/singleplayer-game-end');
        return;
      }

      const shouldEnd = await this.singleplayerGame_checkSingleplayerState();
      if (shouldEnd) {
        this.$router.push('/singleplayer-game-end')
        return;
      }

      this.selectedBuilding = null;
      this.selectedFloor = '';
      this.resetTimer();

      await this.startRound({gameId: this.singleplayerGame_getGameId});
      this.SG_INCREMENT_ROUND();

      this.SG_CHANGE_VIEW("Map");
    },

    handleBuildingSelected(payload) {
      this.selectedBuilding = payload;
    },

    resetTimer() {
      this.timeLeft = 60000
    }
  },
  mounted() {
    this.fetchAllBuildings();
    this.singleplayerGame_createSingleplayerGame();
  }
}
</script>
<style scoped>
.play-background {
  position: fixed;
  inset: 0;
  background: var(--dark-grey);
  z-index: -1;
}
.play-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/GamePage.png') center bottom / cover no-repeat;
  opacity: 0.8;
  pointer-events: none;
}

.game-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 95vw;
  height: 78vh;
  border-radius: 15px;
  overflow: hidden;
  z-index: 1;
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

.submit-button--yellow {
  color: var(--yellow);
}

.submit-button--white {
  color: white;
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

.error-banner {
  position: fixed;
  bottom: 15%;
  left: 50%;
  transform: translateX(-50%);
  background-color: #ff4d4d;
  color: white;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: bold;
  z-index: 1000;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}
.view-pane {
  position: relative;
  width: 100%;
  height: 100%;
}
</style>
