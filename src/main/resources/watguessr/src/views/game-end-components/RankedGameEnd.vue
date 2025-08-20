<template>
  <div class="game-end-container">
    <div class="game-end-panel">
      <div class="game-header">
        <span class="game-label">RANKED GAME COMPLETE</span>
        <div v-if="winner" class="winner-announcement">
          🏆 {{ winner === 'player1' ? 'YOU WIN!' : 'OPPONENT WINS!' }}
        </div>
      </div>

      <!-- Final Results Section -->
      <div class="results-section">
        <div class="player-result player-1">
          <div class="player-header">
            <span class="player-name">YOU</span>
            <span class="player-elo">{{ player1Elo }} ELO</span>
          </div>
          <div class="player-score">{{ player1Score }} PTS</div>
          <div class="elo-change" :class="{ 'positive': player1EloChange > 0, 'negative': player1EloChange < 0 }">
            {{ player1EloChange > 0 ? '+' : '' }}{{ player1EloChange }} ELO
          </div>
        </div>

        <div class="vs-divider">
          <span class="vs-text">VS</span>
        </div>

        <div class="player-result player-2">
          <div class="player-header">
            <span class="player-name">OPPONENT</span>
            <span class="player-elo">{{ player2Elo }} ELO</span>
          </div>
          <div class="player-score">{{ player2Score }} PTS</div>
          <div class="elo-change" :class="{ 'positive': player2EloChange > 0, 'negative': player2EloChange < 0 }">
            {{ player2EloChange > 0 ? '+' : '' }}{{ player2EloChange }} ELO
          </div>
        </div>
      </div>

      <!-- Game Stats Section -->
      <div class="stats-section">
        <div class="stat-item">
          <span class="stat-label">ROUNDS PLAYED</span>
          <span class="stat-value">{{ totalRounds }}/5</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">GAME DURATION</span>
          <span class="stat-value">{{ gameDuration }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">YOUR BEST ROUND</span>
          <span class="stat-value">{{ bestRoundScore }} PTS</span>
        </div>
      </div>

      <!-- ELO Summary -->
      <div class="elo-summary">
        <div class="elo-summary-header">
          <span class="elo-summary-title">ELO CHANGES</span>
        </div>
        <div class="elo-summary-content">
          <div class="elo-summary-item">
            <span class="elo-summary-label">Starting ELO:</span>
            <span class="elo-summary-value">{{ startingElo }}</span>
          </div>
          <div class="elo-summary-item">
            <span class="elo-summary-label">New ELO:</span>
            <span class="elo-summary-value">{{ newElo }}</span>
          </div>
          <div class="elo-summary-item total">
            <span class="elo-summary-label">Total Change:</span>
            <span class="elo-summary-value" :class="{ 'positive': totalEloChange > 0, 'negative': totalEloChange < 0 }">
              {{ totalEloChange > 0 ? '+' : '' }}{{ totalEloChange }}
            </span>
          </div>
        </div>
      </div>

      <!-- Button Section -->
      <div class="button-section">
        <button class="btn rematch-btn" @click="rematch">
          ⚔️ Rematch
        </button>
        <button class="btn ranked-btn" @click="playRanked">
          🏆 Play Another Ranked
        </button>
        <button class="btn home-btn" @click="goHome">
          🏠 Back to Home
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "RankedGameEnd",
  data() {
    return {
      // Hardcoded values for now - will be replaced with real data later
      player1Score: 1250,
      player2Score: 980,
      player1Elo: 1250,
      player2Elo: 1180,
      player1EloChange: 25,
      player2EloChange: -25,
      winner: 'player1', // 'player1' or 'player2'
      totalRounds: 5,
      gameDuration: '8:45',
      bestRoundScore: 450,
      startingElo: 1250,
      newElo: 1275,
      totalEloChange: 25
    };
  },
  computed: {
    ...mapGetters('gameInfo', [
      'getGameMode'
    ]),
    ...mapGetters('user', ["getCurrentUser"])
  },
  methods: {
    ...mapActions('singleplayerGame', {
      doRestartGame: "singleplayerGame_restartGame"
    }),

    rematch() {
      // TODO: Implement rematch functionality
      this.$router.push('/play?mode=ranked');
    },

    playRanked() {
      // TODO: Start a new ranked game
      this.$router.push('/play?mode=ranked');
    },

    goHome() {
      this.$router.push('/');
    }
  }
};
</script>

<style scoped>
.game-end-container {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(8px) saturate(120%);
  -webkit-backdrop-filter: blur(8px) saturate(120%);
  z-index: 1;
}

.game-end-panel {
  width: 95%;
  max-width: 800px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
  padding: 32px 24px 28px;
  border-radius: 16px;
  background: var(--dark-grey);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.game-header {
  text-align: center;
}

.game-label {
  display: block;
  font-size: 36px;
  font-weight: 900;
  color: var(--yellow);
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

.winner-announcement {
  font-size: 24px;
  font-weight: 700;
  color: var(--white);
  margin-top: 8px;
}

/* Results Section */
.results-section {
  display: flex;
  align-items: center;
  gap: 32px;
  width: 100%;
  justify-content: center;
}

.player-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 200px;
}

.player-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.player-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--white);
  text-transform: uppercase;
}

.player-elo {
  font-size: 14px;
  font-weight: 500;
  color: var(--light-grey);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.player-score {
  font-size: 32px;
  font-weight: 900;
  color: var(--yellow);
}

.elo-change {
  font-size: 16px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.elo-change.positive {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.elo-change.negative {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.vs-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--dark-grey);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px solid var(--light-grey);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.vs-text {
  font-size: 20px;
  font-weight: 700;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* Stats Section */
.stats-section {
  display: flex;
  gap: 32px;
  justify-content: center;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.stat-label {
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

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--white);
}

/* ELO Summary */
.elo-summary {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.elo-summary-header {
  text-align: center;
  margin-bottom: 16px;
}

.elo-summary-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.elo-summary-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.elo-summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.elo-summary-item:last-child {
  border-bottom: none;
}

.elo-summary-item.total {
  border-top: 2px solid rgba(255, 255, 255, 0.2);
  padding-top: 12px;
  font-weight: 700;
}

.elo-summary-label {
  font-size: 14px;
  color: var(--light-grey);
  font-weight: 500;
}

.elo-summary-value {
  font-size: 16px;
  font-weight: 600;
  color: var(--white);
}

.elo-summary-value.positive {
  color: #22c55e;
}

.elo-summary-value.negative {
  color: #ef4444;
}

/* Button Section */
.button-section {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.rematch-btn {
  background: var(--yellow);
  color: var(--dark-grey);
}

.rematch-btn:hover {
  background: #e6b800;
  transform: translateY(-2px);
}

.ranked-btn {
  background: rebeccapurple;
  color: var(--white);
}

.ranked-btn:hover {
  background: #2563eb;
  transform: translateY(-2px);
}

.home-btn {
  background: var(--light-grey);
  color: var(--dark-grey);
}

.home-btn:hover {
  background: #9ca3af;
  transform: translateY(-2px);
}

/* Responsive Design */
@media (max-width: 768px) {
  .results-section {
    flex-direction: column;
    gap: 24px;
  }

  .vs-divider {
    width: 60px;
    height: 60px;
  }

  .vs-text {
    font-size: 16px;
  }

  .stats-section {
    gap: 20px;
  }

  .button-section {
    flex-direction: column;
    width: 100%;
  }

  .btn {
    width: 100%;
  }
}
</style>
