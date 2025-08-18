<template>
    <div class="public-lobbies">
    <div class="public-lobbies-container">
            <div class="scrollable-content">
        <div v-if="isLoading" class="loading">
          Loading lobbies...
        </div>

        <div v-else-if="lobbies.length === 0" class="no-lobbies">
          <p>No public lobbies available</p>
          <p class="subtitle">Create a new lobby to get started!</p>
        </div>

        <div v-else class="lobbies-grid">
      <div
        v-for="lobby in lobbies"
        :key="lobby.id"
        class="lobby-card"
        @click="joinLobby(lobby)"
      >
        <div class="lobby-header">
          <h4 class="lobby-name">{{ lobby.lobbyName }}</h4>
          <span class="lobby-mode">{{ lobby.gameMode }}</span>
        </div>

        <div class="lobby-details">
          <div class="detail-row">
            <span class="label">Players:</span>
            <span class="value">{{ lobby.currentPlayers }}/{{ lobby.maxPlayers }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Rounds:</span>
            <span class="value">{{ lobby.multiplayerRoundCount }}</span>
          </div>
          <div class="detail-row">
            <span class="label">Timer:</span>
            <span class="value">{{ displayTimer(lobby.multiplayerTimer) }}s</span>
          </div>
        </div>

        <div class="lobby-actions">
          <button class="join-button" @click.stop="joinLobby(lobby)">
            Join Lobby
          </button>
          </div>
        </div>
      </div>
      </div>

      <div class="refresh-section">
        <button class="refresh-button" @click="refreshLobbies" :disabled="isLoading">
          <span v-if="isLoading">Refreshing...</span>
          <span v-else>Refresh</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { LobbyManager } from '@/services/lobbyManager';
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export default {
  name: 'PublicLobbiesList',
  data() {
    return {
      lobbies: [],
      isLoading: false,
      stompClient: null
    };
  },
  computed: {
    displayTimer() {
      return (timerMs) => {
        // Convert milliseconds to seconds for display
        return Math.round(timerMs / 1000);
      };
    }
  },
  mounted() {
    this.refreshLobbies();
    this.connectToLobbyUpdates();
  },
  beforeUnmount() {
    this.disconnectFromLobbyUpdates();
  },
  methods: {
    async refreshLobbies() {
      try {
        this.isLoading = true;
        this.lobbies = await LobbyManager.getPublicLobbies();
      } catch (error) {
        console.error('Failed to fetch public lobbies:', error);
        this.lobbies = [];
      } finally {
        this.isLoading = false;
      }
    },

    joinLobby(lobby) {
      this.$emit('lobby-selected', lobby);
    },

    connectToLobbyUpdates() {
      const socket = new SockJS("http://localhost:5173/ws-game");
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        debug: (msg) => console.log(msg),
        reconnectDelay: 5000,
        onConnect: () => {
          console.log("Connected to lobby updates");

          // Subscribe to public lobby updates
          this.stompClient.subscribe("/topic/lobbies/public", (message) => {
            console.log("Received public lobby update:", message.body);
            // Refresh the lobby list when we receive an update
            this.refreshLobbies();
          });
        },
        onStompError: (frame) => {
          console.error("STOMP error:", frame);
        },
      });

      this.stompClient.activate();
    },

    disconnectFromLobbyUpdates() {
      if (this.stompClient) {
        this.stompClient.deactivate();
        this.stompClient = null;
      }
    }
  }
};
</script>

<style scoped>
.public-lobbies {
  background: rgba(255, 255, 255, 0.05);
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.public-lobbies-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
  min-height: 0;
}

/* Custom scrollbar styling to match Home.vue theme */
.scrollable-content::-webkit-scrollbar {
  width: 6px;
}

.scrollable-content::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}

.scrollable-content::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.refresh-section {
  display: flex;
  justify-content: flex-end;
  padding-top: 16px;
  flex-shrink: 0;
  margin-right: 13px;
}

.refresh-button {
  background: rgba(255, 255, 255, 0.06);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.12);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.8rem;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  transition: all 0.2s;
}

.refresh-button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
}

.refresh-button:disabled {
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
  border-color: rgba(255, 255, 255, 0.06);
}




.loading,
.no-lobbies {
  text-align: center;
  padding: 40px 20px;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.85rem;
  letter-spacing: 0.7px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.no-lobbies .subtitle {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-top: 8px;
  opacity: 0.7;
}

.lobbies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 20px;
}

.lobby-card {
  background: rgba(42, 42, 44, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
}

.lobby-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.lobby-name {
  margin: 0;
  color: white;
  font-size: 1.1rem;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.3px;
}

.lobby-mode {
  background: rgba(255, 255, 255, 0.06);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.12);
  padding: 4px 8px;
  border-radius: 4px;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.7rem;
  letter-spacing: 0.5px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.lobby-details {
  background: rgba(255, 255, 255, 0.05);
  padding: 24px;
  border-radius: 14px;
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.label {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.value {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--white);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.lobby-actions {
  text-align: center;
}

.join-button {
  background: linear-gradient(to right, rgba(127, 185, 255, 0.1), rgba(170, 127, 255, 0.2));
  color: #7FB9FF;
  border: 1px solid #7FB9FF;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.81rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  transition: all 0.2s;
  width: 100%;
  position: relative;
}

.join-button::before {
  content: '';
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  bottom: -1px;
  background: linear-gradient(to right, #7FB9FF, #AA7FFF);
  border-radius: 6px;
  z-index: -1;
  opacity: 0.7;
}

.join-button:hover {
  border-color: #6BA8FF;
  transform: translateY(-2px);
}

.join-button:hover::before {
  background: linear-gradient(to right, #6BA8FF, #9966FF);
  opacity: 0.9;
}

@media (max-width: 768px) {
  .lobbies-grid {
    grid-template-columns: 1fr;
  }
}
</style>
