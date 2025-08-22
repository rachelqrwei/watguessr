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

      <div class="leaderboard-section">
        <h3 class="leaderboard-title">FINAL LEADERBOARD</h3>
        <div class="leaderboard-table">
          <div class="table-header">
            <div class="rank-col">Rank</div>
            <div class="player-col">Player</div>
            <div class="points-col">Points</div>
          </div>
          <div class="table-body">
            <div
              v-for="(player, index) in finalLeaderboard"
              :key="player.id"
              class="table-row"
              :class="{
                highlight: player.id === myPlayerId,
                winner: index === 0,
                'top-three': index < 3
              }"
            >
              <div class="rank-col">
                <div class="rank-badge" :class="getRankClass(index + 1)">
                  {{ index + 1 }}
                </div>
              </div>
              <div class="player-col">
                <div class="player-info">
                  <div class="player-name">{{ player.name }}</div>
                  <span v-if="player.id === myPlayerId" class="you-tag">(YOU)</span>
                </div>
              </div>
              <div class="points-col">
                <div class="points-value">{{ player.totalPoints }}</div>
              </div>
            </div>
          </div>
        </div>
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
          LEADERBOARD
        </button>
        <button class="btn home-btn" @click="goHome">
          HOME
        </button>
      </div>
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
    },
    getRankClass(rank) {
      if (rank === 1) return 'rank-1'
      if (rank === 2) return 'rank-2'
      if (rank === 3) return 'rank-3'
      return ''
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
  max-width: 800px;
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



.leaderboard-section {
  width: 100%;
  max-width: 600px;
}

.leaderboard-title {
  text-align: center;
  font-size: 1.1rem;
  font-weight: 800;
  margin-bottom: 20px;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.leaderboard-table {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  overflow: hidden;
  padding: 16px;
}

.table-header {
  display: grid;
  grid-template-columns: 80px 1fr 100px;
  gap: 12px;
  padding: 16px 12px 20px 12px;
  color: var(--white);
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 8px;
  align-items: center;
  font-weight: 900;
}

.table-body {
  display: flex;
  flex-direction: column;
}

.table-row {
  display: grid;
  grid-template-columns: 80px 1fr 100px;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
  margin: 8px 0;
}

.table-row:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.07);
}

.table-row.highlight {
  background: rgba(255, 203, 59, 0.2);
  border-color: var(--yellow);
}

.table-row.winner {
  background: rgba(255, 203, 59, 0.1);
}

.rank-col {
  display: flex;
  align-items: center;
  justify-content: center;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: var(--white);
  background: rgba(255, 255, 255, 0.2);
}

.rank-badge.rank-1 {
  background: #FFD700;
  color: var(--dark-grey);
}

.rank-badge.rank-2 {
  background: #C0C0C0;
  color: var(--dark-grey);
}

.rank-badge.rank-3 {
  background: #CD7F32;
  color: var(--white);
}

.player-col {
  display: flex;
  align-items: center;
}

.player-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.player-name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--white);
}

.you-tag {
  font-size: 0.8rem;
  color: var(--yellow);
  font-weight: 700;
}

.points-col {
  display: flex;
  align-items: center;
  justify-content: center;
}

.points-value {
  font-size: 1.1rem;
  font-weight: 900;
  color: var(--yellow);
}

.stats-section {
  display: flex;
  gap: 40px;
  justify-content: center;
  flex-wrap: wrap;
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
  text-transform: uppercase;
  margin-bottom: 8px;
}

.stat-value {
  display: block;
  font-size: 20px;
  font-weight: 900;
  color: var(--white);
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

@media (max-width: 768px) {
  .game-end-panel {
    padding: 30px 20px;
    gap: 24px;
  }

  .game-header h1 {
    font-size: 1.5rem;
  }

  .table-header,
  .table-row {
    grid-template-columns: 60px 1fr 80px;
    gap: 10px;
    padding: 12px;
  }

  .stats-section {
    gap: 20px;
  }

  .button-section {
    flex-direction: column;
    align-items: stretch;
  }
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
    max-width: 600px;
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
    max-width: 500px;
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
