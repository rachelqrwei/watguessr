<template>
  <div class="game-end-background" aria-hidden="true"></div>

  <!-- Home Button -->
  <div class="page-logo">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>

  <div class="game-end-container">
    <div class="game-end-panel">
      <div class="game-header">
        <h1>GAME OVER</h1>
      </div>

      <div class="final-points-section">
        <span class="final-points-label">ROUNDS SURVIVED</span>
        <span class="final-points-value">{{ totalRounds }}</span>
      </div>

      <div class="rounds-section" v-if="rounds.length > 0">
        <div class="rounds-header">ROUND DETAILS</div>
        <div class="rounds-container">
          <div v-if="isLoadingRounds" class="loading">
            <div class="loading-spinner"></div>
            <span>Loading rounds…</span>
          </div>
          <div v-else-if="roundsError" class="error">{{ roundsError }}</div>
          <div v-else class="round-list">
            <div v-for="(round, idx) in rounds" :key="round.id" class="round-item">
              <div class="round-header">Round {{ idx + 1 }}</div>
              <div class="round-details">
                <div class="round-info">
                  <div v-if="round.guess" class="guess-info">
                    <span class="pill" :class="{ positive: (round.guess.points ?? 0) > 0, negative: (round.guess.points ?? 0) < 0 }">
                      {{ round.guess.points ?? 0 }} pts
                    </span>
                    <span class="pill">{{ timeDisplay(round.guess.time) }}</span>
                  </div>
                  <div v-else class="no-guess">No guess made</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="button-section">
        <button class="btn restart-btn" @click="restartGame">
          RESTART GAME
        </button>
        <button class="btn home-btn" @click="goHome">
          RETURN HOME
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "GameEnd",
  data() {
    return {
      rounds: [],
      isLoadingRounds: false,
      roundsError: null
    }
  },
  computed: {
    ...mapGetters("singleplayerGame", ["singleplayerGame_getCurrentRound", "singleplayerGame_getGameId"]),
    totalRounds() {
      return this.singleplayerGame_getCurrentRound ?? 0;
    },
    gameId() {
      return this.singleplayerGame_getGameId;
    }
  },
  mounted() {
    this.loadRounds();
  },
  methods: {
    ...mapActions("singleplayerGame", { doRestartGame: "singleplayerGame_restartGame" }),
    async restartGame() {
      await this.doRestartGame();
      this.$router.push({name: "play", query: {gameMode: 'singleplayer'}});
    },
    goHome() {
      this.$router.push('/');
    },
    async loadRounds() {
      if (!this.gameId) return;

      this.isLoadingRounds = true;
      this.roundsError = null;
      this.rounds = [];

      try {
        const res = await fetch(`/api/round/by-game-with-guesses?gameId=${this.gameId}`);
        if (!res.ok) throw new Error('Failed to fetch round details');

        const payload = await res.json();
        const list = Array.isArray(payload) ? payload : [];

        // Build rounds array with guess data
        this.rounds = list.map(item => ({
          id: item.roundId,
          guess: Array.isArray(item.guesses) && item.guesses.length > 0 ? item.guesses[0] : null
        }));

      } catch (e) {
        this.roundsError = e instanceof Error ? e.message : 'Failed to load round details';
      } finally {
        this.isLoadingRounds = false;
      }
    },
    timeDisplay(ms) {
      if (typeof ms !== 'number') return '0s';
      const seconds = Math.round(ms / 1000);
      return `${seconds}s`;
    }
  }
};
</script>

<style scoped>
.page-logo {
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
  color: var(--yellow);
}

.logo-text {
  text-decoration: none;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--white);
  outline: none;
}

.logo-text:hover {
  color: var(--yellow);
  transition: color 0.3s ease;
}

.game-end-background {
  position: fixed;
  inset: 0;
  background: var(--dark-grey);
  z-index: -1;
}

.game-end-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/ProfilePage.png') center top / cover no-repeat;
  opacity: 0.8;
  pointer-events: none;
}

