<template>
  <div class="game-end-background" aria-hidden="true"></div>
  
  <!-- Home Button -->
  <div class="page-logo">
    <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
    <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
  </div>
  
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
        <h1>RANKED GAME COMPLETE</h1>
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
          HOME
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
  border: 3px solid rgba(255, 203, 59, 0.3);
  border-top: 3px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
  padding: 24px;
}

.player-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.player-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.player-elo {
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--light-grey);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-family: "Red Hat Text", sans-serif;
}

.player-score {
  font-size: 2rem;
  font-weight: 900;
  color: var(--yellow);
}

.elo-change {
  font-size: 0.9rem;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.elo-change.positive {
  background: rgba(182, 255, 127, 0.2);
  color: #B6FF7F;
  border: 1px solid rgba(182, 255, 127, 0.3);
}

.elo-change.negative {
  background: rgba(255, 127, 127, 0.2);
  color: #FF7F7F;
  border: 1px solid rgba(255, 127, 127, 0.3);
}

.vs-divider {
  display: flex;
  align-items: center;
  justify-content: center;
}

.vs-text {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 2px;
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
  font-size: 1rem;
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
  font-size: 0.9rem;
  color: var(--light-grey);
  font-weight: 500;
  font-family: "Red Hat Text", sans-serif;
}

.elo-summary-value {
  font-size: 1rem;
  font-weight: 600;
  color: var(--white);
}

.elo-summary-value.positive {
  color: #B6FF7F;
}

.elo-summary-value.negative {
  color: #FF7F7F;
}

/* Button Section */
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



/* Responsive Design */
@media (max-width: 768px) {
  .game-end-panel {
    padding: 30px 20px;
    gap: 24px;
  }

  .game-header h1 {
    font-size: 1.5rem;
  }

  .results-section {
    flex-direction: column;
    gap: 24px;
  }

  .player-result {
    min-width: auto;
    width: 100%;
    max-width: 300px;
  }

  .vs-divider {
    width: 60px;
    height: 60px;
  }

  .vs-text {
    font-size: 1rem;
  }

  .button-section {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
