<template>
  <div
    class="stopwatch-container"
    :style="{
      background: `conic-gradient(#FFCB3B ${progressAngle}deg, #2b2b2b ${progressAngle}deg)`
    }"
  />
  <div class="stopwatch">
    <div>{{ formattedTimeLeft }}</div>
  </div>
</template>

<script>
import {mapActions, mapGetters, mapMutations} from 'vuex';

export default {
  name: 'Stopwatch',

  props: {
    shouldStart: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      interval: null,
    };
  },

  computed: {
    ...mapGetters('gameInfo', ['getGameMode']),
    ...mapGetters('singleplayerGame', ['singleplayerGame_getTimer']),
    ...mapGetters('multiplayerGame', ['multiplayerGame_getTimer']),
    ...mapGetters('guess', ['getGuessTime']),
    totalTime() {
      if (this.getGameMode == 'singleplayer') {
        return this.singleplayerGame_getTimer;
      }
      if (this.getGameMode == 'multiplayer') {
        return this.multiplayerGame_getTimer;
      }
      return 60000; // Default 60 seconds in milliseconds
    },
    progressAngle() {
      if (!this.totalTime) return 0;
      const percent = this.getGuessTime / this.totalTime;
      return percent * 360;
    },

    formattedTimeLeft() {
      const ms = Math.floor((this.getGuessTime % 1000) / 10);
      const totalSeconds = Math.floor(this.getGuessTime / 1000);
      const s = Math.floor(totalSeconds % 60);
      const m = Math.floor(totalSeconds / 60);
      const pad = (n, z = 2) => String(n).padStart(z, '0');
      return `${pad(m)}:${pad(s)}.${pad(ms)}`;
    },
  },

  watch: {
    shouldStart(newVal) {
      if (newVal) {
        this.startTimer();
      } else {
        this.clearTimer();
      }
    }
  },

  mounted() {
    // Don't start timer automatically - wait for shouldStart prop
  },

  beforeUnmount() {
    this.clearTimer();
  },

  methods: {
    ...mapMutations('guess', [
      'SET_TIME'
    ]),
    ...mapActions('round', [
      "endRound"
    ]),
    ...mapActions('guess', [
      'submitGuess'
    ]),
    ...mapMutations('singleplayerGame', [
      'SG_CHANGE_VIEW',
      'SG_INCREMENT_ROUND'
    ]),
    startTimer() {
      this.clearTimer();

      // Reset timer to 0
      this.SET_TIME(0);

      this.interval = setInterval(async () => {
        if (this.getGuessTime < this.totalTime) {
          // Increment by 100ms (0.1 seconds)
          this.SET_TIME(this.getGuessTime + 100);
        } else {
          // Time limit reached
          this.clearTimer();
          // Use the same flow as manual submission so scoring/ending logic is consistent
          await this.submitGuess();
        }
      }, 100); // Update every 100ms
    },

    clearTimer() {
      if (this.interval !== null) {
        clearInterval(this.interval);
        this.interval = null;
      }
    },
  },
};
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oxanium:wght@200..800&display=swap');

.stopwatch {
  position: fixed;
  top: 29px;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--color-gray-700);
  color: white;
  padding: 19px;
  font-size: 28.5px;
  width: 190px;
  line-height: 38px;
  text-align: center;
  font-weight: bolder;
  font-family: 'Oxanium', sans-serif;
  border-radius: 25px;
  z-index: 999;
}

.stopwatch-container {
  position: fixed;
  top: 5px;
  left: 50%;
  transform: translateX(-50%);
  width: 237.5px;
  border-radius: 50px;
  height: 123.5px;
  background-color: var(--dark-grey);
  z-index: 998;
  border: 19px solid #2A2A2C;
}
</style>
