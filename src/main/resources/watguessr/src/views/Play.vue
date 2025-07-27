<template>
  <div class="logo-container">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>

  <PlayStopwatch />

  <div class="game-container">
    <div v-if="currentView === 'Map'">
      <button class="view-change-button" @click="changeView('Image')">
        <font-awesome-icon icon="image" />
        VIEW IMAGE
      </button>
      <button class="view-change-button test-end-btn" @click="changeView('RoundEnd')">
        TEST ROUND END
      </button>
      <PlayMapView />
    </div>

    <div v-if="currentView === 'Image'">
      <button class="view-change-button" @click="changeView('Map')">
        VIEW MAP
      </button>
      <button class="view-change-button test-end-btn" @click="changeView('RoundEnd')">
        TEST ROUND END
      </button>
      <PlayImageView />
    </div>

    <div v-if="currentView === 'RoundEnd'">
      <PlaySingleplayerRoundEnd />
      <button class="view-change-button" @click="changeView('Map')">
      BACK TO MAP
      </button>
    </div>
  </div>
  <div v-if="(currentView === 'Map' || currentView === 'Image')">
    <button class="submit-button" style="color: var(--yellow);">SUBMIT</button>
  </div>
  <div v-else-if="currentView === 'RoundEnd'">
    <button class="submit-button" style="color: white">NEXT ROUND</button>
  </div>

  <PlayScoreTracker />
</template>
<script>
import { mapGetters, mapActions } from 'vuex';
import PlayStopwatch from '@/views/play-components/Play.Stopwatch.vue'
import PlayMapView from '@/views/play-components/Play.Map.vue'
import PlayImageView from '@/views/play-components/Play.Image.vue'
import PlayScoreTracker from '@/views/play-components/Play.ScoreTracker.vue'
import PlaySingleplayerRoundEnd from '@/views/play-components/Play.SingleplayerRoundEnd.vue'
import { RouterLink } from 'vue-router'

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
      currentView: 'Map'
    }
  },
  computed: {
    ...mapGetters('game', ['gameId', 'gameStatus', 'finalWinner']),
    // Add other module getters similarly:
    ...mapGetters('round', ['scene', 'winner']),
  },
  methods: {
    ...mapActions('game', ['createSingleplayerGame']),
    ...mapActions('guess', ['submitGuess']),

    changeView(nextView) {
      this.currentView = nextView;
    },

    handleSubmit() {
      const guess = {
        user: 'player1', // replace with actual user info
        building: this.scene.building,
        floor: this.scene.floor,
      };
      this.submitGuess(guess).then(() => {
        this.changeView('RoundEnd');
      });
    },

    nextRoundOrEndGame() {
      if (this.gameStatus === 'ended') {
        // Handle end game, e.g. route home or reset
      } else {
        this.changeView('Map');
      }
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
</style>
