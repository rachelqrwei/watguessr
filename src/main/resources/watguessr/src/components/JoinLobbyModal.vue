<template>
  <Transition name="modal-fade" appear>
    <div v-if="isVisible" class="modal-overlay" @click="closeModal">
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
              placeholder="LOBBY CODE"
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
  </Transition>
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
    const currentUser = this.$store.getters['user/getCurrentUser'];
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
  z-index: 6000;
}

.modal-content {
  background: rgba(42, 42, 44, 0.5);
  border-radius: 12px;
  padding: 0;
  max-width: 400px;
  width: 90%;
  border: 1px solid #333;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 15px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h2 {
  color: var(--white);
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
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
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.form-group input {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: rgba(42, 42, 44, 0.8);
  color: white;
  font-size: 18px;
  transition: all 0.2s;
}

.form-group input::placeholder {
  font-family: "Red Hat Text", sans-serif;
  font-size: 0.75rem;
  letter-spacing: 0.8px;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
}

.form-group input:focus {
  outline: none;
  border-color: #7FB9FF;
  box-shadow: 0 0 0 2px rgba(127, 185, 255, 0.25);
}

.form-group small {
  display: block;
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
  font-size: 0.81rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.cancel-button {
  background: rgba(255, 255, 255, 0.06);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.join-button {
  background: linear-gradient(to right, rgba(127, 185, 255, 0.1), rgba(170, 127, 255, 0.2));
  color: var(--white);
  border: 1px solid #7FB9FF;
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

.join-button:hover:not(:disabled) {
  border-color: #6BA8FF;
  transform: translateY(-2px);
}

.join-button:hover:not(:disabled)::before {
  background: linear-gradient(to right, #6BA8FF, #9966FF);
  opacity: 0.9;
}

.join-button:disabled {
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
  border-color: rgba(255, 255, 255, 0.06);
  transform: none;
}


.modal-fade-enter-from .modal-content {
  transform: translateY(-50px) scale(0.8);
  opacity: 0;
}

.modal-fade-leave-to .modal-content {
  transform: translateY(50px) scale(0.8);
  opacity: 0;
}
</style>
