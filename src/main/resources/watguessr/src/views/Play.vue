<template>
  <div class="play-background" aria-hidden="true"></div>
  <div class="logo-container">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>

  <PlayStopwatch v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')" />

  <div class="game-container">
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
      <PlaySingleplayerRoundEnd v-if="getGameMode === 'singleplayer'" :points="getRoundResult.points" :distance="getRoundResult.distance" />
      <PlayMultiplayerRoundEnd v-if="getGameMode === 'multiplayer'" :points="getRoundResult.points" :distance="getRoundResult.distance" />
      <button class="view-change-button" @click="SET_CURRENT_VIEW('Map')">
      BACK TO MAP
      </button>
    </div>

    <PlayFloorPanel
      v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')"
      :round="getCurrentRoundNumber"
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
      {{ getNextRoundButtonText }}
    </button>
  </div>

  <div id="score-tracker">
    <PlaySingleplayerScoreTracker v-if="getGameMode === 'singleplayer'" />
    <PlayMultiplayerScoreTracker v-if="getGameMode === 'multiplayer'" />
    <PlayRankedScoreTracker v-if="getGameMode === 'ranked'" />
  </div>

</template>
<script>
import {mapGetters, mapActions, mapMutations} from 'vuex';
import PlayStopwatch from '@/views/play-components/Play.Stopwatch.vue'
import PlayMapView from '@/views/play-components/Play.Map.vue'
import PlayImageView from '@/views/play-components/Play.Image.vue'
import PlaySingleplayerScoreTracker from '@/views/play-components/score-trackers/Play.SingleplayerScoreTracker.vue'
import PlayMultiplayerScoreTracker from '@/views/play-components/score-trackers/Play.MultiplayerScoreTracker.vue'
import PlayRankedScoreTracker from "@/views/play-components/score-trackers/Play.RankedScoreTracker.vue";
import PlaySingleplayerRoundEnd from '@/views/play-components/Play.SingleplayerRoundEnd.vue'
import PlayMultiplayerRoundEnd from '@/views/play-components/Play.MultiplayerRoundEnd.vue'
import PlayFloorPanel from '@/views/play-components/Play.FloorPanel.vue'
import { RouterLink, useRouter } from 'vue-router'

