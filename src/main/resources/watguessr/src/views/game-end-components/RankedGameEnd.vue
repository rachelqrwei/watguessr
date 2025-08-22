<template>
  <div class="game-end-container">
    <!-- Loading state -->
    <div v-if="!hasCompleteData" class="loading-container">
      <div class="loading-spinner"></div>
      <p>Loading game results...</p>
      <p style="font-size: 14px; opacity: 0.8; margin-top: 10px;">
        Waiting for WebSocket to complete game...
      </p>
      <div class="debug-info" style="background: #333; color: white; padding: 10px; margin: 20px; border-radius: 8px; font-size: 12px;">
        <strong>Debug Info:</strong><br>
        Players: {{ Object.keys(players || {}).length }}<br>
        Has Result: {{ !!rankedGame_getResult }}<br>
        ELO Changes: {{ Object.keys(rankedGame_getResult?.eloChanges || {}).length }}<br>
        Current User: {{ currentUser?.id }}<br>
        Final Winner: {{ rankedGame_getFinalWinner }}<br>
        <br>
        <strong>Opponent Data:</strong><br>
        Opponent ID: {{ opponentPlayerId }}<br>
        Opponent Player: {{ opponentPlayer }}<br>
        Opponent ELO: {{ opponentElo }}<br>
        Opponent ELO Change: {{ player2EloChange }}<br>
        <br>
        <strong>ELO Debug:</strong><br>
        Player 1 Starting ELO: {{ startingElo }}<br>
        Player 1 New ELO: {{ newElo }}<br>
        Player 1 ELO Change: {{ player1EloChange }}<br>
        Player 2 Starting ELO: {{ player2Elo }}<br>
        Player 2 New ELO: {{ player2Elo + player2EloChange }}<br>
        Player 2 ELO Change: {{ player2EloChange }}<br>
        <br>
        <strong>Pre-Game ELOs from Store:</strong><br>
        {{ $store.getters['rankedGame/rankedGame_getPreGameElos'] ? JSON.stringify($store.getters['rankedGame/rankedGame_getPreGameElos']) : 'No pre-game ELOs stored' }}<br>
        <br>
        <strong>Game Result:</strong><br>
        {{ JSON.stringify(rankedGame_getResult, null, 2) }}
      </div>
    </div>

    <!-- Game end content -->
    <div v-else-if="hasCompleteData" class="game-end-panel">
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
        <button class="btn home-btn" @click="goHome">
          Home
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
      // These will be computed from store data
      gameDuration: '0:00', // Will be calculated from actual game time
      bestRoundScore: 0, // Will be calculated from actual round scores
      completionTimeout: null // Timeout for forcing game completion
    };
  },
  computed: {
    ...mapGetters('gameInfo', [
      'getGameMode'
    ]),
    ...mapGetters('user', ["getCurrentUser"]),
    ...mapGetters('rankedGame', [
      'rankedGame_getPlayers',
      'rankedGame_getCurrentRound',
      'rankedGame_getMaxRounds',
      'rankedGame_getFinalWinner',
      'rankedGame_getShouldEnd',
      'rankedGame_getResult',
      'rankedGame_getPreGameElos'
    ]),

    // Get current user info
    currentUser() {
      return this.getCurrentUser;
    },

    // Get all players from ranked game store
    players() {
      return this.rankedGame_getPlayers || {};
    },

    // Get current user's player data
    currentPlayer() {
      if (!this.currentUser || !this.players) return null;
      return this.players[this.currentUser.id];
    },

    // Get opponent player data (first player that's not the current user)
    opponentPlayerId() {
      if (!this.currentUser || !this.players) {
        console.log('🎯 opponentPlayerId: Missing currentUser or players', { currentUser: this.currentUser, players: this.players });
        return null;
      }
      const opponentId = Object.keys(this.players).find(id => id !== this.currentUser.id);
      console.log('🎯 opponentPlayerId computed:', { currentUserId: this.currentUser.id, allPlayerIds: Object.keys(this.players), opponentId });
      return opponentId;
    },

    // Get opponent player data (first player that's not the current user)
    opponentPlayer() {
      if (!this.currentUser || !this.players) return null;
      const opponentId = Object.keys(this.players).find(id => id !== this.currentUser.id);
      return opponentId ? this.players[opponentId] : null;
    },

    // Player 1 (current user) data
    player1Score() {
      return this.currentPlayer?.score || 0;
    },

    player1Elo() {
      // Get pre-game ELO from store if available
      if (this.currentUser?.username) {
        const preGameElos = this.$store.getters['rankedGame/rankedGame_getPreGameElos'];
        if (preGameElos && preGameElos[this.currentUser.username]) {
          console.log('🎯 Using pre-game ELO from store for current user:', preGameElos[this.currentUser.username]);
          return preGameElos[this.currentUser.username];
        }
      }

      // Fallback to current user's ELO if pre-game not available
      if (!this.getCurrentUser?.elo) {
        return 1200; // Fallback to base ELO
      }
      return this.getCurrentUser.elo;
    },

    player1EloChange() {
      if (!this.rankedGame_getResult?.eloChanges || !this.currentUser?.id) {
        return 0;
      }
      return this.rankedGame_getResult.eloChanges[this.currentUser.id] || 0;
    },

    // Player 2 (opponent) data
    player2Score() {
      return this.opponentPlayer?.score || 0;
    },

    opponentElo() {
      // Get pre-game ELO from store if available
      if (this.opponentPlayer?.username) {
        const preGameElos = this.$store.getters['rankedGame/rankedGame_getPreGameElos'];
        console.log('🎯 Debugging opponent ELO:', {
          opponentUsername: this.opponentPlayer.username,
          preGameElos: preGameElos,
          hasOpponentElo: preGameElos && preGameElos[this.opponentPlayer.username]
        });

        if (preGameElos && preGameElos[this.opponentPlayer.username]) {
          console.log('🎯 Using pre-game ELO from store for opponent:', preGameElos[this.opponentPlayer.username]);
          return preGameElos[this.opponentPlayer.username];
        }
      }

      // Fallback value
      console.log('🎯 No pre-game ELO found for opponent, using fallback: 1200');
      return 1200;
    },

    player2Elo() {
      return this.opponentElo;
    },

    player2EloChange() {
      if (!this.rankedGame_getResult?.eloChanges || !this.opponentPlayerId) {
        return 0;
      }
      return this.rankedGame_getResult.eloChanges[this.opponentPlayerId] || 0;
    },

    // Game state data
    winner() {
      const finalWinner = this.rankedGame_getFinalWinner;
      if (finalWinner === this.currentUser?.id) {
        return 'player1';
      } else if (finalWinner && finalWinner !== this.currentUser.id) {
        return 'player2';
      }
      // Fallback: determine winner by score
      return this.player1Score >= this.player2Score ? 'player1' : 'player2';
    },

    // Check if we have all the data we need
    hasCompleteData() {
      // We need at least players data and a final winner to show the game end screen
      // ELO changes can be loaded later if not immediately available
      return this.players &&
             Object.keys(this.players).length > 0 &&
             this.rankedGame_getFinalWinner;
    },

    totalRounds() {
      return this.rankedGame_getMaxRounds || 5;
    },

    startingElo() {
      // Use the pre-game ELO from the store
      return this.player1Elo;
    },

    newElo() {
      // Calculate new ELO by adding ELO change to starting ELO
      return this.startingElo + this.player1EloChange;
    },

    totalEloChange() {
      return this.player1EloChange;
    }
  },
  methods: {
    ...mapActions('singleplayerGame', {
      doRestartGame: "singleplayerGame_restartGame"
    }),

    goHome() {
      this.$router.push('/');
    },
  },

  mounted() {
    // Debug: Log initial state
    console.log('🎯 RankedGameEnd mounted');
    console.log('🎯 Current user:', this.currentUser);
    console.log('🎯 Players from store:', this.players);
    console.log('🎯 Current round:', this.rankedGame_getCurrentRound);
    console.log('🎯 Max rounds:', this.rankedGame_getMaxRounds);
    console.log('🎯 Final winner:', this.rankedGame_getFinalWinner);
    console.log('🎯 Winner computed:', this.winner);
    console.log('🎯 Player 1 score:', this.player1Score);
    console.log('🎯 Player 2 score:', this.player2Score);
    console.log('🎯 Ranked game result:', this.rankedGame_getResult);
    console.log('🎯 ELO changes:', this.rankedGame_getResult?.eloChanges);
    console.log('🎯 Player 1 ELO change:', this.player1EloChange);
    console.log('🎯 Player 2 ELO change:', this.player2EloChange);

    // Debug: Log pre-game ELOs
    const preGameElos = this.$store.getters['rankedGame/rankedGame_getPreGameElos'];
    console.log('🎯 Pre-game ELOs available on mount:', preGameElos);
    if (preGameElos && Object.keys(preGameElos).length > 0) {
      console.log('🎯 Pre-game ELOs details:', preGameElos);
      if (this.currentUser?.id) {
        console.log('🎯 Current user pre-game ELO on mount:', preGameElos[this.currentUser.id]);
      }
      if (this.opponentPlayerId) {
        console.log('🎯 Opponent pre-game ELO on mount:', preGameElos[this.opponentPlayerId]);
      }
    }

    // Try to load final game data if not available
    if (!this.hasCompleteData) {
      console.log('🎯 Incomplete data, trying to load final game data...');
      this.$store.dispatch('rankedGame/rankedGame_loadFinalGameData');
    }

    // If we have game data but no ELO changes, trigger the game completion
    if (this.hasCompleteData && (!this.rankedGame_getResult || !this.rankedGame_getResult.eloChanges || Object.keys(this.rankedGame_getResult.eloChanges).length === 0)) {
      console.log('🎯 Game data available but no ELO changes, triggering game completion...');
      this.$store.dispatch('rankedGame/rankedGame_endGame');
    }

    // Set a timeout to trigger game completion if ELO changes aren't received within 5 seconds
    this.completionTimeout = setTimeout(() => {
      if (this.hasCompleteData && (!this.rankedGame_getResult || !this.rankedGame_getResult.eloChanges || Object.keys(this.rankedGame_getResult.eloChanges).length === 0)) {
        console.log('🎯 Timeout reached, forcing game completion...');
        this.$store.dispatch('rankedGame/rankedGame_endGame');
      }
    }, 5000);
  },

  watch: {
    // Watch for changes in store data
    players: {
      handler(newPlayers) {
        console.log('🎯 Players data changed in RankedGameEnd:', newPlayers);
      },
      deep: true
    },

    'rankedGame_getShouldEnd': {
      handler(newVal) {
        console.log('🎯 shouldEnd changed in RankedGameEnd:', newVal);
      }
    },

    // Watch for ELO changes becoming available
    'rankedGame_getResult': {
      handler(newResult) {
        console.log('🎯 Ranked game result changed in RankedGameEnd:', newResult);
        if (newResult && newResult.eloChanges && Object.keys(newResult.eloChanges).length > 0) {
          console.log('🎯 ELO changes now available:', newResult.eloChanges);
        }
      },
      deep: true
    },

    // Watch for pre-game ELOs becoming available
    'rankedGame_getPreGameElos': {
      handler(newPreGameElos) {
        console.log('🎯 Pre-game ELOs changed in RankedGameEnd:', newPreGameElos);
        if (newPreGameElos && Object.keys(newPreGameElos).length > 0) {
          console.log('🎯 Pre-game ELOs now available:', newPreGameElos);
          // Log specific ELOs for current user and opponent
          if (this.currentUser?.id) {
            console.log('🎯 Current user pre-game ELO:', newPreGameElos[this.currentUser.id]);
          }
          if (this.opponentPlayerId) {
            console.log('🎯 Opponent pre-game ELO:', newPreGameElos[this.opponentPlayerId]);
          }
        }
      },
      deep: true
    }
  },

  beforeUnmount() {
    // Clean up when leaving the game end screen
    console.log('🎯 RankedGameEnd unmounting, cleaning up');
    // Don't disconnect WebSocket here - let it stay connected to receive completion events
    // this.$store.dispatch('rankedGame/rankedGame_disconnect');
    this.$store.dispatch('rankedGame/rankedGame_clearFinalGameData');
    if (this.completionTimeout) {
      clearTimeout(this.completionTimeout);
      this.completionTimeout = null;
    }
  }
};
</script>

