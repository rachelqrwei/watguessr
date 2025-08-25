<template>
  <div class="lobby-background" aria-hidden="true"></div>
  <div class="lobby-page">
    <transition name="card-fade" appear>
      <div class="lobby-card">
      <div class="lobby-header">
        <h1 class="lobby-title">{{ gameModeLabel === 'multiplayer' && lobbyInfo ? lobbyInfo.lobbyName : 'LOBBY' }}</h1>
        <div class="game-mode-tag" :class="gameModeLabel.toLowerCase()">
          {{ gameModeLabel.toUpperCase() }}
        </div>
      </div>

      <div class="lobby-content">
        <transition name="fade-slide" mode="out-in">
        <!-- SINGLEPLAYER -->
        <div v-if="gameModeLabel === 'singleplayer'" key="singleplayer" class="singleplayer-section">
          <div class="game-settings">
            <h3>Game Settings</h3>
            <div class="settings-grid">
              <div class="setting-item">
                <span class="setting-label">Timer</span>
                <span class="setting-value">30 seconds</span>
              </div>
              <div class="setting-item">
                <span class="setting-label">Rounds</span>
                <span class="setting-value">Unlimited</span>
              </div>
              <div class="setting-item">
                <span class="setting-label">Health</span>
                <span class="setting-value">1000 points</span>
              </div>
            </div>
          </div>
          <button class="play-button" @click="goToPlay">PLAY</button>
        </div>

      <!-- MULTIPLAYER -->
      <div v-else-if="gameModeLabel === 'multiplayer'" key="multiplayer" class="multiplayer-section">
        <div v-if="lobbyInfo" class="lobby-details">
          <div v-if="lobbyInfo.isPrivate && lobbyInfo.lobbyCode" class="lobby-code">
            <span class="code-label">Lobby Code:</span>
            <span class="code">{{ lobbyInfo.lobbyCode }}</span>
          </div>

          <div class="lobby-stats">
            <div class="stat-group">
              <h4>Game Settings</h4>
              <div class="settings-grid">
                <div class="setting-item">
                  <span class="setting-label">Timer</span>
                  <span class="setting-value">{{ displayTimer }}s</span>
                </div>
                <div class="setting-item">
                  <span class="setting-label">Rounds</span>
                  <span class="setting-value">{{ lobbyInfo.multiplayerRoundCount }}</span>
                </div>
              </div>
            </div>

            <div class="stat-group lobby-status">
              <h4>Lobby Status</h4>
              <div class="settings-grid">
                <div class="setting-item">
                  <span class="setting-label">Players</span>
                  <span class="setting-value">{{ players.length }}/{{ lobbyInfo.maxPlayers }}</span>
                </div>
                <div class="setting-item">
                  <span class="setting-label">Ready</span>
                  <span class="setting-value ready-indicator-text">{{ readyCount }}/{{ players.length }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <transition-group name="list-fade" tag="ul" class="players-list">
          <li v-for="player in players" :key="player.userId" class="player-item">
            <div class="player-info">
              <span class="player-name">{{ player.username }}</span>
              <span v-if="player.userId === myId" class="you-badge">(YOU)</span>
            </div>
            <div class="ready-status">
              <!-- Show ready button only for current user -->
              <button
                v-if="player.userId === myId"
                class="ready-button"
                :class="{ 'ready': player.ready }"
                @click="toggleReady"
              >
                {{ player.ready ? 'READY' : 'NOT READY' }}
              </button>
              <!-- Show read-only indicator for other players -->
              <span v-else class="ready-indicator" :class="{ 'ready': player.ready }">
                {{ player.ready ? 'READY' : 'NOT READY' }}
              </span>
            </div>
          </li>
        </transition-group>

        <!-- Show loading message if no players -->
        <div v-if="players.length === 0" class="loading-players-msg">
          <p><strong>LOADING USERS</strong><span class="dots-animation loading-dots"><span>.</span><span>.</span><span>.</span></span></p>
        </div>

        <p v-if="players.length < 2" class="waiting-msg">
          Waiting for more players to join...
        </p>

        <p v-else-if="readyCount < players.length" class="waiting-msg">
          Waiting for all players to be ready...
        </p>

        <button
          v-if="players.length >= 2 && readyCount === players.length && !gameStarted"
          class="play-button start-game-button"
          @click="startGameClick"
        >
          START GAME
        </button>

        <p v-if="gameStarted" class="waiting-msg">
          Game started! Redirecting...
        </p>
      </div>

      <!-- RANKED -->
      <div v-else-if="gameModeLabel === 'ranked'" key="ranked" class="ranked-section">

        <!-- Show initial state when not in queue -->
        <div v-if="rankedQueueState === 'idle'" class="ranked-info">
          <h3 class="ranked-title">RANKED MATCHMAKING</h3>
          <p class="ranked-subtitle">
            <em>Compete against other players in ranked matches and climb the leaderboard!</em>
          </p>
          <div class="game-settings">
            <div class="settings-grid">
              <div class="setting-item">
                <span class="setting-label">Timer</span>
                <span class="setting-value">20 seconds</span>
              </div>
              <div class="setting-item">
                <span class="setting-label">Rounds</span>
                <span class="setting-value">5</span>
              </div>
              <div class="setting-item">
                <span class="setting-label">Your Elo</span>
                <span class="setting-value">{{ $store.getters['user/getCurrentUser']?.elo ?? 1200 }}</span>
              </div>
            </div>
          </div>
          <button class="play-button" @click="initiateRankedWebSocketConnection">
            Join Ranked Queue
          </button>
        </div>

        <!-- Show matchmaking queue component when in queue, match found, or error -->
        <MatchmakingQueue
          v-else
          :queue-state="rankedQueueState"
          :match-info="rankedMatchInfo"
          :error-message="rankedErrorMessage"
          @cancel-queue="cancelRankedQueue"
          @retry-connection="retryRankedConnection"
          @game-start="startRankedGame"
          :key="'matchmaking-' + rankedQueueState"
        />
        </div>
        </transition>
      </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { mapMutations, mapActions } from "vuex";
import { connectLobby, joinLobby, startGame, disconnectLobby, setPlayerReady, leaveLobby, forceLeaveLobby } from "@/services/lobby";
import { LobbyManager } from "@/services/lobbyManager";
import { connectToMultiplayerGame } from "@/services/multiplayerGameWebSocket";
import {
  connectToMatchmakingWebSocket,
  disconnectFromMatchmakingWebSocket,
  joinRankedQueue,
  leaveRankedQueue
} from "@/services/matchmakingWebSocket";
import { connectToRankedGame } from "@/services/rankedGameWebSocket.js";
import MatchmakingQueue from "@/components/MatchmakingQueue.vue";

export default {
  name: "Lobby",
  components: {
    MatchmakingQueue
  },
  data() {
    return {
      players: [],
      myId: "",
      gameStarted: false,
      lobbyInfo: null,
      isConnected: false,
      connectionAttempts: 0,
      maxConnectionAttempts: 3,
      cleanupCalled: false,
      // Ranked matchmaking state
      rankedQueueState: 'idle', // 'idle', 'searching', 'match_found', 'error'
      rankedMatchInfo: null,
      rankedErrorMessage: ''
    };
  },
  computed: {
    gameModeLabel() {
      const mode = (this.$route.query.gameMode ?? "").toString();
      return mode ? mode : "singleplayer";
    },
    lobbyId() {
      return this.$route.query.lobbyId;
    },
    readyCount() {
      return this.players.filter(player => player.ready).length;
    },
    displayTimer() {
      if (this.lobbyInfo?.multiplayerTimer) {
        // Convert milliseconds to seconds for display
        return Math.round(this.lobbyInfo.multiplayerTimer / 1000);
      }
      return 60; // Default fallback
    }
  },
  methods: {
    ...mapMutations("gameInfo", ["SET_GAME_MODE"]),

    goToPlay() {
      this.SET_GAME_MODE(this.gameModeLabel);
      this.$router.push({name: "play", query: {gameMode: this.gameModeLabel}});
    },

    toggleReady() {
      const currentPlayer = this.players.find(p => p.userId === this.myId);
      if (currentPlayer) {
        const newReadyStatus = !currentPlayer.ready;
        setPlayerReady(this.myId, newReadyStatus);
      }
    },

    startGameClick() {
      if (this.lobbyId) {
        startGame(this.lobbyId, this.gameModeLabel, this.lobbyInfo.multiplayerRoundCount, this.lobbyInfo.multiplayerTimer); // broadcast to all clients that game is starting
      }
    },

    async fetchLobbyInfo() {
      if (this.lobbyId) {
        try {
          // Try to get lobby info from the API
          const lobby = await LobbyManager.getLobbyById(this.lobbyId);
          this.lobbyInfo = {
            lobbyName: lobby.lobbyName,
            isPrivate: lobby.isPrivate,
            lobbyCode: lobby.lobbyCode,
            multiplayerRoundCount: lobby.multiplayerRoundCount,
            multiplayerTimer: lobby.multiplayerTimer,
            maxPlayers: lobby.maxPlayers
          };
        } catch (error) {
          console.error('Failed to fetch lobby info:', error);
          // Fallback to route query params
          this.lobbyInfo = {
            lobbyName: this.$route.query.lobbyName || 'Multiplayer Lobby',
            isPrivate: this.$route.query.lobbyCode ? true : false,
            lobbyCode: this.$route.query.lobbyCode,
            multiplayerRoundCount: 5, // Default values
            multiplayerTimer: 30000,
            maxPlayers: 8
          };
        }
      }
    },

    async reconnectToLobby() {
      if (this.connectionAttempts >= this.maxConnectionAttempts) {
        console.error('Max reconnection attempts reached, redirecting to home');
        this.$router.push({name: 'home'});
        return;
      }

      this.connectionAttempts++;

      try {
        // Check if lobby still exists and user is still in it
        const lobby = await LobbyManager.getLobbyById(this.lobbyId);
        const currentUser = this.$store.getters["user/getCurrentUser"];

        // First, try to clean up any stale lobby state
        disconnectLobby();

        // Try to rejoin the lobby
        await LobbyManager.joinLobby({
          lobbyCode: lobby.lobbyCode,
          userId: currentUser.id
        });

        // Reconnect WebSocket
        this.connectToLobbyWebSocket();

        this.isConnected = true;
        this.connectionAttempts = 0;
      } catch (error) {
        console.error('Failed to reconnect to lobby:', error);

        // If lobby doesn't exist or user can't rejoin, redirect to home
        if (error.message.includes('404') || error.message.includes('not found')) {
          this.$router.push({name: 'home'});
        } else {
          // Try again after a delay
          setTimeout(() => this.reconnectToLobby(), 2000);
        }
      }
    },
    connectToLobbyWebSocket() {
      if (this.gameModeLabel === 'multiplayer' && this.lobbyId) {
        const currentUser = this.$store.getters["user/getCurrentUser"];

        // Connect to lobby WebSocket
        connectLobby(
          (lobbyUpdate) => {
            this.players = lobbyUpdate.players;
            this.isConnected = true;
          },
          (startInfo) => {
            this.gameStarted = true;
            this.SET_GAME_MODE("multiplayer");

            // The lobby system has already created the game and provided the gameId
            if (startInfo.gameId) {
              // Set the game ID from the lobby
              this.$store.commit('multiplayerGame/MG_SET_GAME_ID', startInfo.gameId);

              // Set game settings from lobby info
              this.$store.commit('multiplayerGame/MG_SET_MAX_ROUNDS', this.lobbyInfo?.multiplayerRoundCount || 5);
              this.$store.commit('multiplayerGame/MG_SET_TIMER', this.lobbyInfo?.multiplayerTimer || 30000);

              // Initialize players in game state
              const players = {};
              startInfo.users.forEach(user => {
                players[user.id] = {
                  status: 'loading',
                  score: 0,
                  username: user.username
                };
              });
              this.$store.commit('multiplayerGame/MG_SET_PLAYERS', players);

              // Connect to multiplayer game WebSocket using the lobby's gameId
              connectToMultiplayerGame(startInfo.gameId);

              // Navigate to play with the lobby's gameId
              this.$router.push({
                name: "play",
                query: {gameMode: "multiplayer", gameId: startInfo.gameId}
              });
            } else {
              console.error('❌ No gameId received from lobby start event');
            }
          }
        );

        // Join the lobby
        joinLobby(currentUser, this.lobbyId);
      }
    },
    async goHome() {
      const currentUser = this.$store.getters["user/getCurrentUser"];

      if (this.lobbyId && this.gameModeLabel === "multiplayer") {
        try {
          await leaveLobby(currentUser);
          await disconnectLobby();
        } catch (err) {
          console.error("❌ Failed to cleanup lobby:", err);
        }
      }

      // Now navigate home
      this.$router.push({ name: "home" });
    },
    initiateRankedWebSocketConnection() {
      this.rankedQueueState = 'searching';
      this.rankedErrorMessage = '';
      // Connect to matchmaking WebSocket with callbacks
      connectToMatchmakingWebSocket(this.myId, {
        onConnected: () => {
          // Connection is ready, now join the queue
          console.log('🚀 Connection ready, joining ranked queue...')
          joinRankedQueue(this.myId)
        },
        onQueueJoined: () => {
          // Ensure we stay in searching state
          this.rankedQueueState = 'searching'
          console.log('Successfully joined ranked queue')
        },
        onQueueLeft: () => {
          this.rankedQueueState = 'idle';
        },
        onMatchFound: (matchInfo) => {
          this.rankedQueueState = 'match_found';

          // Find opponent from the players list
          const players = matchInfo.players || [];

          const opponent = players.find((p) => p.id !== this.myId) || players[0]

          this.rankedMatchInfo = {
            opponentName: opponent?.username || 'Anonymous Player',
            opponentRating: opponent?.elo || 1200,
            roundCount: 5, // Default for ranked games
            timeLimit: 20, // Default for ranked games
            gameId: matchInfo.gameId,
          }
        },
        onQueueTimeout: (message) => {
          this.rankedQueueState = 'error'
          this.rankedErrorMessage = message
        },
        onError: (error) => {
          this.rankedQueueState = 'error'
          this.rankedErrorMessage = error
        },
      })
    },
    cancelRankedQueue() {
      leaveRankedQueue(this.myId);
      this.rankedQueueState = 'idle';
      this.rankedMatchInfo = null;
      disconnectFromMatchmakingWebSocket();
    },
    retryRankedConnection() {
      this.rankedQueueState = 'idle';
      this.rankedErrorMessage = '';
      // Try to reconnect
      this.initiateRankedWebSocketConnection();
    },
    startRankedGame() {
      if (this.rankedMatchInfo && this.rankedMatchInfo.gameId) {
        // Set game mode and navigate to play
        this.SET_GAME_MODE("ranked");
        // Store game information in Vuex if needed
        this.$store.commit('gameInfo/SET_GAME_MODE', 'ranked');

        // Initialize the ranked game state
        this.$store.dispatch('rankedGame/rankedGame_createRankedGame');
        this.$store.commit('rankedGame/RG_SET_GAME_ID', this.rankedMatchInfo.gameId);

        this.$store.dispatch('rankedGame/rankedGame_storeOpponentElos', {
          opponentName: this.rankedMatchInfo.opponentName,
          opponentElo: this.rankedMatchInfo.opponentRating
        });

        this.$router.push({
          name: "play",
          query: {
            gameMode: "ranked",
            gameId: this.rankedMatchInfo.gameId
          }
        });
        connectToRankedGame(this.rankedMatchInfo.gameId);
      } else {
        this.rankedQueueState = 'error';
        this.rankedErrorMessage = 'Failed to start game: No game ID received';
      }
    },
    cleanup() {
      if (this.cleanupCalled) {
        return;
      }
      this.cleanupCalled = true;
      // Remove window event listener
      window.removeEventListener('beforeunload', this.cleanup);
      if (this.gameModeLabel === 'multiplayer') {
        disconnectLobby();
      } else if (this.gameModeLabel === 'ranked') {
        // Leave the ranked queue if we're in it
        if (this.myId && this.rankedQueueState !== 'idle') {
          leaveRankedQueue(this.myId);
        } else {
          console.warn("⚠️ No user ID available for leaving queue or not in queue");
        }
        // Reset ranked state
        this.rankedQueueState = 'idle';
        this.rankedMatchInfo = null;
        // Disconnect from matchmaking WebSocket
        disconnectFromMatchmakingWebSocket();
      }
    }
  },
  async mounted() {
    const currentUser = this.$store.getters["user/getCurrentUser"];
    this.myId = currentUser.id;

    if (this.gameModeLabel === "multiplayer") {
      this.fetchLobbyInfo();
      this.connectToLobbyWebSocket();
    } else if (this.gameModeLabel === 'ranked') {
      // For ranked mode, we'll connect when user clicks "Join Queue"
      // Just add window beforeunload listener as backup cleanup
      window.addEventListener('beforeunload', this.cleanup);
    }

    // Watch for route changes to leave lobby if navigating away
    this.unwatchRoute = this.$watch(
      () => this.$route.fullPath,
      (newPath, oldPath) => {
        if (oldPath.includes("lobby") && !newPath.includes("lobby")) {
          // Leaving lobby route
          leaveLobby(currentUser);
          disconnectLobby();
        }
      }
    );
  },
  beforeUnmount() {
    // Cleanup watcher
    if (this.unwatchRoute) this.unwatchRoute();

    const currentUser = this.$store.getters["user/getCurrentUser"];
    if (this.lobbyId && currentUser) {
      leaveLobby(currentUser);
      disconnectLobby();
    }

    this.cleanup();
  },
  beforeRouteLeave(to, from, next) {
    this.cleanup();
    next();
  },
}

</script>

<style scoped>
.lobby-background {
  position: fixed;
  inset: 0;
  background: var(--dark-grey);
  z-index: -1;
}

.lobby-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/ProfilePage.png') center top / cover no-repeat;
  opacity: 0.8;
  pointer-events: none;
}

