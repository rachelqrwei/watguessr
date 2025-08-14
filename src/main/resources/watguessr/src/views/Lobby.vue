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
    </div>
  </div>
</template>

<script>
import { mapMutations } from "vuex";
import { connectLobby, joinLobby, startGame, disconnectLobby } from "@/services/lobby";

export default {
  name: "Lobby",
  data() {
    return {
      users: [],
      myId: "",
      gameStarted: false,
    };
  },
  computed: {
    gameModeLabel() {
      const mode = (this.$route.query.gameMode ?? "").toString();
      return mode ? mode : "singleplayer";
    },
  },
  methods: {
    ...mapMutations("gameInfo", ["SET_GAME_MODE"]),

    goToPlay() {
      this.SET_GAME_MODE("singleplayer");
      this.$router.push({ name: "play", query: { gameMode: "singleplayer" } });
    },

    startGameClick() {
      startGame(); // broadcast to all clients that game is starting
    },
  },
  mounted() {
    const currentUser = this.$store.getters["user/currentUser"];
    this.myId = currentUser.id;

    if (this.gameModeLabel === "multiplayer") {
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

      joinLobby(currentUser);
    }
  },
  beforeUnmount() {
    if (this.gameModeLabel === "multiplayer") disconnectLobby();
  },
};
</script>

<style scoped>
.players-list {
  list-style: none;
  padding: 0;
  margin: 16px 0;
  color: white;
}
.players-list li {
  margin: 6px 0;
  font-weight: bold;
}
.waiting-msg {
  color: white;
  opacity: 0.8;
  margin: 10px 0;
}
</style>
