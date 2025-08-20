<template>
  <div class="lobby-container">
    <div class="logo-container cursor-pointer" @click.prevent="goHome">
      <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
      <span class="logo-text">WATGUESSR.IO</span>
    </div>

    <div class="lobby-card">
      <h1 class="lobby-title">Lobby</h1>
      <p class="lobby-subtitle">Mode: {{ gameModeLabel }}</p>

      <!-- SINGLEPLAYER -->
      <div v-if="gameModeLabel === 'singleplayer'">
        <button class="play-button" @click="goToPlay">PLAY</button>
      </div>

      <!-- MULTIPLAYER -->
      <div v-else-if="gameModeLabel === 'multiplayer'">
        <div v-if="lobbyInfo" class="lobby-info">
          <h3>{{ lobbyInfo.lobbyName }}</h3>
          <p v-if="lobbyInfo.isPrivate && lobbyInfo.lobbyCode" class="lobby-code">
            Lobby Code: <span class="code">{{ lobbyInfo.lobbyCode }}</span>
          </p>
          <p class="lobby-settings">
            {{ lobbyInfo.multiplayerRoundCount }} rounds • {{ displayTimer }}s timer
          </p>
          <p class="lobby-players">
            Players: {{ players.length }}/{{ lobbyInfo.maxPlayers }}
          </p>
          <p class="ready-count">
            Ready: {{ readyCount }}/{{ players.length }}
          </p>
        </div>

        <ul class="players-list">
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
        </ul>

        <!-- Show message if no players -->
        <div v-if="players.length === 0" class="no-players-msg" style="background: rgba(255,0,0,0.2); padding: 15px; margin: 15px 0; border-radius: 8px; color: white; text-align: center;">
          <p><strong>⚠️ No players loaded yet</strong></p>
          <p>Players array is empty. This might indicate a connection issue.</p>
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
      <div v-else-if="gameModeLabel === 'ranked'">

        <!-- Show initial state when not in queue -->
        <div v-if="rankedQueueState === 'idle'" class="ranked-info">
          <p class="waiting-msg">🏆 Ranked Matchmaking</p>
          <p class="waiting-msg" style="font-size: 0.9rem; margin-top: 8px;">
            Compete against other players in ranked matches and climb the leaderboard!
          </p>
          <button class="play-button" @click="initiateRankedWebSocketConnection" style="margin-top: 16px;">
            🎯 Join Ranked Queue
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
    </div>
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
    ...mapActions("multiplayerGame", ["multiplayerGame_createMultiplayerGame"]),

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
        startGame(this.lobbyId, this.lobbyInfo.multiplayerRoundCount, this.lobbyInfo.multiplayerTimer); // broadcast to all clients that game is starting
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
            multiplayerTimer: 60,
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
              this.$store.commit('multiplayerGame/MG_SET_TIMER', this.lobbyInfo?.multiplayerTimer || 60);

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
      console.log('🎯 Starting ranked matchmaking...');
      this.rankedQueueState = 'searching';
      this.rankedErrorMessage = '';
      // Connect to matchmaking WebSocket with callbacks
      connectToMatchmakingWebSocket(this.myId, {
        onQueueJoined: () => {
          // Ensure we stay in searching state
          this.rankedQueueState = 'searching';
        },
        onQueueLeft: () => {
          this.rankedQueueState = 'idle';
        },
        onMatchFound: (matchInfo) => {
          this.rankedQueueState = 'match_found';
          // Find opponent from the players list
          const players = matchInfo.players || [];
          const opponent = players.find(p => p.id !== this.myId) || players[0];
          this.rankedMatchInfo = {
            opponentName: opponent?.username || 'Anonymous Player',
            opponentRating: opponent?.elo || 1200,
            roundCount: 5, // Default for ranked games
            timeLimit: 60, // Default for ranked games
            gameId: matchInfo.gameId
          };
        },
        onQueueTimeout: (message) => {
          this.rankedQueueState = 'error';
          this.rankedErrorMessage = message;
        },
        onError: (error) => {
          this.rankedQueueState = 'error';
          this.rankedErrorMessage = error;
        }
      });
      // Join the queue after a delay to ensure connection is established
      setTimeout(() => {
        console.log('📤 Sending join queue message...');
        joinRankedQueue(this.myId);
      }, 1500);
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
        this.$router.push({
          name: "play",
          query: {
            gameMode: "ranked",
            gameId: this.rankedMatchInfo.gameId
          }
        });
      } else {
        this.rankedQueueState = 'error';
        this.rankedErrorMessage = 'Failed to start game: No game ID received';
      }
    },
    cleanup() {
      if (this.cleanupCalled) {
        console.log("🧹 Cleanup already called, skipping...");
        return;
      }
      this.cleanupCalled = true;
      console.log("🧹 Cleaning up connections for mode:", this.gameModeLabel);
      // Remove window event listener
      window.removeEventListener('beforeunload', this.cleanup);
      if (this.gameModeLabel === 'multiplayer') {
        console.log("🧹 Disconnecting from multiplayer lobby...");
        disconnectLobby();
      } else if (this.gameModeLabel === 'ranked') {
        console.log("🧹 Leaving ranked queue and disconnecting...");
        console.log("🧹 User ID:", this.myId);
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
.lobby-container {
  min-height: 100vh;
  background: var(--dark-grey);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 40px;
}

.logo-icon {
  color: var(--accent-color);
  font-size: 24px;
}

.logo-text {
  color: var(--white);
  text-decoration: none;
  font-size: 24px;
  font-weight: bold;
}

.lobby-card {
  background: var(--card-bg);
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  max-width: 500px;
  width: 100%;
  border: 2px solid var(--border-color);
}

.lobby-title {
  color: var(--white);
  margin-bottom: 8px;
  font-size: 2rem;
}

.lobby-subtitle {
  color: var(--text-secondary);
  margin-bottom: 32px;
  font-size: 1.1rem;
}

.lobby-info {
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.lobby-info h3 {
  color: var(--white);
  margin: 0 0 12px 0;
  font-size: 1.3rem;
}

.lobby-code {
  color: var(--text-secondary);
  margin: 8px 0;
  font-size: 0.9rem;
}

.lobby-code .code {
  background: var(--accent-color);
  color: var(--white);
  padding: 4px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-weight: bold;
  letter-spacing: 1px;
}

.lobby-settings {
  color: var(--text-secondary);
  margin: 8px 0 0 0;
  font-size: 0.9rem;
}

.lobby-players {
  color: var(--text-secondary);
  margin: 8px 0 0 0;
  font-size: 0.9rem;
  font-weight: 500;
}

.ready-count {
  color: var(--accent-color);
  margin: 8px 0 0 0;
  font-size: 0.9rem;
  font-weight: 600;
}

.ranked-info {
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(33, 150, 243, 0.3);
}

.players-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
  color: white;
}

.player-item {
  margin: 8px 0;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  background: var(--accent-color);
  color: var(--white);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: bold;
}

.ready-status {
  display: flex;
  align-items: center;
}

.ready-button {
  background: var(--text-secondary);
  color: var(--white);
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.ready-button:hover {
  background: var(--accent-color);
  transform: translateY(-1px);
}

.ready-button.ready {
  background: var(--accent-color);
}

.ready-indicator {
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: bold;
  background: var(--text-secondary);
  color: var(--white);
}

.ready-indicator.ready {
  background: var(--accent-color);
  color: var(--white);
}

.waiting-msg {
  color: white;
  opacity: 0.8;
  margin: 10px 0;
}

.play-button {
  background: var(--accent-color);
  color: var(--white);
  border: none;
  padding: 16px 32px;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 16px;
}

.play-button:hover {
  background: var(--accent-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.start-game-button {
  background: #4CAF50;
  animation: pulse 2s infinite;
}

.start-game-button:hover {
  background: #45a049;
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
  .lobby-card {
    padding: 24px;
    margin: 0 16px;
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
  }
}
</style>
