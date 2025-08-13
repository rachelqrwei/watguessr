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

  data() {
    return {
      interval: null,
      totalTime: 10000, // 60 seconds
    };
  },

  computed: {
    ...mapGetters('guess', ['getGuessTime']),

    progressAngle() {
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

  mounted() {
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
    ...mapMutations('singleplayer', [
      'SG_CHANGE_VIEW',
      'SG_INCREMENT_ROUND'
    ]),
    startTimer() {
      this.clearTimer();

      this.SET_TIME(0);

      this.interval = setInterval(async () => {
        if (this.getGuessTime < this.totalTime) {
          this.SET_TIME(this.getGuessTime + 100); // increase by 100ms
        } else {
          // when 60s reached
          this.clearTimer();
          // Use the same flow as manual submission so scoring/ending logic is consistent
          await this.submitGuess();
        }
      }, 100);
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
