<script setup lang="ts">
import {ref, onMounted, onUnmounted, computed} from 'vue'

// start from 60 seconds (or change this)
const totalTime = 60_000;
const timeLeft = ref(60_000)
let interval: number | undefined

onMounted(() => {
  interval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value -= 100;
    } else {
      clearInterval(interval);
    }
  }, 100)
})

onUnmounted(() => {
  clearInterval(interval)
});

const progressAngle = computed(() => {
  const percent = 1 - timeLeft.value / totalTime;
  return percent * 360;
});

const formattedTimeLeft = computed(() => {
  const ms = Math.floor((timeLeft.value % 1000) / 10);
  const totalSeconds = Math.floor(timeLeft.value / 1000);
  const s = Math.floor((totalSeconds) % 60);
  const m = Math.floor(totalSeconds / 60);

  const pad = (n: number, z = 2) => String(n).padStart(z, '0');
  return `${pad(m)}:${pad(s)}.${pad(ms)}`;
})
</script>
<template>
  <div class="stopwatch-container"
       :style="{
          background: `conic-gradient(#FFCB3B ${progressAngle}deg, #2b2b2b ${progressAngle}deg)`
        }"
  />
  <div class="stopwatch"
  >
    <div>
      {{ formattedTimeLeft }}
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oxanium:wght@200..800&display=swap');

.stopwatch {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  background-color: var(--color-gray-700);
  color: white;
  padding: 20px;
  font-size: 30px;
  width: 200px;
  line-height: 40px;
  text-align: center;
  font-weight: bolder;
  font-family: "Oxanium", sans-serif;
  border-radius: 25px;
  z-index: 999;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.stopwatch-container {
  position: fixed;
  top: 25px;
  left: 50%;
  transform: translateX(-50%);
  width: 210px;
  border-radius: 30px;
  height: 90px; /* Or however tall you want */
  background-color: var(--dark-grey);
  z-index: 998; /* Below the stopwatch box */
}
</style>
