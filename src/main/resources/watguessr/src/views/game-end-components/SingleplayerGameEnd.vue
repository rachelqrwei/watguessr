<template>
  <div class="game-end-container">
    <div class="game-end-panel">
      <div class="game-header">
        <span class="game-label">GAME OVER</span>
      </div>

      <div class="final-points-section">
        <span class="final-points-label">ROUNDS SURVIVED</span>
        <span class="final-points-value">{{ totalRounds }}</span>
      </div>

      <!-- Additional stats removed for singleplayer minimal display -->

      <div class="button-section">
        <button class="btn restart-btn" @click="restartGame">
          🔄 Restart Game
        </button>
        <button class="btn home-btn" @click="goHome">
          🏆 Leaderboard / Home
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "GameEnd",
  computed: {
    ...mapGetters("singleplayer", ["singleplayerGame_getCurrentRound"]),
    totalRounds() {
      return this.singleplayerGame_getCurrentRound ?? 0;
    },
  },
  methods: {
    ...mapActions("singleplayer", { doRestartGame: "singleplayerGame_restartGame" }),
    async restartGame() {
      await this.doRestartGame();
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
  max-width: 740px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 28px 24px 24px;
  border-radius: 16px;
  background: var(--dark-grey);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
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
