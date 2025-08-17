<template>
  <div class="lobby-browser">
    <div class="browser-header">
      <h2>Multiplayer Lobbies</h2>
      <div class="header-actions">
        <button class="join-private-button" @click="$emit('open-join-modal')">
          Join Private
        </button>
        <button class="create-lobby-button" @click="$emit('open-create-modal')">
          Create Lobby
        </button>
      </div>
    </div>

    <PublicLobbiesList @lobby-selected="handleLobbySelected" />


  </div>
</template>

<script>
import PublicLobbiesList from './PublicLobbiesList.vue';

export default {
  name: 'LobbyBrowser',
  components: {
    PublicLobbiesList
  },
  emits: ['open-create-modal', 'open-join-modal', 'lobby-created', 'lobby-joined'],
  methods: {
    handleLobbyCreated(lobby) {
      this.$emit('lobby-created', lobby);
    },

    handleLobbyJoined(lobby) {
      this.$emit('lobby-joined', lobby);
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
  border-radius: 16px;
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  background: rgba(42, 42, 44, 0.7);
  display: flex;
  flex-direction: column;
  height: 80vh;
  max-height: 700px;
}

.browser-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 0;
  flex-wrap: wrap;
  gap: 20px;
  position: relative;
  z-index: 1;
  background: rgba(42, 42, 44, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  padding: 16px 20px;
}

.browser-header h2 {
  margin: 0;
  color: var(--white);
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 7px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.header-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  position: relative;
  z-index: 1;
}

.join-private-button,
.create-lobby-button {
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  font-size: 0.81rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  font-family: inherit;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.join-private-button::before,
.create-lobby-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.join-private-button:hover::before,
.create-lobby-button:hover::before {
  left: 100%;
}

.join-private-button {
  background: rgba(255, 255, 255, 0.06);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.join-private-button:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  border-color: rgba(255, 255, 255, 0.2);
}

.create-lobby-button {
  background: linear-gradient(to right, #7FB9FF, #AA7FFF);
  color: white;
  border: 1px solid #7FB9FF;
  box-shadow: 0 4px 15px rgba(127, 185, 255, 0.3);
}

.create-lobby-button:hover {
  background: linear-gradient(to right, #6BA8FF, #9966FF);
  border-color: #6BA8FF;
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(127, 185, 255, 0.4);
}

.create-lobby-button:active,
.join-private-button:active {
  transform: translateY(-1px);
}

@media (max-width: 768px) {
  .lobby-browser {
    padding: 24px 20px;
    margin: 0 16px;
    border-radius: 12px;
  }

  .browser-header {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
    gap: 24px;
  }

  .browser-header h2 {
    font-size: 1rem;
  }

  .header-actions {
    justify-content: center;
    gap: 12px;
  }

  .join-private-button,
  .create-lobby-button {
    padding: 12px 24px;
    font-size: 0.75rem;
    border-radius: 8px;
  }
}

@media (max-width: 480px) {
  .header-actions {
    flex-direction: column;
    gap: 12px;
  }

  .join-private-button,
  .create-lobby-button {
    width: 100%;
  }
}
</style>
