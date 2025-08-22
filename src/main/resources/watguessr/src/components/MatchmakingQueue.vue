<template>
  <div class="matchmaking-queue">
    <!-- Queue Status -->
    <div v-if="queueState === 'searching'" class="queue-status searching">
      <h3 class="queue-title">Searching for opponents <span class="dots-animation header-dots"><span>.</span><span>.</span><span>.</span></span></h3>
      <div class="queue-timer">
        <span class="timer-text">Time in queue:</span>
        <span class="timer-value">{{ formatTime(queueTime) }}</span>
      </div>
      
      <button class="cancel-queue-btn" @click="cancelQueue">
        Cancel Search
      </button>
    </div>

    <!-- Match Found -->
    <div v-if="queueState === 'match_found'" class="queue-status match-found">
      <div class="match-found-animation">
        <div class="success-checkmark">✓</div>
      </div>
      <h3>🎮 Match Found!</h3>
      <div class="match-info" v-if="matchInfo">
        <div class="opponent-info">
          <h4>Your Opponent:</h4>
          <div class="opponent-card">
            <span class="opponent-name">{{ matchInfo.opponentName }}</span>
            <span class="opponent-rating">Rating: {{ matchInfo.opponentRating }}</span>
          </div>
        </div>
        <div class="game-details">
          <p>{{ matchInfo.roundCount }} rounds</p>
          <p>⏱{{ matchInfo.timeLimit }}s per round</p>
        </div>
      </div>
      <div class="starting-timer">
        <p>Game starting in:</p>
        <div class="countdown-number">{{ gameStartCountdown }}</div>
      </div>
    </div>

    <!-- Error State -->
    <div v-if="queueState === 'error'" class="queue-status error">
      <div class="error-icon">❌</div>
      <h3>Connection Error</h3>
      <p>{{ errorMessage }}</p>
      <button class="retry-btn" @click="retryConnection">
        Try Again
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MatchmakingQueue',
  props: {
    queueState: {
      type: String,
      default: 'idle', // 'idle', 'searching', 'match_found', 'error'
      validator: (value) => ['idle', 'searching', 'match_found', 'error'].includes(value)
    },
    matchInfo: {
      type: Object,
      default: null
    },
    errorMessage: {
      type: String,
      default: 'An error occurred while searching for a match'
    }
  },
  data() {
    return {
      queueTime: 0,
      queueTimer: null,
      gameStartCountdown: 5,
      gameStartTimer: null
    };
  },
  watch: {
    queueState(newState, oldState) {
      console.log(`🔄 Queue state changed: ${oldState} → ${newState}`);

      if (newState === 'searching' && oldState !== 'searching') {
        console.log('⏱️ Starting queue timer');
        this.startQueueTimer();
      } else if (newState !== 'searching' && oldState === 'searching') {
        console.log('⏱️ Stopping queue timer');
        this.stopQueueTimer();
      }

      if (newState === 'match_found' && oldState !== 'match_found') {
        console.log('⏰ Starting game countdown');
        this.startGameCountdown();
      } else if (newState !== 'match_found' && oldState === 'match_found') {
        console.log('⏰ Stopping game countdown');
        this.stopGameCountdown();
      }
    }
  },
  mounted() {
    // Start timers if component is mounted in the right state
    if (this.queueState === 'searching') {
      console.log('🔄 Component mounted in searching state, starting timer');
      this.startQueueTimer();
    }
    if (this.queueState === 'match_found') {
      console.log('🔄 Component mounted in match_found state, starting countdown');
      this.startGameCountdown();
    }
  },
  methods: {
    startQueueTimer() {
      console.log('⏱️ Starting queue timer');
      this.stopQueueTimer(); // Stop any existing timer
      this.queueTime = 0;
      this.queueTimer = setInterval(() => {
        this.queueTime++;
        // Only log every 10 seconds to reduce noise
        if (this.queueTime % 10 === 0) {
          console.log(`⏱️ Queue time: ${this.queueTime}s`);
        }
      }, 1000);
    },

    stopQueueTimer() {
      if (this.queueTimer) {
        console.log('⏹️ Stopping queue timer');
        clearInterval(this.queueTimer);
        this.queueTimer = null;
      }
    },

    startGameCountdown() {
      console.log('⏰ Starting game countdown from 5');
      this.stopGameCountdown(); // Stop any existing countdown
      this.gameStartCountdown = 5;
      this.gameStartTimer = setInterval(() => {
        this.gameStartCountdown--;
        console.log(`⏰ Game countdown: ${this.gameStartCountdown}`);
        if (this.gameStartCountdown <= 0) {
          this.stopGameCountdown();
          console.log('🎮 Emitting game-start event');
          this.$emit('game-start');
        }
      }, 1000);
    },

    stopGameCountdown() {
      if (this.gameStartTimer) {
        clearInterval(this.gameStartTimer);
        this.gameStartTimer = null;
      }
    },

    formatTime(seconds) {
      const mins = Math.floor(seconds / 60);
      const secs = seconds % 60;
      return `${mins}:${secs.toString().padStart(2, '0')}`;
    },

    cancelQueue() {
      this.$emit('cancel-queue');
    },

    retryConnection() {
      this.$emit('retry-connection');
    }
  },

  beforeUnmount() {
    this.stopQueueTimer();
    this.stopGameCountdown();
  }
};
</script>

