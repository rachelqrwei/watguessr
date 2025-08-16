<template>
  <div class="public-lobbies">
    <div class="header">
      <h3>Public Lobbies</h3>
      <button class="refresh-button" @click="refreshLobbies" :disabled="isLoading">
        <span v-if="isLoading">Refreshing...</span>
        <span v-else>Refresh</span>
      </button>
    </div>

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
            <span class="value">{{ lobby.multiplayerTimer }}s</span>
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
  background: #1a1a1a;
  border-radius: 12px;
  padding: 24px;
  border: 2px solid #333;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h3 {
  margin: 0;
  color: white;
  font-size: 1.25rem;
}

.refresh-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.refresh-button:hover:not(:disabled) {
  background: #0056b3;
}

.refresh-button:disabled {
  background: #666;
  cursor: not-allowed;
}

.loading,
.no-lobbies {
  text-align: center;
  padding: 40px 20px;
  color: #888;
}

.no-lobbies .subtitle {
  font-size: 14px;
  margin-top: 8px;
  opacity: 0.7;
}

.lobbies-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.lobby-card {
  background: #2a2a2a;
  border: 1px solid #444;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.2s;
}

.lobby-card:hover {
  border-color: #007bff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.2);
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
  font-weight: 600;
}

.lobby-mode {
  background: #007bff;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.lobby-details {
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
  color: #888;
  font-size: 14px;
}

.value {
  color: white;
  font-weight: 500;
  font-size: 14px;
}

.lobby-actions {
  text-align: center;
}

.join-button {
  background: #28a745;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  width: 100%;
}

.join-button:hover {
  background: #218838;
}

@media (max-width: 768px) {
  .lobbies-grid {
    grid-template-columns: 1fr;
  }

  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
}
</style>
