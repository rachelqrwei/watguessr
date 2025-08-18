<template>
  <Transition name="modal-fade" appear>
    <div v-if="isVisible" class="modal-overlay" @click="closeModal">
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

          <div class="form-row">
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
                <option value="15000">15 seconds</option>
                <option value="20000">20 seconds</option>
                <option value="30000">30 seconds</option>
                <option value="45000">45 seconds</option>
                <option value="60000">60 seconds</option>
                <option value="90000">90 seconds</option>
              </select>
            </div>
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
  </Transition>
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
        multiplayerTimer: 30000,
        isPrivate: false,
        creatorId: ''
      },
      isCreating: false
    };
  },
  mounted() {
    const currentUser = this.$store.getters['user/getCurrentUser'];
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
  z-index: 6000;
}

.modal-content {
  background: rgba(42, 42, 44, 0.5);
  border-radius: 12px;
  padding: 0;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
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

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-row .form-group {
  flex: 1;
  margin-bottom: 0;
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

.form-group input,
.form-group select {
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

.form-group select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;

  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 16px;
  padding-right: 48px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-group select:hover {
  border-color: rgba(255, 255, 255, 0.2);
  background-color: rgba(42, 42, 44, 0.9);
}

.form-group select option {
  background: rgba(42, 42, 44, 0.98);
  color: white;
  font-family: "Red Hat Text", sans-serif;
  font-size: 0.75rem;
  font-weight: 400;
  letter-spacing: 0.6px;
  padding: 16px 12px;
  border: none;
  outline: none;
}

.form-group select option:hover {
  background: rgba(127, 185, 255, 0.2);
}

.form-group select option:checked {
  background: rgba(127, 185, 255, 0.3);
  color: #7FB9FF;
}

.form-group select:not([size]) {
  font-family: "Red Hat Text", sans-serif;
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: var(--white);
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #7FB9FF;
  box-shadow: 0 0 0 2px rgba(127, 185, 255, 0.25);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
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
.create-button {
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

.create-button {
  background: linear-gradient(to right, rgba(127, 185, 255, 0.1), rgba(170, 127, 255, 0.2));
  color: var(--white);
  border: 1px solid #7FB9FF;
  position: relative;
}

.create-button::before {
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

.create-button:hover:not(:disabled) {
  border-color: #6BA8FF;
  transform: translateY(-2px);
}

.create-button:hover:not(:disabled)::before {
  background: linear-gradient(to right, #6BA8FF, #9966FF);
  opacity: 0.9;
}

.create-button:disabled {
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
