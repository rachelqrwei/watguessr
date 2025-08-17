<template>
  <div class="countdown-overlay" v-if="isVisible">
    <div class="countdown-container">
      <div class="countdown-number" :class="{ 'animate': isAnimating }">
        {{ countdownNumber }}
      </div>
      <div class="countdown-text">
        {{ countdownText }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CountdownTimer',
  props: {
    isVisible: {
      type: Boolean,
      default: false
    },
    duration: {
      type: Number,
      default: 3000 // 3 seconds total
    }
  },
  data() {
    return {
      countdownNumber: 3,
      countdownText: 'Get Ready!',
      isAnimating: false,
      countdownInterval: null
    };
  },
  watch: {
    isVisible(newVal) {
      if (newVal) {
        this.startCountdown();
      } else {
        this.stopCountdown();
      }
    }
  },
  methods: {
    startCountdown() {
      this.countdownNumber = 3;
      this.countdownText = 'Get Ready!';
      this.isAnimating = false;
      
      // Start the countdown
      this.countdownInterval = setInterval(() => {
        if (this.countdownNumber > 1) {
          this.countdownNumber--;
          this.countdownText = 'Get Ready!';
          this.isAnimating = true;
          
          // Reset animation after a short delay
          setTimeout(() => {
            this.isAnimating = false;
          }, 200);
        } else if (this.countdownNumber === 1) {
          this.countdownNumber = 0;
          this.countdownText = 'GO!';
          this.isAnimating = true;
          
          // Emit start event and hide countdown after showing "GO!"
          setTimeout(() => {
            this.$emit('countdown-complete');
            this.isVisible = false;
          }, 500);
        }
      }, 1000);
    },
    
    stopCountdown() {
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
    }
  },
  
  beforeUnmount() {
    this.stopCountdown();
  }
};
</script>

<style scoped>
.countdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.countdown-container {
  text-align: center;
  color: white;
}

.countdown-number {
  font-size: 120px;
  font-weight: 900;
  color: var(--yellow);
  text-shadow: 0 0 30px rgba(255, 215, 0, 0.5);
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.countdown-number.animate {
  transform: scale(1.2);
  text-shadow: 0 0 50px rgba(255, 215, 0, 0.8);
}

.countdown-text {
  font-size: 24px;
  font-weight: 600;
  color: white;
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* Animation for the countdown numbers */
@keyframes countdownPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.1); }
  100% { transform: scale(1); }
}

.countdown-number.animate {
  animation: countdownPulse 0.3s ease-in-out;
}

/* Responsive design */
@media (max-width: 768px) {
  .countdown-number {
    font-size: 80px;
  }
  
  .countdown-text {
    font-size: 18px;
  }
}
</style>