export default {
  components: {
    PlayStopwatch,
    PlayMapView,
    PlayImageView,
    PlaySingleplayerScoreTracker,
    PlayMultiplayerScoreTracker,
    PlayRankedScoreTracker,
    PlaySingleplayerRoundEnd,
    PlayMultiplayerRoundEnd,
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
      'getCurrentView',
      'getGameMode'
    ]),
    ...mapGetters('singleplayerGame', [
      'singleplayerGame_getGameId',
      'singleplayerGame_getGameStatus',
      'singleplayerGame_getFinalWinner',
      'singleplayerGame_getCurrentRound',
      'singleplayerGame_getShouldEnd',
    ]),
    ...mapGetters('multiplayerGame', [
      'multiplayerGame_getTimer',
      'multiplayerGame_getCurrentRound',
      'multiplayerGame_getMaxRounds',
      'multiplayerGame_getShouldEnd',
      'multiplayerGame_getGameId',
      'multiplayerGame_getPlayers'
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
    },

    getNextRoundButtonText() {
      if (this.getGameMode === 'singleplayer') {
        return this.singleplayerGame_getShouldEnd ? 'END GAME' : 'NEXT ROUND';
      } else if (this.getGameMode === 'multiplayer') {
        if (this.multiplayerGame_getShouldEnd) {
          return 'VIEW RESULTS';
        }
        // Check if this is the last round
        if (this.multiplayerGame_getCurrentRound >= this.multiplayerGame_getMaxRounds) {
          return 'FINISH GAME';
        }
        return 'READY FOR NEXT ROUND';
      }
      return 'NEXT ROUND';
    },

    getCurrentRoundNumber() {
      if (this.getGameMode === 'singleplayer') {
        return this.singleplayerGame_getCurrentRound;
      } else if (this.getGameMode === 'multiplayer') {
        const currentRound = this.multiplayerGame_getCurrentRound;
        console.log('🎮 Current multiplayer round number:', currentRound);
        return currentRound;
      }
      return 1;
    }

  },
  watch: {
    selectedFloor(newVal) {
      if (newVal) {
        this.SET_FLOOR(newVal);
      }
    },
    getGuessBuilding(newVal, oldVal) {
      if (newVal !== oldVal) {
        const floors = this.availableFloors;
        this.selectedFloor = (floors && floors.length > 0) ? floors[0] : '';
      }
    },
    getCurrentView(newVal, oldVal) {
      // Reset selections when starting a new round (view changes back to 'Map')
      if (newVal === 'Map' && oldVal === 'RoundEnd') {
        console.log('New round started, resetting selections');
        this.selectedBuilding = '';
        this.selectedFloor = '';
        this.resetTimer();
      }
    },
    singleplayerGame_getShouldEnd(newVal) {
      if (newVal) {
        this.$router.push('/singleplayer-game-end');
      }
    }
  },
  methods: {
    ...mapActions('singleplayerGame', [
      'singleplayerGame_createSingleplayerGame',
      'singleplayerGame_endCurrentRound',
      'singleplayerGame_checkSingleplayerState'
    ]),
    ...mapActions('multiplayerGame', [
      'multiplayerGame_createMultiplayerGame',
      'multiplayerGame_endCurrentRound',
      'multiplayerGame_checkMultiplayerState',
      'multiplayerGame_updatePlayerStatus',
      'multiplayerGame_setPlayerReady',
      'multiplayerGame_disconnect',
      'multiplayerGame_endGame'
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

    async handleSubmit() {
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

      // For multiplayer, update status to indicate player is submitting
      if (this.getGameMode === 'multiplayer') {
        this.multiplayerGame_updatePlayerStatus({ status: 'ended' });
      }

      await this.submitGuess();
    },

    async nextRoundOrEndGame() {
      if (this.getGameMode === 'singleplayer') {
        // Handle singleplayer logic
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
      } else if (this.getGameMode === 'multiplayer') {
        // Handle multiplayer logic
        if (this.multiplayerGame_getShouldEnd) {
          this.$router.push('/multiplayer-game-end');
          return;
        }

        // Check if this is the last round
        if (this.multiplayerGame_getCurrentRound >= this.multiplayerGame_getMaxRounds) {
          // End the multiplayer game
          await this.multiplayerGame_endGame();
          this.$router.push('/multiplayer-game-end');
          return;
        }

        // Set player as ready for next round
        console.log('🕹️ Setting player as ready for next round...');
        this.multiplayerGame_setPlayerReady();

        // The WebSocket will handle round progression when all players are ready
        console.log('⏳ Player marked as ready for next round. Waiting for other players...');

        // Debug: Check if round progression works properly
        setTimeout(() => {
          if (this.getCurrentView === 'RoundEnd') {
            console.log('⚠️ Still on RoundEnd after 3 seconds. Check WebSocket connection.');
          }
        }, 3000);
      }
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
    window.addEventListener('keydown', this.onGlobalKeyDown);

    if (this.getGameMode == 'singleplayer') {
      this.singleplayerGame_createSingleplayerGame();
    }
    else if (this.getGameMode == 'multiplayer') {
      // For multiplayer, the game is already initialized from Lobby.vue
      // Get the gameId from the route query or store
      const gameId = this.$route.query.gameId || this.$store.getters['multiplayerGame/multiplayerGame_getGameId'];

      if (gameId) {
        // Start the first round for multiplayer
        this.startRound({ gameId });
      }

      // Update player status to 'playing'
      this.multiplayerGame_updatePlayerStatus({ status: 'playing' });
    }
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.onGlobalKeyDown);

    // Disconnect from multiplayer WebSocket when leaving
    if (this.getGameMode === 'multiplayer') {
      this.multiplayerGame_disconnect();
    }
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
