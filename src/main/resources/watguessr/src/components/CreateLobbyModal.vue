<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Create Lobby</h2>
        <button class="close-button" @click="closeModal">&times;</button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="createLobby">
          <div class="form-group">
            <label for="lobbyName">Lobby Name</label>
            <input
              id="lobbyName"
              v-model="form.lobbyName"
              type="text"
              required
              placeholder="Enter lobby name"
              maxlength="100"
            />
          </div>

          <div class="form-group">
            <label for="gameMode">Game Mode</label>
            <select id="gameMode" v-model="form.gameMode" required>
              <option value="Multiplayer">Multiplayer</option>
              <option value="Ranked">Ranked</option>
            </select>
          </div>

          <div class="form-group">
            <label for="maxPlayers">Max Players</label>
            <select id="maxPlayers" v-model="form.maxPlayers" required>
              <option value="2">2 Players</option>
              <option value="4">4 Players</option>
              <option value="6">6 Players</option>
              <option value="8">8 Players</option>
            </select>
          </div>

          <div class="form-group">
            <label for="roundCount">Rounds</label>
            <select id="roundCount" v-model="form.multiplayerRoundCount" required>
              <option value="3">3 Rounds</option>
              <option value="5">5 Rounds</option>
              <option value="7">7 Rounds</option>
              <option value="10">10 Rounds</option>
            </select>
          </div>

          <div class="form-group">
            <label for="timer">Timer (seconds)</label>
            <select id="timer" v-model="form.multiplayerTimer" required>
              <option value="30">30 seconds</option>
              <option value="45">45 seconds</option>
              <option value="60">60 seconds</option>
              <option value="90">90 seconds</option>
            </select>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="form.isPrivate"
              />
              Private Lobby
            </label>
            <small v-if="form.isPrivate">
              Private lobbies require a code to join
            </small>
          </div>

          <div class="form-actions">
            <button type="button" class="cancel-button" @click="closeModal">
              Cancel
            </button>
            <button type="submit" class="create-button" :disabled="isCreating">
              {{ isCreating ? 'Creating...' : 'Create Lobby' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { LobbyManager } from '@/services/lobbyManager';

export default {
  name: 'CreateLobbyModal',
  props: {
    isVisible: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      form: {
        lobbyName: '',
        gameMode: 'Multiplayer',
        maxPlayers: 4,
        multiplayerRoundCount: 5,
        multiplayerTimer: 60,
        isPrivate: false,
        creatorId: ''
      },
      isCreating: false
    };
  },
  mounted() {
    const currentUser = this.$store.getters['user/currentUser'];
    if (currentUser) {
      this.form.creatorId = currentUser.id;
    }
  },
  methods: {
    async createLobby() {
      try {
        this.isCreating = true;

        const request = {
          ...this.form,
          maxPlayers: Number(this.form.maxPlayers),
          multiplayerRoundCount: Number(this.form.multiplayerRoundCount),
          multiplayerTimer: Number(this.form.multiplayerTimer)
        };

        const lobby = await LobbyManager.createLobby(request);

        this.$emit('lobby-created', lobby);
        this.closeModal();
      } catch (error) {
        console.error('Failed to create lobby:', error);
        alert('Failed to create lobby. Please try again.');
      } finally {
        this.isCreating = false;
      }
    },

    closeModal() {
      this.$emit('close');
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #1a1a1a;
  border-radius: 12px;
  padding: 0;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  border: 2px solid #333;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #333;
}

.modal-header h2 {
  margin: 0;
  color: white;
  font-size: 1.5rem;
}

.close-button {
  background: none;
  border: none;
  color: #888;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-button:hover {
  background: #333;
  color: white;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: white;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #444;
  border-radius: 6px;
  background: #2a2a2a;
  color: white;
  font-size: 14px;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.form-group small {
  display: block;
  margin-top: 4px;
  color: #888;
  font-size: 12px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.cancel-button,
.create-button {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-button {
  background: #444;
  color: white;
}

.cancel-button:hover {
  background: #555;
}

.create-button {
  background: #007bff;
  color: white;
}

.create-button:hover:not(:disabled) {
  background: #0056b3;
}

.create-button:disabled {
  background: #666;
  cursor: not-allowed;
}
</style>
