<template>
  <div class="game-end-container">
    <div class="game-header">
      <span class="game-label">GAME OVER</span>
    </div>

    <div class="leaderboard-section">
      <h3 class="leaderboard-title">FINAL LEADERBOARD</h3>
      <table class="leaderboard-table">
        <thead>
        <tr>
          <th>#</th>
          <th>Player</th>
          <th>Total Points</th>
        </tr>
        </thead>
        <tbody>
        <tr
          v-for="(player, index) in finalLeaderboard"
          :key="player.id"
          :class="{ highlight: player.id === myPlayerId }"
        >
          <td>{{ index + 1 }}</td>
          <td>{{ player.name }}</td>
          <td>{{ player.totalPoints }}</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="stats-section">
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
  name: "MultiplayerGameEnd",
  computed: {
    ...mapGetters("game", ["getPlayers", "getScores", "getCurrentRound"]),
    myPlayerId() {
      return this.$store.state.playerId;
    },
    finalLeaderboard() {
      //TODO
      // build array: [{id, name, totalPoints}, ...] sorted by points
      // return this.getPlayers
      //   .map(p => ({
      //     id: p.id,
      //     name: p.name,
      //     totalPoints: this.getScores[p.id] ?? 0
      //   }))
      //   .sort((a, b) => b.totalPoints - a.totalPoints);
    },
    totalRounds() {
      return this.getCurrentRound ?? 0;
    },
    bestRoundPoints() {
      // Replace with actual best round logic if stored
      const myScores = Object.values(this.getScores || {});
      return myScores.length ? Math.max(...myScores) : "-";
    }
  },
  methods: {
    ...mapActions("game", ["endGame"]),
    restartGame() {
      this.endGame();
      this.$router.push("/play");
    },
    goHome() {
      this.$router.push("/");
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
  padding: 20px;
}

.game-header {
  text-align: center;
}

.game-label {
  font-size: 42px;
  font-weight: 900;
  color: var(--yellow);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.leaderboard-section {
  width: 100%;
  max-width: 500px;
}

.leaderboard-title {
  text-align: center;
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 10px;
  color: var(--yellow);
}

.leaderboard-table {
  width: 100%;
  border-collapse: collapse;
}

.leaderboard-table th,
.leaderboard-table td {
  padding: 8px;
  text-align: left;
}

.leaderboard-table th {
  background-color: #333;
  font-weight: 700;
}

.leaderboard-table tr:nth-child(even) {
  background-color: #2a2a2a;
}

.highlight {
  background-color: rgba(255, 204, 0, 0.2);
}

.stats-section {
  display: flex;
  gap: 40px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #bbb;
  font-weight: 700;
}

.stat-value {
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
