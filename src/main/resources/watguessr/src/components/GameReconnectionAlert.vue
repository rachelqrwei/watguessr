<template>
  <div v-if="showReconnectAlert" class="reconnection-alert">
    <div class="alert-content">
      <div class="alert-icon">🔁</div>
      <div class="alert-text">
        <h3>Connection Lost</h3>
        <p>Attempting to reconnect to your game...</p>
        <p class="countdown">Time remaining: {{ countdown }} seconds</p>
      </div>
      <button class="cancel-button" @click="cancelReconnection">Cancel</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GameReconnectionAlert',
  data() {
    return {
      showReconnectAlert: false,
      countdown: 20,
      countdownInterval: null
    };
  },
  mounted() {
    // Listen for disconnection events
    this.$socket?.on('disconnect', () => {
      this.startReconnectionCountdown();
    });
    
    this.$socket?.on('connect', () => {
      this.stopReconnectionCountdown();
    });
  },
  methods: {
    startReconnectionCountdown() {
      this.showReconnectAlert = true;
      this.countdown = 20;
      
      this.countdownInterval = setInterval(() => {
        this.countdown--;
        
        if (this.countdown <= 0) {
          this.stopReconnectionCountdown();
          this.$emit('reconnection-failed');
        }
      }, 1000);
    },
    
    stopReconnectionCountdown() {
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
      this.showReconnectAlert = false;
    },
    
    cancelReconnection() {
      this.stopReconnectionCountdown();
      this.$emit('reconnection-cancelled');
    }
  },
  beforeUnmount() {
    this.stopReconnectionCountdown();
  }
};
</script>

<style scoped>
.reconnection-alert {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  background: rgba(255, 193, 7, 0.95);
  border: 2px solid #ffc107;
  border-radius: 12px;
  padding: 20px;
  color: #333;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(8px);
  max-width: 400px;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.alert-icon {
  font-size: 32px;
}

.alert-text {
  flex: 1;
}

.alert-text h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.alert-text p {
  margin: 8px 0;
  font-size: 16px;
}

.countdown {
  font-weight: 600;
}

.cancel-button {
  background: rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.2);
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.cancel-button:hover {
  background: rgba(0, 0, 0, 0.2);
}
</style>
