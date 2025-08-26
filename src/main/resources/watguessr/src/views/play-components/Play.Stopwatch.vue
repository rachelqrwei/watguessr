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
    ...mapGetters('rankedGame', ['rankedGame_getTimer']),
    totalTime() {
      if (this.getGameMode == 'singleplayer') {
        return this.singleplayerGame_getTimer;
      }
      if (this.getGameMode == 'multiplayer') {
        return this.multiplayerGame_getTimer;
      }
      if (this.getGameMode == 'ranked') {
        return this.rankedGame_getTimer;
      }
      return 30000; // Fallback for unknown modes
    },
    progressAngle() {
      if (!this.totalTime) return 0;
      const percent = this.getGuessTime / this.totalTime;
      return percent * 360;
    },

    formattedTimeLeft() {
      const remainingTime = Math.max(0, this.totalTime - this.getGuessTime);
      const ms = Math.floor((remainingTime % 1000) / 10);
      const totalSeconds = Math.floor(remainingTime / 1000);
      const s = Math.floor(totalSeconds % 60);
      const m = Math.floor(totalSeconds / 60);
      const pad = (n, z = 2) => String(n).padStart(z, '0');
      return `${pad(m)}:${pad(s)}.${pad(ms)}`;
    },
  },
  mounted() {
    // Don't start timer automatically - wait for shouldStart prop
    this.startTimer();
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
    ]),
    startTimer() {
      this.clearTimer();

      // Reset timer to 0
      this.SET_TIME(0);

      this.interval = setInterval(async () => {
        if (this.getGuessTime < this.totalTime) {
          this.SET_TIME(this.getGuessTime + 100);
        } else {
          // Time limit reached
          this.clearTimer();
          // Set default values for timeout submission
          // This ensures the backend receives valid data for default guess fallback
          if (!this.$store.state.guess.building || this.$store.state.guess.guessX === null) {
            this.$store.commit('guess/SET_BUILDING_AND_LOCATIONS', { building: 'NO_GUESS', guessX: null, guessY: null });
          }
          if (!this.$store.state.guess.floor) {
            this.$store.commit('guess/SET_FLOOR', 'UNKNOWN');
          }
          if (!this.$store.state.guess.time) {
            this.$store.commit('guess/SET_TIME', this.totalTime);
          }

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