.lobby-page {
  min-height: calc(100vh - 80px);
  position: relative;
  padding: 80px 20px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.lobby-card {
  background: rgba(42, 42, 44, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(8px);
  padding: 32px;
  text-align: center;
  max-width: 600px;
  width: 100%;
}

/* Transitions */
.card-fade-enter-from,
.card-fade-leave-to { opacity: 0; transform: translateY(8px); }
.card-fade-enter-active,
.card-fade-leave-active { transition: opacity 300ms ease, transform 300ms ease; }

.fade-slide-enter-from,
.fade-slide-leave-to { opacity: 0; transform: translateY(10px); }
.fade-slide-enter-active,
.fade-slide-leave-active { transition: opacity 250ms ease, transform 250ms ease; }

.list-fade-enter-from { opacity: 0; transform: translateY(6px); }
.list-fade-enter-active { transition: all 220ms ease; }
.list-fade-leave-active { transition: all 180ms ease; opacity: 0; transform: translateY(-6px); }

.lobby-header {
  text-align: center;
  margin-bottom: 20px;
}

.lobby-content {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 16px 24px 24px;
}

.lobby-title {
  font-size: 1.8rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.game-mode-tag {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 1px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  color: var(--white);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.game-mode-tag.singleplayer {
  background: linear-gradient(135deg, rgba(255, 227, 127, 0.2), rgba(255, 127, 127, 0.2));
  border: 1px solid rgba(255, 227, 127, 0.4);
  box-shadow: 0 0 20px rgba(255, 227, 127, 0.1);
  color: rgba(255, 227, 127, 1);
}

.game-mode-tag.multiplayer {
  background: linear-gradient(135deg, rgba(127, 185, 255, 0.2), rgba(170, 127, 255, 0.2));
  border: 1px solid rgba(127, 185, 255, 0.4);
  box-shadow: 0 0 20px rgba(127, 185, 255, 0.1);
  color: rgba(127, 185, 255, 1);
}

.game-mode-tag.ranked {
  background: linear-gradient(135deg, rgba(255, 200, 100, 0.2), rgba(255, 150, 100, 0.2));
  border: 1px solid rgba(255, 180, 100, 0.4);
  box-shadow: 0 0 20px rgba(255, 180, 100, 0.1);
  color: rgba(255, 180, 100, 1);
}



/* Section styling */
.singleplayer-section,
.multiplayer-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Typography hierarchy */
h3 {
  color: var(--white);
  margin: 0 0 16px 0;
  font-size: 1.3rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

h4 {
  color: var(--white);
  margin: 0 0 12px 0;
  font-size: 1rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  opacity: 0.9;
}

/* Game settings grid */
.game-settings { margin-bottom: 0; }
image.png.lobby-details .stat-group:first-of-type { margin-top: 0; }

/* Make Game Settings header style match Lobby Status */
.multiplayer-section .stat-group h4 {
  margin-top: 8px;
}

/* Add more top padding before section headers */
.multiplayer-section h4 {
  padding-top: 0;
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.setting-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.setting-label {
  font-size: 0.75rem;
  color: var(--light-grey);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}

.setting-value {
  font-size: 0.9rem;
  color: var(--white);
  font-weight: 700;
}

/* Multiplayer specific styles */
.lobby-details {
  margin-bottom: 0;
}



.lobby-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-group { margin-top: 14px; }

.ready-indicator-text {
  color: #7FB9FF;
}

/* Add spacing below game settings grid in ranked pane */
.ranked-section .game-settings { margin-bottom: 24px; }

.lobby-code {
  text-align: center;
  margin: 0 0 12px 0;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.code-label {
  color: var(--light-grey);
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-right: 8px;
}

.lobby-code .code {
  background: linear-gradient(to right, rgba(127, 185, 255, 0.22), rgba(170, 127, 255, 0.22));
  color: var(--white);
  padding: 4px 8px;
  border-radius: 6px;
  font-family: monospace;
  font-weight: bold;
  letter-spacing: 1px;
}



.ranked-info {
  margin-bottom: 0;
  padding: 20px;
  background: none;
  border-radius: 14px;
  border: none;
  display: flex;
  flex-direction: column;
  min-height: 260px;
}

.ranked-title {
  color: var(--white);
  margin: 0 0 8px 0;
  font-size: 1.2rem;
  font-weight: 800;
  letter-spacing: 0.6px;
  text-transform: uppercase;
}

.ranked-subtitle {
  color: var(--light-grey);
  font-size: 0.95rem;
  line-height: 1.5;
  margin: 0 0 12px 0;
  font-family: "Red Hat Text", sans-serif;
}

.ranked-info .waiting-msg:last-of-type,
.ranked-info .play-button {
  margin-top: auto;
}

.players-list {
  list-style: none;
  padding: 0;
  margin: 8px 0;
  color: white;
}

.player-item {
  margin: 6px 0;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.player-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.player-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.player-name {
  font-weight: bold;
}

.you-badge {
  background: linear-gradient(to right, rgba(127, 185, 255, 0.22), rgba(170, 127, 255, 0.22));
  color: var(--white);
  padding: 2px 6px;
  border-radius: 6px;
  font-size: 0.7rem;
  font-weight: bold;
}

.ready-status {
  display: flex;
  align-items: center;
}

.ready-button {
  background: transparent;
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 700;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.ready-button:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.35);
  transform: translateY(-1px);
}

.ready-button.ready {
  background: rgba(255, 255, 255, 0.12);
  color: var(--white);
  border-color: rgba(255, 255, 255, 0.5);
}

.ready-indicator {
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 700;
  background: transparent;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.ready-indicator.ready {
  background: rgba(255, 255, 255, 0.12);
  color: var(--white);
  border-color: rgba(255, 255, 255, 0.5);
}

.waiting-msg {
  color: var(--light-grey);
  margin: 10px 0;
  font-family: "Red Hat Text", sans-serif;
}

.play-button {
  padding: 12px 16px;
  border: none;
  border-radius: 10px;
  font-size: 1.1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  font-family: inherit;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.6);
  color: white;
  margin-top: 16px;
}

.play-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.play-button:hover::before {
  left: 100%;
}

.play-button:active {
  transform: translateY(-1px);
}

/* Singleplayer button styling */
.singleplayer-section .play-button {
  background: linear-gradient(to right, #FFE37F, #FF7F7F);
  border: 1px solid #FFE37F;
  box-shadow: 0 4px 15px rgba(255, 227, 127, 0.3);
}

.singleplayer-section .play-button:hover {
  background: linear-gradient(to right, #FFD96B, #FF6B6B);
  border-color: #FFD96B;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 227, 127, 0.4);
}

/* Multiplayer button styling */
.multiplayer-section .play-button {
  background: linear-gradient(to right, #7FB9FF, #AA7FFF);
  border: 1px solid #7FB9FF;
  box-shadow: 0 4px 15px rgba(127, 185, 255, 0.3);
}

.multiplayer-section .play-button:hover {
  background: linear-gradient(to right, #6BA8FF, #9966FF);
  border-color: #6BA8FF;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(127, 185, 255, 0.4);
}

/* Ranked button styling */
.ranked-section .play-button {
  background: linear-gradient(to right, #FFB366, #FF8B6B);
  border: 1px solid #FFB366;
  box-shadow: 0 4px 15px rgba(255, 179, 102, 0.3);
}

.ranked-section .play-button:hover {
  background: linear-gradient(to right, #FFA652, #FF7555);
  border-color: #FFA652;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 179, 102, 0.4);
}

.start-game-button {
  background: linear-gradient(to right, #7FB9FF, #AA7FFF) !important;
  color: white !important;
  border: 1px solid #7FB9FF !important;
  box-shadow: 0 4px 15px rgba(127, 185, 255, 0.3);
  animation: none;
}

.start-game-button:hover {
  background: linear-gradient(to right, #6BA8FF, #9966FF) !important;
  border-color: #6BA8FF !important;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(127, 185, 255, 0.4);
}

/* Loading message styling */
.loading-players-msg {
  background: rgba(128, 128, 128, 0.2);
  padding: 15px;
  margin: 15px 0;
  border-radius: 8px;
  color: white;
  text-align: center;
}

.loading-players-msg p {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.loading-dots {
  display: inline-flex;
  gap: 2px;
  margin-left: 4px;
}

.loading-dots span {
  animation: dots 1.4s infinite ease-in-out both;
  font-size: 1.2rem;
  color: var(--white);
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

@keyframes dots {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(76, 175, 80, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(76, 175, 80, 0);
  }
}

@media (max-width: 768px) {
  .lobby-page {
    padding: 60px 20px 20px;
  }

  .lobby-card {
    padding: 20px;
    margin: 0 auto;
    max-width: 95vw;
  }

  .lobby-content {
    padding: 20px;
  }

  .lobby-title {
    font-size: 1.5rem;
  }

  .lobby-subtitle {
    font-size: 1rem;
  }

  .player-item {
    flex-direction: column;
    gap: 8px;
    text-align: center;
    padding: 16px;
  }

  .play-button {
    padding: 10px 16px;
    font-size: 1rem;
  }

  .singleplayer-section .play-button:hover,
  .multiplayer-section .play-button:hover,
  .ranked-section .play-button:hover {
    transform: translateY(-2px);
  }



  .ranked-info {
    padding: 16px;
  }

  .settings-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .lobby-stats {
    gap: 16px;
  }
}
</style>
