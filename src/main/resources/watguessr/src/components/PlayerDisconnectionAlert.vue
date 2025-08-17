<template>
  <div v-if="showDisconnectionAlert" class="disconnection-alert">
    <div class="alert-content">
      <div class="alert-icon">⚠️</div>
      <div class="alert-text">
        <h3>Player Disconnected</h3>
        <p>{{ disconnectedPlayerName }} has left the game</p>
        <div v-if="isGameAbandoned" class="game-abandoned">
          <p class="abandoned-message">Game abandoned - not enough players</p>
          <p class="redirect-message">Redirecting to home in {{ countdown }} seconds...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex';

export default {
  name: 'PlayerDisconnectionAlert',
  data() {
    return {
      countdown: 5,
      countdownInterval: null
    };
  },
  computed: {
    ...mapGetters('multiplayerGame', [
      'multiplayerGame_hasDisconnectedPlayers',
      'multiplayerGame_getDisconnectedPlayers',
      'multiplayerGame_isGameAbandoned'
    ]),
    
    showDisconnectionAlert() {
      return this.multiplayerGame_hasDisconnectedPlayers;
    },
    
    disconnectedPlayerName() {
      const disconnectedPlayers = this.multiplayerGame_getDisconnectedPlayers;
      const playerNames = Object.values(disconnectedPlayers).map(player => player.username);
      return playerNames.join(', ');
    },
    
    isGameAbandoned() {
      return this.multiplayerGame_isGameAbandoned;
    }
  },
  
  watch: {
    isGameAbandoned(newValue) {
      if (newValue) {
        this.startCountdown();
      } else {
        this.stopCountdown();
      }
    }
  },
  
  beforeUnmount() {
    this.stopCountdown();
  },
  
  methods: {
    ...mapActions('multiplayerGame', [
      'multiplayerGame_handleGameAbandoned'
    ]),
    
    startCountdown() {
      this.countdown = 5;
      this.countdownInterval = setInterval(() => {
        this.countdown--;
        if (this.countdown <= 0) {
          this.stopCountdown();
          this.multiplayerGame_handleGameAbandoned();
        }
      }, 1000);
    },
    
    stopCountdown() {
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
    }
  }
};
</script>

<style scoped>
.disconnection-alert {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  background: rgba(239, 68, 68, 0.95);
  border: 2px solid #dc2626;
  border-radius: 12px;
  padding: 20px;
  color: white;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
  max-width: 400px;
  text-align: center;
}

.alert-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.alert-icon {
  font-size: 32px;
  animation: pulse 2s infinite;
}

.alert-text h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.alert-text p {
  margin: 8px 0;
  font-size: 16px;
  opacity: 0.9;
}

.game-abandoned {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
}

.abandoned-message {
  color: #fbbf24;
  font-weight: 600;
  margin: 8px 0;
}

.redirect-message {
  font-size: 14px;
  opacity: 0.8;
  margin: 8px 0;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* Responsive design */
@media (max-width: 768px) {
  .disconnection-alert {
    top: 10px;
    left: 10px;
    right: 10px;
    transform: none;
    max-width: none;
  }
}
</style>
