<template>
  <div class="countdown-overlay" v-if="isVisible && getCurrentRound < 1">
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
  <div v-if="isVisible && getCurrentRound >= 1" class="progress-overlay">
    <div class="progress-container">
      <div class="progress-bar" :style="{ width: progressWidth + '%' }"></div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "CountdownTimer",
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
        if (this.getCurrentRound < 1) {
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
      this.progressWidth = 100;

      const stepTime = 50; // update every 50ms
      const step = (100 * stepTime) / this.duration;

      this.progressTimer = setInterval(() => {
        if (this.progressWidth > 0) {
          this.progressWidth -= step;
        } else {
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
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* === Progress Overlay === */
.progress-overlay {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  z-index: 9999;
}
.progress-container {
  width: 100%;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  overflow: hidden;
}
.progress-bar {
  height: 100%;
  background: var(--yellow);
  transition: width 50ms linear;
}
</style>
