<script setup lang="ts">
import {ref, onMounted, onUnmounted, computed} from 'vue'

// start from 60 seconds (or change this)
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
  <div class="stopwatch">
    {{ formattedTimeLeft }}
  </div>
</template>

<style scoped>
.stopwatch {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #333;
  color: #fff;
  padding: 12px 24px;
  font-size: 20px;
  font-weight: bold;
  border-radius: 30px;
  z-index: 9999;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
</style>