<style scoped>
.matchmaking-queue {
  width: 100%;
}

.queue-status {
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  color: white;
  transition: all 0.3s ease;
}

.queue-status.searching { background: none; border: none; backdrop-filter: none; }

.queue-status.match-found { background: none; border: none; }

.queue-status.error { background: none; border: none; }

.queue-animation {
  margin-bottom: 16px;
}

.search-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(33, 150, 243, 0.3);
  border-top: 3px solid #2196F3;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

.match-found-animation {
  margin-bottom: 16px;
}

.success-checkmark {
  width: 60px;
  height: 60px;
  background: #4CAF50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: white;
  margin: 0 auto;
  animation: bounce 0.6s ease-out;
}

.error-icon {
  font-size: 40px;
  margin-bottom: 16px;
}

.queue-status h3 {
  margin: 0 0 12px 0;
  font-size: 1.2rem;
  font-weight: 800;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: var(--white);
}

.queue-timer {
  margin: 16px 0;
  padding: 12px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.timer-text {
  color: var(--light-grey);
  font-size: 0.85rem;
  text-transform: none;
}

.timer-value {
  font-size: 1.1rem;
  font-weight: 900;
  color: #FFB366; /* ranked accent */
  font-family: monospace;
}

.queue-details {
  margin: 8px 0 12px 0;
  color: var(--light-grey);
  font-family: "Red Hat Text", sans-serif;
}

.dots-animation {
  margin-top: 8px;
}

.dots-animation span {
  animation: dots 1.4s infinite ease-in-out both;
  font-size: 1.5rem;
  color: var(--white);
}

.header-dots { display: inline-flex; gap: 2px; margin-left: 4px; }

.dots-animation span:nth-child(1) { animation-delay: -0.32s; }
.dots-animation span:nth-child(2) { animation-delay: -0.16s; }

.match-info {
  margin: 20px 0;
  text-align: left;
}

.opponent-info h4 {
  margin: 0 0 8px 0;
  color: var(--white);
  text-align: center;
}

.opponent-card {
  background: rgba(255, 255, 255, 0.1);
  padding: 12px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.opponent-name {
  font-weight: 800;
  letter-spacing: 0.3px;
  color: var(--white);
}

.opponent-rating {
  color: var(--light-grey);
  font-weight: 700;
}

.game-details {
  text-align: center;
  color: var(--text-secondary);
}

.game-details p {
  margin: 4px 0;
}

.starting-timer {
  margin: 20px 0;
}

.starting-timer p {
  margin: 0 0 8px 0;
  color: var(--text-secondary);
}

.countdown-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: #4CAF50;
  animation: countdown-pulse 1s infinite;
}

.cancel-queue-btn, .retry-btn {
  background: rgba(255, 255, 255, 0.1);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 16px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* CANCEL: white shine + translate only */
.cancel-queue-btn { position: relative; overflow: hidden; }
.cancel-queue-btn::before {
  content: '';
  position: absolute;
  top: 0; left: -100%;
  width: 100%; height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.25), transparent);
  transition: left 0.5s ease;
}
.cancel-queue-btn:hover { transform: translateY(-1px); }
.cancel-queue-btn:hover::before { left: 100%; }

/* RETRY: keep ranked accent hover */
.retry-btn:hover {
  background: #FFB366; /* ranked accent */
  color: var(--dark-grey);
  border-color: #FFB366;
  transform: translateY(-1px);
}

/* Animations */
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes bounce {
  0%, 20%, 53%, 80%, 100% {
    transform: translate3d(0, 0, 0);
  }
  40%, 43% {
    transform: translate3d(0, -8px, 0);
  }
  70% {
    transform: translate3d(0, -4px, 0);
  }
  90% {
    transform: translate3d(0, -2px, 0);
  }
}

@keyframes dots {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes pulse-green {
  0% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(76, 175, 80, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
  }
}

@keyframes countdown-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

/* Responsive design */
@media (max-width: 768px) {
  .queue-status {
    padding: 20px;
  }

  .queue-status h3 {
    font-size: 1.1rem;
  }

  .opponent-card {
    flex-direction: column;
    gap: 8px;
    text-align: center;
  }

  .countdown-number {
    font-size: 2rem;
  }
}

/* Typography refinements */
.queue-status p {
  color: var(--light-grey);
  font-size: 0.95rem;
  line-height: 1.5;
  font-family: "Red Hat Text", sans-serif;
}

.timer-value { letter-spacing: 0.5px; text-shadow: 0 1px 2px rgba(0,0,0,0.25); }
</style>
