<template>
  <div class="play-background" aria-hidden="true"></div>
  <div class="logo-container">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>

  <PlayStopwatch v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')" />

  <div class="game-container">
    <!-- FLOOR SELECT DROPDOWN -->
    <div v-if="getCurrentView === 'Map'" class="view-pane">
      <button class="view-change-button" @click="SET_CURRENT_VIEW('Image')">
        <font-awesome-icon icon="image" />
        VIEW IMAGE
      </button>
      <PlayMapView @building-selected="handleBuildingSelected" />
    </div>

    <div v-if="getCurrentView === 'Image'" class="view-pane">
      <button class="view-change-button" @click="SET_CURRENT_VIEW('Map')">
        VIEW MAP
      </button>
      <PlayImageView />
    </div>

    <div v-if="getCurrentView === 'RoundEnd'">
      <PlaySingleplayerRoundEnd :points="getRoundResult.points" :distance="getRoundResult.distance" />
    </div>

    <PlayFloorPanel
      v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')"
      :round="singleplayerGame_getCurrentRound"
      :building="getGuessBuilding"
      :lat="getGuessX"
      :lng="getGuessY"
      :floors="availableFloors"
      v-model:floor="selectedFloor"
    />
  </div>

  <div v-if="errorMessage" class="error-banner">
    {{ errorMessage }}
  </div>

  <div v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')">
    <button class="submit-button submit-button--yellow" @click="handleSubmit">SUBMIT</button>
  </div>
  <div v-else-if="getCurrentView === 'RoundEnd'">
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
import PlayFloorPanel from '@/views/play-components/Play.FloorPanel.vue'
import { RouterLink, useRouter } from 'vue-router'

export default {
  components: {
    PlayStopwatch,
    PlayMapView,
    PlayImageView,
    PlayScoreTracker,
    PlaySingleplayerRoundEnd,
    PlayFloorPanel,
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
    ...mapGetters("gameInfo", [
      'getCurrentView'
    ]),
    ...mapGetters('singleplayerGame', [
      'singleplayerGame_getGameId',
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
        if (this.getCurrentView !== 'RoundEnd') {
          this.SET_CURRENT_VIEW('RoundEnd');
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
    ...mapActions('singleplayerGame', [
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
    ...mapMutations('gameInfo', [
      "SET_CURRENT_VIEW"
    ]),
    ...mapMutations('singleplayerGame', [
      'SG_INCREMENT_ROUND'
    ]),
    ...mapMutations('guess', [
      'SET_FLOOR'
    ]),

    onGlobalKeyDown(e) {
      if (e.code !== 'Enter' && e.code !== 'NumpadEnter') return;
      if (this.getCurrentView === 'Map' || this.getCurrentView === 'Image') {
        e.preventDefault();
        this.handleSubmit();
      } else if (this.getCurrentView === 'RoundEnd') {
        e.preventDefault();
        this.nextRoundOrEndGame();
      }
    },

    handleSubmit() {
      if (!this.getGuessBuilding) {
        this.errorMessage = "Please select a building.";
        return;
      }
      if (!this.selectedFloor) {
        this.errorMessage = "Please select a floor.";
        return;
      }
      this.errorMessage = "";
      this.SET_FLOOR(this.selectedFloor);
      this.submitGuess();
    },

    async nextRoundOrEndGame() {
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

      this.SET_CURRENT_VIEW("Map");
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
    window.addEventListener('keydown', this.onGlobalKeyDown);
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.onGlobalKeyDown);
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
  top: 20px;
  left: 20px;
  z-index: 10;
  background: rgba(0, 0, 0, 0.7);
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 40px;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  width: 200px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  color: var(--white);
  transition: all 0.2s ease;
}

.view-change-button:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 255, 255, 0.2);
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