<style scoped>
.loading-container {
  text-align: center;
  color: white;
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(8px) saturate(120%);
  -webkit-backdrop-filter: blur(8px) saturate(120%);
  z-index: 1;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 5px solid rgba(255, 255, 255, 0.3);
  border-top: 5px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

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

/* Responsive styling for different laptop/desktop sizes */
@media (max-width: 1400px) {
  .game-end-panel {
    max-width: 720px;
    padding: 28px 20px 24px;
    gap: 20px;
  }
  
  .game-label {
    font-size: 32px;
  }
  
  .winner-announcement {
    font-size: 22px;
  }
  
  .results-section {
    gap: 28px;
  }
  
  .player-result {
    min-width: 180px;
  }
  
  .player-score {
    font-size: 28px;
  }
  
  .vs-divider {
    width: 70px;
    height: 70px;
  }
  
  .vs-text {
    font-size: 18px;
  }
  
  .elo-summary {
    max-width: 360px;
    padding: 18px;
  }
  
  .btn {
    padding: 10px 20px;
    font-size: 13px;
  }
}

@media (max-width: 1200px) {
  .game-end-panel {
    max-width: 640px;
    padding: 24px 18px 20px;
    gap: 18px;
  }
  
  .game-label {
    font-size: 28px;
  }
  
  .winner-announcement {
    font-size: 20px;
  }
  
  .results-section {
    gap: 24px;
  }
  
  .player-result {
    min-width: 160px;
  }
  
  .player-score {
    font-size: 24px;
  }
  
  .vs-divider {
    width: 65px;
    height: 65px;
  }
  
  .vs-text {
    font-size: 17px;
  }
  
  .elo-summary {
    max-width: 320px;
    padding: 16px;
  }
  
  .btn {
    padding: 9px 18px;
    font-size: 12px;
  }
}

@media (max-width: 1024px) {
  .game-end-panel {
    max-width: 580px;
    padding: 20px 16px 18px;
    gap: 16px;
  }
  
  .game-label {
    font-size: 24px;
  }
  
  .winner-announcement {
    font-size: 18px;
  }
  
  .results-section {
    gap: 20px;
  }
  
  .player-result {
    min-width: 140px;
  }
  
  .player-score {
    font-size: 20px;
  }
  
  .vs-divider {
    width: 60px;
    height: 60px;
  }
  
  .vs-text {
    font-size: 16px;
  }
  
  .elo-summary {
    max-width: 300px;
    padding: 14px;
  }
  
  .btn {
    padding: 8px 16px;
    font-size: 11px;
  }
}

@media (max-width: 900px) {
  .game-end-panel {
    max-width: 520px;
    padding: 18px 14px 16px;
    gap: 14px;
  }
  
  .game-label {
    font-size: 20px;
  }
  
  .winner-announcement {
    font-size: 16px;
  }
  
  .results-section {
    gap: 18px;
  }
  
  .player-result {
    min-width: 120px;
  }
  
  .player-score {
    font-size: 18px;
  }
  
  .vs-divider {
    width: 55px;
    height: 55px;
  }
  
  .vs-text {
    font-size: 15px;
  }
  
  .elo-summary {
    max-width: 280px;
    padding: 12px;
  }
  
  .btn {
    padding: 7px 14px;
    font-size: 10px;
  }
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
