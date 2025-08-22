<template>
  <div class="player-score-tracker-container">
    <!-- Player 1 (YOU) -->
    <div class="player-score-tracker-1">
      <div class="player-score-text-container">
        <span class="player-name">{{ player1Name }}</span>
        <span class="player-points">{{ player1Score }} PTS</span>
        <span v-if="player1Status === 'ended'" class="player-status completed">✓</span>
        <span v-else-if="player1Status === 'playing'" class="player-status playing">●</span>
        <span v-else-if="player1Status === 'ready'" class="player-status ready">⏳</span>
        <span v-else-if="player1Status === 'disconnected'" class="player-status disconnected">❌</span>
      </div>
      <div class="player-score-progress-container">
        <div
          class="player-score-progress-bar"
          :style="{
            width: player1ScorePercentage + '%',
            background: 'var(--player-1-gradient)'
          }"
        />
      </div>
    </div>

    <!-- Player 2 (OPPONENT) -->
    <div class="player-score-tracker-2">
      <div class="player-score-text-container">
        <span class="player-name">{{ player2Name }}</span>
        <span class="player-points">{{ player2Score }} PTS</span>
        <span v-if="player2Status === 'ended'" class="player-status completed">✓</span>
        <span v-else-if="player2Status === 'playing'" class="player-status playing">●</span>
        <span v-else-if="player2Status === 'ready'" class="player-status ready">⏳</span>
        <span v-else-if="player2Status === 'disconnected'" class="player-status disconnected">❌</span>
      </div>
      <div class="player-score-progress-container">
        <div
          class="player-score-progress-bar"
          :style="{
            width: player2ScorePercentage + '%',
            background: 'var(--player-2-gradient)'
          }"
        />
      </div>
    </div>


  </div>
</template>

<script>
import {mapGetters} from "vuex";

export default {
  name: "RankedScoreTracker",
  computed: {
    ...mapGetters('gameInfo', [
      'getGameMode'
    ]),
    ...mapGetters('rankedGame', [
      'rankedGame_getPlayers'
    ]),
    ...mapGetters('user', [
      'getCurrentUser'
    ]),

    // Get current user info
    currentUser() {
      return this.getCurrentUser;
    },

    // Get all players from ranked game store
    players() {
      const players = this.rankedGame_getPlayers || {};
      console.log('🎯 Players from store:', players);
      return players;
    },



    // Get current user's player data
    currentPlayer() {
      if (!this.currentUser || !this.players) return null;
      const player = this.players[this.currentUser.id];
      console.log('🎯 Current player data:', player);
      return player;
    },

    // Get opponent player data (first player that's not the current user)
    opponentPlayer() {
      console.log(this.players);
      if (!this.currentUser || !this.players) return null;
      const opponentId = Object.keys(this.players).find(id => id !== this.currentUser.id);
      const opponent = opponentId ? this.players[opponentId] : null;
      console.log('🎯 Opponent player data:', opponent);
      return opponent;
    },

    // Player 1 (current user) data
    player1Name() {
      return this.currentPlayer?.username || 'YOU';
    },

    player1Score() {
      return this.currentPlayer?.score || 0;
    },

    player1Status() {
      return this.currentPlayer?.status || 'loading';
    },



    // Player 2 (opponent) data
    player2Name() {
      return this.opponentPlayer?.username || 'OPPONENT';
    },

    player2Score() {
      return this.opponentPlayer?.score || 0;
    },

    player2Status() {
      return this.opponentPlayer?.status || 'loading';
    },



    // Score percentages for progress bars
    player1ScorePercentage() {
      const total = this.player1Score + this.player2Score || 1;
      return Math.floor((this.player1Score * 100) / total);
    },

    player2ScorePercentage() {
      const total = this.player1Score + this.player2Score || 1;
      return Math.floor((this.player2Score * 100) / total);
    }
  },

  watch: {
    // Watch for score changes to add animation
    player1Score(newScore, oldScore) {
      if (newScore !== oldScore && oldScore !== undefined) {
        this.animateScoreChange('player1');
      }
    },
    player2Score(newScore, oldScore) {
      if (newScore !== oldScore && oldScore !== undefined) {
        this.animateScoreChange('player2');
      }
    }
  },

  methods: {
    // Animate score change
    animateScoreChange(player) {
      const scoreElement = player === 'player1' ?
        this.$el.querySelector('.player-score-tracker-1 .player-points') :
        this.$el.querySelector('.player-score-tracker-2 .player-points');

      if (scoreElement) {
        scoreElement.classList.add('score-updated');
        setTimeout(() => {
          scoreElement.classList.remove('score-updated');
        }, 300);
      }
    }
  },

  mounted() {
    // Debug: Log initial state
    console.log('🎯 RankedScoreTracker mounted');
    console.log('🎯 Current user:', this.currentUser);
    console.log('🎯 Players from store:', this.players);
  }
};
</script>

<style scoped>
.player-score-tracker-container {
  position: fixed;
  bottom: 9%;
  left: 50%;
  transform: translateX(-50%);
  width: 99vw;
  color: white;
  padding: 0 16px;
  z-index: 5;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.player-score-tracker-1,
.player-score-tracker-2 {
  display: flex;
  flex-direction: column;
}

.player-score-text-container {
  background: var(--dark-grey);
  padding: 12px 25px 0 25px;
  display: flex;
  gap: 28px;
  align-items: center;
  width: fit-content;
}

.player-score-tracker-1 .player-score-text-container {
  border-radius: 25px 25px 0 0px;
}

.player-score-tracker-2 .player-score-text-container {
  border-radius: 25px 25px 0px 0;
  margin-left: auto;
}

.player-score-progress-container {
  background: var(--dark-grey);
  padding: 12px 12px 15px 25px;
  width: 400px;
  height: 40px;
  box-shadow: 0 10px 10px rgba(0, 0, 0, 0.2);
  position: relative;
}

.player-score-tracker-1 .player-score-progress-container {
  border-radius: 0 25px 25px 25px;
}

.player-score-tracker-2 .player-score-progress-container {
  border-radius: 25px 0 25px 25px;
  direction: rtl;
}

.player-score-progress-container::before {
  content: '';
  position: absolute;
  top: 12px;
  left: 25px;
  right: 12px;
  bottom: 15px;
  background: #474F54;
  border-radius: 12px;
}

.player-score-progress-bar {
  height: 100%;
  border-radius: 12px;
  transition: width 0.3s ease;
  position: relative;
  z-index: 1;
}

.player-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--white);
}

.player-points {
  font-size: 14px;
  font-weight: 600;
  color: var(--light-grey);
  transition: all 0.3s ease;
}

.player-points.score-updated {
  transform: scale(1.1);
  color: var(--yellow);
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.player-status {
  font-size: 16px;
  font-weight: bold;
  margin-left: 8px;
}

.player-status.completed {
  color: #B6FF7F; /* Green checkmark */
}

.player-status.playing {
  color: #FF9F1C; /* Orange dot for playing */
  animation: pulse 2s infinite;
}

.player-status.ready {
  color: #2196F3; /* Blue hourglass for ready */
}

.player-status.disconnected {
  color: #f44336; /* Red X for disconnected */
  animation: fadeOut 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

@keyframes fadeOut {
  0% { opacity: 1; }
  50% { opacity: 0.3; }
  100% { opacity: 1; }
}


</style>
