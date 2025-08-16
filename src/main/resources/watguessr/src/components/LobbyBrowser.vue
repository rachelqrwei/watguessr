<template>
  <div class="lobby-browser">
    <div class="browser-header">
      <h2>Multiplayer Lobbies</h2>
      <div class="header-actions">
        <button class="join-private-button" @click="showJoinModal = true">
          Join Private
        </button>
        <button class="create-lobby-button" @click="showCreateModal = true">
          Create Lobby
        </button>
      </div>
    </div>

    <PublicLobbiesList @lobby-selected="handleLobbySelected" />

    <!-- Create Lobby Modal -->
    <CreateLobbyModal
      v-if="showCreateModal"
      :isVisible="showCreateModal"
      @close="showCreateModal = false"
      @lobby-created="handleLobbyCreated"
    />

    <!-- Join Private Lobby Modal -->
    <JoinLobbyModal
      v-if="showJoinModal"
      :isVisible="showJoinModal"
      @close="showJoinModal = false"
      @lobby-joined="handleLobbyJoined"
    />
  </div>
</template>

<script>
import CreateLobbyModal from './CreateLobbyModal.vue';
import JoinLobbyModal from './JoinLobbyModal.vue';
import PublicLobbiesList from './PublicLobbiesList.vue';

export default {
  name: 'LobbyBrowser',
  components: {
    CreateLobbyModal,
    JoinLobbyModal,
    PublicLobbiesList
  },
  data() {
    return {
      showCreateModal: false,
      showJoinModal: false
    };
  },
  methods: {
    handleLobbyCreated(lobby) {
      console.log('Lobby created:', lobby);
      // Navigate to the lobby
      this.$router.push({ 
        name: 'lobby', 
        query: { 
          gameMode: 'multiplayer',
          lobbyId: lobby.id,
          lobbyCode: lobby.lobbyCode
        } 
      });
    },

    handleLobbyJoined(lobby) {
      console.log('Lobby joined:', lobby);
      // Navigate to the lobby
      this.$router.push({ 
        name: 'lobby', 
        query: { 
          gameMode: 'multiplayer',
          lobbyId: lobby.id,
          lobbyCode: lobby.lobbyCode
        } 
      });
    },

    handleLobbySelected(lobby) {
      console.log('Lobby selected:', lobby);
      // Navigate to the lobby
      this.$router.push({ 
        name: 'lobby', 
        query: { 
          gameMode: 'multiplayer',
          lobbyId: lobby.id
        } 
      });
    }
  }
};
</script>

<style scoped>
.lobby-browser {
  background: #1a1a1a;
  border-radius: 16px;
  padding: 32px;
  border: 2px solid #333;
  max-width: 1200px;
  margin: 0 auto;
}

.browser-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 20px;
}

.browser-header h2 {
  margin: 0;
  color: white;
  font-size: 1.75rem;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.join-private-button,
.create-lobby-button {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.join-private-button {
  background: #6c757d;
  color: white;
}

.join-private-button:hover {
  background: #5a6268;
}

.create-lobby-button {
  background: #007bff;
  color: white;
}

.create-lobby-button:hover {
  background: #0056b3;
}

@media (max-width: 768px) {
  .lobby-browser {
    padding: 20px;
    margin: 0 16px;
  }

  .browser-header {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  .header-actions {
    justify-content: center;
  }
}
</style>
