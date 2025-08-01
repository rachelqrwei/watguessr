<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'

const props = defineProps<{
  timeLeft: number
}>()

const emit = defineEmits<{
  (e: 'time-up'): void
}>()


const internalTimeLeft = ref(props.timeLeft)
const totalTime = props.timeLeft
let interval: number | undefined

const startTimer = () => {
  clearInterval(interval)
  internalTimeLeft.value = props.timeLeft

  interval = setInterval(() => {
    if (internalTimeLeft.value > 0) {
      internalTimeLeft.value -= 100
    } else {
      clearInterval(interval)
      emit('time-up') // ✅ Emit time-up event to parent
    }
  }, 100)
}

onMounted(() => {
  startTimer()
})

onUnmounted(() => {
  clearInterval(interval)
})

watch(() => props.timeLeft, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    startTimer()
  }
})

const progressAngle = computed(() => {
  const percent = 1 - internalTimeLeft.value / totalTime
  return percent * 360
})

const formattedTimeLeft = computed(() => {
  const ms = Math.floor((internalTimeLeft.value % 1000) / 10)
  const totalSeconds = Math.floor(internalTimeLeft.value / 1000)
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
