<template>
  <div class="play-background" aria-hidden="true"></div>

  <PlayStopwatch v-if="(getCurrentView === 'Map' || getCurrentView === 'Image') && showStopwatch"/>

  <!-- Player Disconnection Alert -->
  <PlayerDisconnectionAlert v-if="getGameMode === 'multiplayer'" />

  <!-- Countdown Timer for All Games -->
  <CountdownTimer
    :isVisible="showCountdown"
    @countdown-complete="onCountdownComplete"
  />

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
    </div>

    <PlayFloorPanel
      v-if="(getCurrentView === 'Map' || getCurrentView === 'Image')"
      :round="getCurrentRoundNumber"
      :building="getGuessBuilding"
      :lat="getGuessX"
      :lng="getGuessY"
      :floors="availableFloors"
      :buildings-map="getBuildingsMap"
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
import PlayerDisconnectionAlert from '@/components/PlayerDisconnectionAlert.vue'
import CountdownTimer from '@/components/CountdownTimer.vue'


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
    PlayerDisconnectionAlert,
    CountdownTimer,
  },
  data() {
    return {
      timeLeft: 60000,
      selectedBuilding: '',
      selectedFloor: '',
      nextRoundOrEndGameButtonText: 'NEXT ROUND',
      errorMessage: '',
      showCountdown: false,
      countdownShown: false,
      showStopwatch: false
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
      return b?.floors || [];
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
        const buildingExists = this.getBuildingsMap && this.getBuildingsMap[newVal];
        if (!buildingExists) {
          // Building not in database, clear floor selection
          this.selectedFloor = '';
        }
        // Let PlayFloorPanel handle floor selection with proper sorting
      }
    },
    getCurrentView(newVal, oldVal) {
      // Reset selections when starting a new round (view changes from 'RoundEnd' to either 'Image' or 'Map')
      if (oldVal === 'RoundEnd' && (newVal === 'Image' || newVal === 'Map')) {
        console.log('New round started, resetting selections');
        this.selectedBuilding = '';
        this.selectedFloor = '';
        this.resetTimer();
        this.SET_MAP_CENTER(null);
        this.SET_MAP_ZOOM(null);
      }
    },
    singleplayerGame_getShouldEnd(newVal) {
      if (newVal && this.getCurrentView !== 'RoundEnd') {
        // Only navigate automatically if we're not on RoundEnd view
        // Add a small delay to ensure RoundEnd view has time to show
        setTimeout(() => {
          if (this.getCurrentView !== 'RoundEnd') {
            this.$router.push('/singleplayer-game-end');
          }
        }, 100);
      }
    },
    // Watch for when the game is ready to start (all players ready)
    'multiplayerGame_getPlayers': {
      handler(newPlayers) {
        if (this.getGameMode === 'multiplayer' && newPlayers) {
          // Only check for game ready if we haven't shown the countdown yet
          if (!this.countdownShown) {
            this.checkGameReady();
          }
        }
      },
      deep: true,
      immediate: true
    }
  },
  methods: {
    ...mapActions('singleplayerGame', [
      'singleplayerGame_createSingleplayerGame',
      'singleplayerGame_endCurrentRound',
      'singleplayerGame_checkSingleplayerState',
      'singleplayerGame_endGame'
    ]),
    ...mapActions('multiplayerGame', [
      'multiplayerGame_createMultiplayerGame',
      'multiplayerGame_endCurrentRound',
      'multiplayerGame_checkMultiplayerState',
      'multiplayerGame_updatePlayerStatus',
      'multiplayerGame_setPlayerReady',
      'multiplayerGame_setPlayerCompleted',
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
      "SET_CURRENT_VIEW",
      "SET_MAP_CENTER",
      "SET_MAP_ZOOM"
    ]),
    ...mapMutations('singleplayerGame', [
      'SG_INCREMENT_ROUND'
    ]),
    ...mapMutations('guess', [
      'SET_FLOOR'
    ]),

    onGlobalKeyDown(e) {
      // handle space key for submit
      if (e.code === 'Space') {
        e.preventDefault();
        if (this.getCurrentView === 'Map' || this.getCurrentView === 'Image') {
          this.handleSubmit();
        } else if (this.getCurrentView === 'RoundEnd') {
          this.nextRoundOrEndGame();
        }
        return;
      }

      // handle A/D keys for view switching
      if (e.code === 'KeyA' || e.code === 'KeyD') {
        if (this.getCurrentView === 'Map' || this.getCurrentView === 'Image') {
          e.preventDefault();
          if (e.code === 'KeyA') {
            this.SET_CURRENT_VIEW('Image');
          } else if (e.code === 'KeyD') {
            this.SET_CURRENT_VIEW('Map');
          }
          return;
        }
      }

      // handle W/D keys for floor switching (only when on Map or Image view)
      if ((e.code === 'KeyW' || e.code === 'KeyS') && (this.getCurrentView === 'Map' || this.getCurrentView === 'Image')) {
        e.preventDefault();
        this.switchFloor(e.code === 'KeyW' ? 'down' : 'up');
        return;
      }
    },

    async handleSubmit() {
      if (!this.getGuessBuilding) {
        this.errorMessage = "Please select a building.";
        return;
      }

      // Check if the building exists in our database
      const buildingExists = this.getBuildingsMap && this.getBuildingsMap[this.getGuessBuilding];

      if (buildingExists && !this.selectedFloor) {
        this.errorMessage = "Please select a floor.";
        return;
      }

      this.errorMessage = "";

      // If building exists in database, use selected floor; otherwise use fallback
      const floorToSubmit = buildingExists ? this.selectedFloor : 'floor';
      this.SET_FLOOR(floorToSubmit);

      // For multiplayer, update status to indicate player is submitting
      if (this.getGameMode === 'multiplayer') {
        this.multiplayerGame_updatePlayerStatus({ status: 'ended' });
      }

      await this.submitGuess();
    },

    async nextRoundOrEndGame() {
      console.log('🎮 nextRoundOrEndGame called, gameMode:', this.getGameMode);
      console.log('🎮 singleplayerGame_getShouldEnd:', this.singleplayerGame_getShouldEnd);

      if (this.getGameMode === 'singleplayer') {
        // Handle singleplayer logic
        if (this.singleplayerGame_getShouldEnd) {
          console.log('🎮 Navigating to singleplayer-game-end');
          this.$router.push('/singleplayer-game-end');
          return;
        }

        // Show countdown before starting next round
        this.showCountdown = true;
        this.showStopwatch = false;

        const shouldEnd = await this.singleplayerGame_checkSingleplayerState();
        if (shouldEnd) {
          // Call endGame to properly finish the game
          await this.singleplayerGame_endGame();
          this.$router.push('/singleplayer-game-end')
          return;
        }

        this.selectedBuilding = null;
        this.selectedFloor = '';
        this.resetTimer();

        // Clear saved map position for new round
        this.SET_MAP_CENTER(null);
        this.SET_MAP_ZOOM(null);

        this.SET_CURRENT_VIEW("Image");
      } else if (this.getGameMode === 'multiplayer') {
        // Handle multiplayer logic
        if (this.multiplayerGame_getShouldEnd) {
          this.$router.push('/multiplayer-game-end');
          return;
        }

        // Check if this is the last round
        if (this.multiplayerGame_getCurrentRound >= this.multiplayerGame_getMaxRounds) {
          // End the multiplayer game - send completed status
          this.multiplayerGame_setPlayerCompleted();
          return;
        }

        // Show countdown before starting next round
        this.showCountdown = true;
        this.showStopwatch = false;

        this.multiplayerGame_setPlayerReady();

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

    switchFloor(direction) {
      // If building doesn't exist in database, don't allow floor switching
      const buildingExists = this.getBuildingsMap && this.getBuildingsMap[this.getGuessBuilding];
      if (!buildingExists || !this.availableFloors || this.availableFloors.length === 0) return;

      const currentIndex = this.availableFloors.findIndex(f => String(f) === String(this.selectedFloor));
      let newIndex;

      if (direction === 'up') {
        newIndex = currentIndex <= 0 ? 0 : currentIndex - 1;
      } else {
        newIndex = currentIndex >= this.availableFloors.length - 1 ? this.availableFloors.length - 1 : currentIndex + 1;
      }

      this.selectedFloor = this.availableFloors[newIndex];
    },

    resetTimer() {
      this.timeLeft = 60000
    },

    onCountdownComplete() {
      this.showCountdown = false;
      this.showStopwatch = true;

      // also clear saved map view at round start
      this.SET_MAP_CENTER(null);
      this.SET_MAP_ZOOM(null);

      // Start the round
      if (this.getGameMode === 'multiplayer') {
        const gameId = this.$route.query.gameId || this.$store.getters['multiplayerGame/multiplayerGame_getGameId'];
        if (gameId) {
          this.startRound({ gameId });
        }
      }
      else if (this.getGameMode === 'singleplayer'){
        this.startSingleplayerRound();
      }
    },

    async checkGameReady() {
      const players = this.multiplayerGame_getPlayers;
      if (players && Object.keys(players).length > 0) {
        const allPlayersReady = Object.values(players).every(p => p.status === 'ready');
        if (allPlayersReady && !this.showCountdown && !this.countdownShown) {
          this.showCountdown = true;
          this.showStopwatch  = false;
          this.countdownShown = true;
        }
      }
    },

    async startSingleplayerRound() {
      try {
        await this.startRound({ gameId: this.singleplayerGame_getGameId });

        this.SG_INCREMENT_ROUND();
        this.SET_CURRENT_VIEW("Image");

        // clear saved map position for new round
        this.SET_MAP_CENTER(null);
        this.SET_MAP_ZOOM(null);
      } catch (error) {
        console.error('Failed to start singleplayer round:', error);
      }
    }
  },
  mounted() {
    this.fetchAllBuildings();
    window.addEventListener('keydown', this.onGlobalKeyDown);

    // Show countdown for singleplayer mode
    this.showCountdown = true;
    this.showStopwatch = false;

    if (this.getGameMode == 'singleplayer') {
      this.singleplayerGame_createSingleplayerGame();
    }
    else if (this.getGameMode == 'multiplayer') {
      // For multiplayer, the game is already initialized from Lobby.vue
      // Get the gameId from the route query or store
      const gameId = this.$route.query.gameId || this.$store.getters['multiplayerGame/multiplayerGame_getGameId'];

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

.test-disconnect-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #ef4444;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  z-index: 100;
  transition: all 0.2s ease;
}

.test-disconnect-btn:hover {
  background: #dc2626;
  transform: translateY(-2px);
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
