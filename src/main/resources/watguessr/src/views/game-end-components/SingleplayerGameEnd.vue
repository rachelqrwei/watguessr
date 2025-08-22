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
    ...mapGetters("singleplayerGame", ["singleplayerGame_getCurrentRound"]),
    totalRounds() {
      return this.singleplayerGame_getCurrentRound ?? 0;
    },
  },
  methods: {
    ...mapActions("singleplayerGame", { doRestartGame: "singleplayerGame_restartGame" }),
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
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
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
