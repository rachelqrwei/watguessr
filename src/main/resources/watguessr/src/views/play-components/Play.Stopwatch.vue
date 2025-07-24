<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

const totalTime = 60_000
const timeLeft = ref(60_000)
let interval: number | undefined

onMounted(() => {
  interval = setInterval(() => {
    if (timeLeft.value > 0) {
      timeLeft.value -= 100
    } else {
      clearInterval(interval)
    }
  }, 100)
})

onUnmounted(() => {
  clearInterval(interval)
})

const progressAngle = computed(() => {
  const percent = 1 - timeLeft.value / totalTime
  return percent * 360
})

const formattedTimeLeft = computed(() => {
  const ms = Math.floor((timeLeft.value % 1000) / 10)
  const totalSeconds = Math.floor(timeLeft.value / 1000)
  const s = Math.floor(totalSeconds % 60)
  const m = Math.floor(totalSeconds / 60)

  const pad = (n: number, z = 2) => String(n).padStart(z, '0')
  return `${pad(m)}:${pad(s)}.${pad(ms)}`
})
</script>

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

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Oxanium:wght@200..800&display=swap');

.stopwatch {
  position: fixed;
  top: 30px;
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
