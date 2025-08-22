<template>
  <div class="game-end-container">
    <div class="game-header">
      <span class="game-label">GAME OVER</span>
      <div v-if="finalLeaderboard.length > 0" class="winner-announcement">
        🏆 {{ winnerName }} Wins!
      </div>
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
          :class="{
            highlight: player.id === myPlayerId,
            winner: index === 0,
            'top-three': index < 3
          }"
        >
          <td>
            <span class="rank">
              <span v-if="index === 0" class="trophy">🥇</span>
              <span v-else-if="index === 1" class="trophy">🥈</span>
              <span v-else-if="index === 2" class="trophy">🥉</span>
              <span v-else>{{ index + 1 }}</span>
            </span>
          </td>
          <td>
            {{ player.name }}
            <span v-if="player.id === myPlayerId" class="you-tag">(YOU)</span>
          </td>
          <td>{{ player.totalPoints }} PTS</td>
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
        <span class="stat-label">YOUR SCORE</span>
        <span class="stat-value">{{ bestRoundPoints }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">PLAYERS</span>
        <span class="stat-value">{{ finalLeaderboard.length }}</span>
      </div>
    </div>

    <div class="button-section">
      <button class="btn home-btn" @click="goLeaderboard">
        🏆 Leaderboard
      </button>
      <button class="btn home-btn" @click="goHome">
        🏠 Home
      </button>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from "vuex";

export default {
  name: "MultiplayerGameEnd",
  computed: {
    ...mapGetters("multiplayerGame", [
      "multiplayerGame_getPlayers",
      "multiplayerGame_getCurrentRound",
      "multiplayerGame_getMaxRounds",
      "multiplayerGame_getFinalWinner"
    ]),
    ...mapGetters("user", ["getCurrentUser"]),
    myPlayerId() {
      return this.getCurrentUser?.id;
    },
    finalLeaderboard() {
      if (!this.multiplayerGame_getPlayers) return [];

      // Convert players object to array and sort by score
      return Object.entries(this.multiplayerGame_getPlayers)
        .map(([playerId, playerData]) => ({
          id: playerId,
          name: this.getPlayerName(playerId),
          totalPoints: playerData.score || 0,
          status: playerData.status
        }))
        .sort((a, b) => b.totalPoints - a.totalPoints);
    },
    totalRounds() {
      return this.multiplayerGame_getMaxRounds || 0;
    },
    bestRoundPoints() {
      // Get the current player's best score
      const myPlayerData = this.multiplayerGame_getPlayers?.[this.myPlayerId];
      return myPlayerData?.score || 0;
    },
    winnerName() {
      const winner = this.finalLeaderboard[0];
      return winner ? winner.name : "Unknown";
    }
  },
  watch: {
    multiplayerGame_getPlayers: {
      immediate: true
    }
  },
  mounted() {
    // Load final game data from localStorage if store is empty
    if (!this.multiplayerGame_getPlayers || Object.keys(this.multiplayerGame_getPlayers).length === 0) {
      this.multiplayerGame_loadFinalGameData();
    }
  },
  beforeUnmount() {
    // Clean up localStorage data when leaving
    this.multiplayerGame_clearFinalGameData();
  },
  methods: {
    ...mapActions("multiplayerGame", [
      "multiplayerGame_disconnect",
      "multiplayerGame_loadFinalGameData",
      "multiplayerGame_clearFinalGameData"
    ]),
    getPlayerName(playerId) {
      const player = this.multiplayerGame_getPlayers[playerId];
      return player?.username || `Player`;
    },
    goLeaderboard() {
      // Disconnect from current game and return to lobby
      this.multiplayerGame_disconnect();
      this.$router.push("/leaderboard");
    },
    goHome() {
      this.multiplayerGame_disconnect();
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

.winner-announcement {
  font-size: 24px;
  font-weight: 700;
  color: #4CAF50;
  margin-top: 12px;
  text-shadow: 0 0 10px rgba(76, 175, 80, 0.3);
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
  border: 2px solid var(--yellow);
}

.winner {
  background-color: rgba(76, 175, 80, 0.2);
  border: 2px solid #4CAF50;
}

.top-three {
  font-weight: 700;
}

.trophy {
  font-size: 18px;
}

.rank {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
}

.you-tag {
  font-size: 12px;
  color: var(--yellow);
  font-weight: 700;
  margin-left: 8px;
}

.stats-section {
  display: flex;
  gap: 40px;
}

.stat-item {
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

/* Responsive styling for different laptop/desktop sizes */
@media (max-width: 1400px) {
  .game-end-container {
    gap: 20px;
    padding: 18px;
  }
  
  .game-label {
    font-size: 38px;
  }
  
  .winner-announcement {
    font-size: 22px;
    margin-top: 10px;
  }
  
  .leaderboard-section {
    max-width: 450px;
  }
  
  .leaderboard-title {
    font-size: 18px;
  }
  
  .stats-section {
    gap: 32px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .btn {
    padding: 9px 18px;
    font-size: 15px;
  }
}

@media (max-width: 1200px) {
  .game-end-container {
    gap: 18px;
    padding: 16px;
  }
  
  .game-label {
    font-size: 34px;
  }
  
  .winner-announcement {
    font-size: 20px;
    margin-top: 8px;
  }
  
  .leaderboard-section {
    max-width: 400px;
  }
  
  .leaderboard-title {
    font-size: 17px;
  }
  
  .stats-section {
    gap: 28px;
  }
  
  .stat-value {
    font-size: 17px;
  }
  
  .btn {
    padding: 8px 16px;
    font-size: 14px;
  }
}

@media (max-width: 1024px) {
  .game-end-container {
    gap: 16px;
    padding: 14px;
  }
  
  .game-label {
    font-size: 30px;
  }
  
  .winner-announcement {
    font-size: 18px;
    margin-top: 6px;
  }
  
  .leaderboard-section {
    max-width: 360px;
  }
  
  .leaderboard-title {
    font-size: 16px;
  }
  
  .stats-section {
    gap: 24px;
  }
  
  .stat-value {
    font-size: 16px;
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
  .game-end-container {
    gap: 14px;
    padding: 12px;
  }
  
  .game-label {
    font-size: 26px;
  }
  
  .winner-announcement {
    font-size: 16px;
    margin-top: 4px;
  }
  
  .leaderboard-section {
    max-width: 320px;
  }
  
  .leaderboard-title {
    font-size: 15px;
  }
  
  .stats-section {
    gap: 20px;
  }
  
  .stat-value {
    font-size: 15px;
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
