<template>
  <div class="player-score-tracker-container">
    <!-- Player 1 (YOU) -->
    <div class="player-score-tracker-1">
      <div class="player-score-text-container" :class="{ 'status-ready': player1Status === 'ready', 'status-ended': player1Status === 'ended', 'status-playing': player1Status === 'playing' }">
        <div class="player-info">
          <span class="player-name">{{ player1Name }}</span>
          <span class="player-elo">{{ player1Elo }} ELO</span>
          <span class="player-status" v-if="player1Status !== 'playing'">{{ getStatusText(player1Status) }}</span>
        </div>
        <span class="player-points">{{ player1Score }} PTS</span>
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
      <div class="player-score-text-container" :class="{ 'status-ready': player2Status === 'ready', 'status-ended': player2Status === 'ended', 'status-playing': player2Status === 'playing' }">
        <div class="player-info">
          <span class="player-name">{{ player2Name }}</span>
          <span class="player-elo">{{ player2Elo }} ELO</span>
          <span class="player-status" v-if="player2Status !== 'playing'">{{ getStatusText(player2Status) }}</span>
        </div>
        <span class="player-points">{{ player2Score }} PTS</span>
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

    <!-- Round indicator -->
    <div class="round-indicator">
      <span class="round-text">ROUND {{ currentRound }}/{{ maxRounds }}</span>
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
      'rankedGame_getPlayers',
      'rankedGame_getCurrentRound',
      'rankedGame_getMaxRounds'
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

    // Get current round and max rounds
    currentRound() {
      return this.rankedGame_getCurrentRound || 1;
    },

    maxRounds() {
      return this.rankedGame_getMaxRounds || 5;
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

    player1Elo() {
      // Get pre-game ELO from store if available
      if (this.currentUser?.username) {
        const preGameElos = this.$store.getters['rankedGame/rankedGame_getPreGameElos'];
        if (preGameElos && preGameElos[this.currentUser.username]) {
          console.log('🎯 ScoreTracker: Using pre-game ELO from store for current user:', this.currentUser.username, preGameElos[this.currentUser.username]);
          return preGameElos[this.currentUser.username];
        }
      }

      // Fallback to current user's ELO if pre-game not available
      if (this.currentUser?.elo) {
        return this.currentUser.elo;
      }

      // Final fallback
      return 1200;
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

    player2Elo() {
      const preGameElos = this.$store.getters['rankedGame/rankedGame_getPreGameElos'];
      return preGameElos[this.opponentPlayer?.username];
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
    },
    // Watch for status changes to log them
    player1Status(newStatus, oldStatus) {
      if (newStatus !== oldStatus) {
        console.log('🎯 Player 1 status changed:', oldStatus, '→', newStatus);
      }
    },
    player2Status(newStatus, oldStatus) {
      if (newStatus !== oldStatus) {
        console.log('🎯 Player 2 status changed:', oldStatus, '→', newStatus);
      }
    }
  },

  methods: {
    // Convert status to display text
    getStatusText(status) {
      switch (status) {
        case 'ready':
          return 'READY';
        case 'ended':
          return 'FINISHED';
        case 'loading':
          return 'LOADING';
        case 'completed':
          return 'COMPLETED';
        default:
          return status?.toUpperCase() || '';
      }
    },

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
    console.log('🎯 Current round:', this.currentRound);
    console.log('🎯 Max rounds:', this.maxRounds);
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
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

/* Responsive styling for ranked score tracker */
@media (max-width: 1200px) {
  .player-score-progress-container {
    width: 350px;
    height: 35px;
    padding: 10px 10px 12px 20px;
  }
  
  .player-score-text-container {
    padding: 10px 20px 0 20px;
    gap: 20px;
  }
  
  .player-name,
  .player-elo {
    font-size: 13px;
  }
}

@media (max-width: 1024px) {
  .player-score-progress-container {
    width: 300px;
    height: 32px;
    padding: 8px 8px 10px 18px;
  }
  
  .player-score-text-container {
    padding: 8px 18px 0 18px;
    gap: 18px;
  }
  
  .player-name,
  .player-elo {
    font-size: 12px;
  }
}

@media (max-width: 900px) {
  .player-score-progress-container {
    width: 280px;
    height: 30px;
    padding: 6px 6px 8px 16px;
  }
  
  .player-score-text-container {
    padding: 6px 16px 0 16px;
    gap: 16px;
  }
  
  .player-name,
  .player-elo {
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .player-score-tracker-container {
    bottom: 8%;
    padding: 0 12px;
  }
  
  .player-score-progress-container {
    width: 260px;
    height: 28px;
    padding: 5px 5px 7px 14px;
  }
  
  .player-score-text-container {
    padding: 5px 14px 0 14px;
    gap: 14px;
  }
  
  .player-name,
  .player-elo {
    font-size: 10px;
  }
}

@media (max-width: 600px) {
  .player-score-tracker-container {
    bottom: 7%;
    padding: 0 10px;
  }
  
  .player-score-progress-container {
    width: 240px;
    height: 26px;
    padding: 4px 4px 6px 12px;
  }
  
  .player-score-text-container {
    padding: 4px 12px 0 12px;
    gap: 12px;
  }
  
  .player-name,
  .player-elo {
    font-size: 9px;
  }
}

@media (max-width: 480px) {
  .player-score-tracker-container {
    bottom: 6%;
    padding: 0 8px;
  }
  
  .player-score-progress-container {
    width: 220px;
    height: 24px;
    padding: 3px 3px 5px 10px;
  }
  
  .player-score-text-container {
    padding: 3px 10px 0 10px;
    gap: 10px;
  }
  
  .player-name,
  .player-elo {
    font-size: 8px;
  }
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

.player-elo {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  text-transform: uppercase;
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
  font-size: 11px;
  font-weight: 700;
  color: var(--yellow);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

/* Status-based styling */
.status-ready .player-status {
  color: #4CAF50;
}

.status-ready {
  border-color: #4CAF50;
  box-shadow: 0 0 10px rgba(76, 175, 80, 0.3);
}

.status-ended .player-status {
  color: #FF9800;
}

.status-ended {
  border-color: #FF9800;
  box-shadow: 0 0 10px rgba(255, 152, 0, 0.3);
}

.status-playing .player-status {
  color: var(--yellow);
}

.status-playing {
  border-color: var(--yellow);
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.3);
}

.round-indicator {
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--dark-grey);
  padding: 8px 20px;
  border-radius: 20px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.round-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 1px;
}
</style>