.game-end-container {
  min-height: 100vh;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.game-end-panel {
  width: 100%;
  max-width: 600px;
  background: rgba(42, 42, 44, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(8px);
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
}

.game-header {
  text-align: center;
  margin-bottom: 5px;
}

.game-header h1 {
  font-size: 1.8rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.final-points-section {
  text-align: center;
  width: 100%;
  max-width: 300px;
}

.final-points-label {
  display: block;
  font-size: 0.85rem;
  color: var(--light-grey);
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 12px;
  font-family: "Red Hat Text", sans-serif;
}

.final-points-value {
  display: block;
  font-size: 2.5rem;
  font-weight: 900;
  color: var(--yellow);
}

.button-section {
  display: flex;
  gap: 20px;
  width: 100%;
  justify-content: center;
  flex-wrap: wrap;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: var(--white);
  font-weight: 600;
  font-size: 0.81rem;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  position: relative;
  overflow: hidden;
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.btn:hover::before {
  left: 100%;
}

.restart-btn {
  background: var(--yellow);
  color: var(--dark-grey);
  border-color: var(--yellow);
}

.rounds-section {
  width: 100%;
  max-width: 500px;
}

.rounds-header {
  text-align: center;
  font-size: 0.85rem;
  color: var(--light-grey);
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  margin-bottom: 16px;
  font-family: "Red Hat Text", sans-serif;
}

.rounds-container {
  max-height: 300px;
  overflow-y: auto;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.loading,
.error {
  padding: 20px;
  text-align: center;
  color: var(--light-grey);
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top: 2px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.round-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
}

.round-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px;
}

.round-header {
  font-weight: 700;
  font-size: 0.9rem;
  margin-bottom: 8px;
  color: var(--white);
}

.round-details {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.round-info {
  width: 100%;
}

.guess-info {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.no-guess {
  color: var(--light-grey);
  font-style: italic;
  font-size: 0.85rem;
}

.pill {
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  font-weight: 700;
  font-size: 0.8rem;
  color: var(--white);
}

.pill.positive {
  background: rgba(182, 255, 127, 0.15);
  color: #B6FF7F;
  border-color: rgba(182, 255, 127, 0.35);
}

.pill.negative {
  background: rgba(255, 127, 127, 0.15);
  color: #FF7F7F;
  border-color: rgba(255, 127, 127, 0.35);
}

.rounds-container::-webkit-scrollbar {
  width: 6px;
}

.rounds-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.rounds-container::-webkit-scrollbar-thumb {
  background: rgba(255, 203, 59, 0.3);
  border-radius: 3px;
}

.rounds-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 203, 59, 0.5);
}

@media (max-width: 768px) {
  .game-end-panel {
    padding: 30px 20px;
    gap: 24px;
  }

  .game-header h1 {
    font-size: 1.5rem;
  }

  .final-points-value {
    font-size: 2rem;
  }

  .button-section {
    flex-direction: column;
    align-items: stretch;
  }

  .rounds-container {
    max-height: 250px;
  }

  .rounds-section {
    max-width: none;
  }
}

/* Responsive styling for different laptop/desktop sizes */
@media (max-width: 1400px) {
  .game-end-panel {
    max-width: 680px;
    padding: 24px 20px 20px;
    gap: 14px;
  }

  .game-label {
    font-size: 38px;
  }

  .final-points-value {
    font-size: 32px;
  }

  .final-points-label {
    font-size: 15px;
  }

  .btn {
    padding: 9px 18px;
    font-size: 15px;
  }
}

@media (max-width: 1200px) {
  .game-end-panel {
    max-width: 600px;
    padding: 22px 18px 18px;
    gap: 12px;
  }

  .game-label {
    font-size: 34px;
  }

  .final-points-value {
    font-size: 28px;
  }

  .final-points-label {
    font-size: 14px;
  }

  .btn {
    padding: 8px 16px;
    font-size: 14px;
  }
}

@media (max-width: 1024px) {
  .game-end-panel {
    max-width: 540px;
    padding: 20px 16px 16px;
    gap: 10px;
  }

  .game-label {
    font-size: 30px;
  }

  .final-points-value {
    font-size: 24px;
  }

  .final-points-label {
    font-size: 13px;
  }

  .btn {
    padding: 7px 14px;
    font-size: 13px;
  }

  .button-section {
    gap: 12px;
  }
}

@media (max-width: 900px) {
  .game-end-panel {
    max-width: 480px;
    padding: 18px 14px 14px;
    gap: 8px;
  }

  .game-label {
    font-size: 26px;
  }

  .final-points-value {
    font-size: 20px;
  }

  .final-points-label {
    font-size: 12px;
  }

  .btn {
    padding: 6px 12px;
    font-size: 12px;
  }

  .button-section {
    gap: 10px;
  }
}
</style>
