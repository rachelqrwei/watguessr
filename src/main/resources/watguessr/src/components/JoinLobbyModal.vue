<template>
  <div class="modal-overlay" @click="closeModal">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>Join Private Lobby</h2>
        <button class="close-button" @click="closeModal">&times;</button>
      </div>

      <div class="modal-body">
        <form @submit.prevent="joinLobby">
          <div class="form-group">
            <label for="lobbyCode">Lobby Code</label>
            <input
              id="lobbyCode"
              v-model="form.lobbyCode"
              type="text"
              required
              placeholder="Enter 6-character lobby code"
              maxlength="6"
              pattern="[A-Z0-9]{6}"
              style="text-transform: uppercase; letter-spacing: 2px; font-family: monospace; font-size: 18px; text-align: center;"
            />
            <small>Enter the 6-character code provided by the lobby creator</small>
          </div>

          <div class="form-actions">
            <button type="button" class="cancel-button" @click="closeModal">
              Cancel
            </button>
            <button type="submit" class="join-button" :disabled="isJoining || !form.lobbyCode">
              {{ isJoining ? 'Joining...' : 'Join Lobby' }}
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
  name: 'JoinLobbyModal',
  props: {
    isVisible: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      form: {
        lobbyCode: '',
        userId: ''
      },
      isJoining: false
    };
  },
  mounted() {
    const currentUser = this.$store.getters['user/currentUser'];
    if (currentUser) {
      this.form.userId = currentUser.id;
    }
  },
  methods: {
    async joinLobby() {
      try {
        this.isJoining = true;

        const request = {
          lobbyCode: this.form.lobbyCode.toUpperCase(),
          userId: this.form.userId
        };

        const lobby = await LobbyManager.joinLobby(request);
        console.log(lobby);

        this.$emit('lobby-joined', lobby);
        this.closeModal();
      } catch (error) {
        console.error('Failed to join lobby:', error);
        alert('Failed to join lobby. Please check the code and try again.');
      } finally {
        this.isJoining = false;
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
  max-width: 400px;
  width: 90%;
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

.form-group input {
  width: 100%;
  padding: 16px;
  border: 2px solid #444;
  border-radius: 8px;
  background: #2a2a2a;
  color: white;
  font-size: 18px;
  transition: all 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
}

.form-group small {
  display: block;
  margin-top: 8px;
  color: #888;
  font-size: 12px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.cancel-button,
.join-button {
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

.join-button {
  background: #28a745;
  color: white;
}

.join-button:hover:not(:disabled) {
  background: #218838;
}

.join-button:disabled {
  background: #666;
  cursor: not-allowed;
}
</style>
