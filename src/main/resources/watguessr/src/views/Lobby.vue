<template>
  <div class="lobby-container">
    <div class="logo-container">
      <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
      <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
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
            {{ lobbyInfo.multiplayerRoundCount }} rounds • {{ lobbyInfo.multiplayerTimer }}s timer
          </p>
          <p class="lobby-players">
            Players: {{ users.length }}/{{ lobbyInfo.maxPlayers }}
          </p>
        </div>

        <ul class="players-list">
          <li v-for="u in users" :key="u.id">
            {{ u.username }} <span v-if="u.id === myId">(YOU)</span>
          </li>
        </ul>

        <p v-if="users.length < 2" class="waiting-msg">
          Waiting for more players to join...
        </p>

        <button
          v-if="users.length >= 2 && !gameStarted"
          class="play-button"
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
        <button class="play-button" @click="goToPlay">PLAY RANKED</button>
      </div>
    </div>
  </div>
</template>

<script>
import { mapMutations } from "vuex";
import { connectLobby, joinLobby, startGame, disconnectLobby } from "@/services/lobby";
import { LobbyManager } from "@/services/lobbyManager";

export default {
  name: "Lobby",
  data() {
    return {
      users: [],
      myId: "",
      gameStarted: false,
      lobbyInfo: null
    };
  },
  computed: {
    gameModeLabel() {
      const mode = (this.$route.query.gameMode ?? "").toString();
      return mode ? mode : "singleplayer";
    },
    lobbyId() {
      return this.$route.query.lobbyId;
    }
  },
  methods: {
    ...mapMutations("gameInfo", ["SET_GAME_MODE"]),

    goToPlay() {
      this.SET_GAME_MODE(this.gameModeLabel);
      this.$router.push({ name: "play", query: { gameMode: this.gameModeLabel } });
    },

    startGameClick() {
      if (this.lobbyId) {
        startGame(this.lobbyId); // broadcast to all clients that game is starting
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
    }
  },
  mounted() {
    const currentUser = this.$store.getters["user/currentUser"];
    this.myId = currentUser.id;

    if (this.gameModeLabel === 'multiplayer') {
      this.fetchLobbyInfo();
      
      // Connect to lobby WebSocket
      connectLobby(
        (lobbyUpdate) => {
          this.users = lobbyUpdate.users;
        },
        (startInfo) => {
          this.gameStarted = true;
          this.SET_GAME_MODE("multiplayer");
          this.$router.push({ name: "play", query: { gameMode: "multiplayer" } });
        }
      );

      if (this.lobbyId) {
        joinLobby(currentUser, this.lobbyId);
      }
    }
  },
  beforeUnmount() {
    if (this.gameModeLabel === 'multiplayer') disconnectLobby();
  },
};
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

.players-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
  color: white;
}

.players-list li {
  margin: 6px 0;
  font-weight: bold;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.1);
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
}
</style>
