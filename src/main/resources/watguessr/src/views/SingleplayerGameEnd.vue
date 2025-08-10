<template>
  <div class="game-end-container">
    <div class="game-header">
      <span class="game-label">GAME OVER</span>
    </div>

    <div class="final-points-section">
      <span class="final-points-label">FINAL SCORE</span>
      <span class="final-points-value">{{ finalScore }}</span>
    </div>

    <div class="final-stats-section">
      <div class="stat-item">
        <span class="stat-label">ROUNDS PLAYED</span>
        <span class="stat-value">{{ totalRounds }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">BEST ROUND</span>
        <span class="stat-value">{{ bestRoundPoints }}</span>
      </div>
    </div>

    <div class="button-section">
      <button class="btn restart-btn" @click="restartGame">
        🔄 Restart Game
      </button>
      <button class="btn home-btn" @click="goHome">
        🏆 Leaderboard / Home
      </button>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "GameEnd",
  computed: {
    ...mapGetters("game", ["getScores", "getCurrentRound"]),
    ...mapGetters("round", ["getRoundResult"]),
    finalScore() {
      return this.getScores['undefined'] ?? 0;
    },
    totalRounds() {
      return this.getCurrentRound ?? 0;
    },
    bestRoundPoints() {
      // Example: you might pull from a stats module
      return this.getFinalWinner?.bestRound ?? "-";
    }
  },
  methods: {
    ...mapActions("game", ["endGame"]),
    restartGame() {
      this.endGame();

      this.$router.push('/play');
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
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #1a1a1c;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
  z-index: 1;
}

.game-header {
  text-align: center;
}

.game-label {
  display: block;
  font-size: 42px;
  font-weight: 900;
  color: var(--yellow);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.final-points-section {
  text-align: center;
}

.final-points-label {
  display: block;
  font-size: 16px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
  margin-bottom: 5px;
}

.final-points-value {
  display: block;
  font-size: 36px;
  font-weight: 900;
  color: #fff;
}

.final-stats-section {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 900;
  color: var(--yellow);
}

.button-section {
  display: flex;
  gap: 15px;
}

.btn {
  background: var(--yellow);
  color: #1a1a1c;
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 800;
  font-size: 16px;
  cursor: pointer;
  border: none;
  transition: transform 0.1s ease;
}

.btn:hover {
  transform: scale(1.05);
}

.restart-btn {
  background-color: #ffcb3b;
}

.home-btn {
  background-color: #ffe066;
}
</style>
