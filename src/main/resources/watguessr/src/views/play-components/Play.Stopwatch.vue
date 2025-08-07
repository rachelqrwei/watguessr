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

<script lang="ts">
export default {
  name: 'Stopwatch',

  props: {
    timeLeft: {
      type: Number,
      required: true,
    },
  },

  emits: ['time-up'],

  data() {
    return {
      internalTimeLeft: this.timeLeft,
      interval: null as number | null,
      totalTime: this.timeLeft,
    };
  },

  computed: {
    progressAngle(): number {
      const percent = 1 - this.internalTimeLeft / this.totalTime;
      return percent * 360;
    },

    formattedTimeLeft(): string {
      const ms = Math.floor((this.internalTimeLeft % 1000) / 10);
      const totalSeconds = Math.floor(this.internalTimeLeft / 1000);
      const s = Math.floor(totalSeconds % 60);
      const m = Math.floor(totalSeconds / 60);
      const pad = (n: number, z = 2) => String(n).padStart(z, '0');
      return `${pad(m)}:${pad(s)}.${pad(ms)}`;
    },
  },

  watch: {
    timeLeft(newVal: number, oldVal: number) {
      if (newVal !== oldVal) {
        this.startTimer();
      }
    },
  },

  mounted() {
    this.startTimer();
  },

  beforeUnmount() {
    this.clearTimer();
  },

  methods: {
    startTimer() {
      this.clearTimer();
      this.internalTimeLeft = this.timeLeft;

      this.interval = setInterval(() => {
        if (this.internalTimeLeft > 0) {
          this.internalTimeLeft -= 100;
        } else {
          this.clearTimer();
          this.$emit('time-up');
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
