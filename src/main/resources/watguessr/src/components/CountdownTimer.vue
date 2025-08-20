<template>
  <div class="countdown-overlay" v-if="isVisible && getCurrentRound === 1">
    <div class="countdown-container">
      <div class="countdown-number" :class="{ 'animate': isAnimating }">
        {{ countdownNumber }}
      </div>
      <div class="countdown-text">
        {{ countdownText }}
      </div>
    </div>
  </div>

  <!-- Progress bar appears when round >= 1 -->
  <div v-if="isVisible && getCurrentRound > 1" class="progress-overlay">
    <div class="progress-container">
      <div class="progress-bar" :style="{ width: progressWidth + '%' }"></div>
    </div>
    <div class="progress-text">
      {{ countdownText }}
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "CountdownTimer",
  emits: ["countdown-complete"],
  props: {
    isVisible: {
      type: Boolean,
      default: false,
    },
    duration: {
      type: Number,
      default: 3000, // 3 seconds
    },
  },
  data() {
    return {
      countdownNumber: 3,
      countdownText: "Get Ready!",
      isAnimating: false,
      countdownInterval: null,
      progressWidth: 100,
      progressTimer: null,
    };
  },
  watch: {
    isVisible(newVal) {
      if (newVal) {
        // reset animation state whenever we show
        this.stopCountdown();
        this.stopProgressBar();
        this.countdownNumber = 3;
        this.progressWidth = 0;

        if (this.getCurrentRound === 1) {
          this.startCountdown();
        } else {
          this.startProgressBar();
        }
      } else {
        this.stopCountdown();
        this.stopProgressBar();
      }
    },
  },
  computed: {
    ...mapGetters("multiplayerGame", ["multiplayerGame_getCurrentRound"]),
    ...mapGetters("singleplayerGame", ["singleplayerGame_getCurrentRound"]),
    ...mapGetters("gameInfo", ["getGameMode"]),
    getCurrentRound() {
      if (this.getGameMode === "singleplayer") {
        return this.singleplayerGame_getCurrentRound;
      }
      if (this.getGameMode === "multiplayer") {
        return this.multiplayerGame_getCurrentRound;
      }
    },
  },
  methods: {
    // === Countdown ===
    startCountdown() {
      this.countdownNumber = 3;
      this.countdownText = "Get Ready!";
      this.isAnimating = false;

      this.countdownInterval = setInterval(() => {
        if (this.countdownNumber > 1) {
          this.countdownNumber--;
          this.countdownText = "Get Ready!";
          this.isAnimating = true;

          setTimeout(() => {
            this.isAnimating = false;
          }, 200);
        } else if (this.countdownNumber === 1) {
          this.countdownNumber = 0;
          this.countdownText = "GO!";
          this.isAnimating = true;

          setTimeout(() => {
            this.$emit("countdown-complete");
          }, 500);
        }
      }, 1000);
    },
    stopCountdown() {
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
    },

    // === Progress Bar ===
    startProgressBar() {
      this.progressWidth = 0;
      this.countdownText = "Get Ready!";

      const stepTime = 50; // update every 50ms
      const step = (100 * stepTime) / this.duration;

      this.progressTimer = setInterval(() => {
        if (this.progressWidth < 100) {
          this.progressWidth += step;

          // Update text based on progress
          if (this.progressWidth < 66) {
            this.countdownText = "Get Ready!";
          } else if (this.progressWidth < 100) {
            this.countdownText = "GO!";
          } else {
            this.countdownText = "GO!";
          }
        } else {
          this.progressWidth = 100;
          this.stopProgressBar();
          this.$emit("countdown-complete");
        }
      }, stepTime);
    },
    stopProgressBar() {
      if (this.progressTimer) {
        clearInterval(this.progressTimer);
        this.progressTimer = null;
      }
    },
  },
  beforeUnmount() {
    this.stopCountdown();
    this.stopProgressBar();
  },
};
</script>

<style scoped>
/* === Countdown Overlay === */
.countdown-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.65);
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
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* === Progress Overlay === */
.progress-overlay {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  max-width: 400px;
  z-index: 9999;
}

.progress-container {
  width: 100%;
  height: 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  margin-bottom: 8px;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--yellow), #ffd700);
  border-radius: 6px;
  transition: width 50ms linear;
  box-shadow: 0 0 10px rgba(255, 215, 0, 0.4);
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: white;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-align: center;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.8);
}

/* Responsive design */
@media (max-width: 768px) {
  .progress-overlay {
    width: 90%;
    top: 15px;
  }

  .progress-container {
    height: 10px;
  }
}
</style>
