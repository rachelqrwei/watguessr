<template>
  <div class="lobby-container">
    <div class="logo-container">
      <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
      <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
    </div>

    <div class="lobby-card">
      <h1 class="lobby-title">Lobby</h1>
      <p class="lobby-subtitle">Mode: {{ gameModeLabel }}</p>
      <button class="play-button" @click="goToPlay">PLAY</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const gameModeLabel = computed(() => {
  const mode = (route.query.gameMode ?? '').toString()
  return mode ? mode : 'singleplayer'
})

function goToPlay() {
  router.push({ name: 'play' })
}
</script>

<style scoped>
.lobby-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: var(--dark-grey);
  padding-top: 40px;
}

.logo-container {
  position: fixed;
  top: 18px;
  left: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  z-index: 10;
}

.logo-icon {
  color: var(--yellow);
  font-size: 20px;
}

.logo-text {
  color: var(--white);
  font-weight: 900;
  text-decoration: none;
}

.lobby-card {
  margin-top: 120px;
  width: 90%;
  max-width: 520px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.lobby-title {
  color: var(--white);
  font-size: 36px;
  margin: 0 0 8px;
}

.lobby-subtitle {
  color: var(--white);
  opacity: 0.85;
  margin: 0 0 20px;
}

.play-button {
  display: inline-block;
  padding: 12px 24px;
  border-radius: 8px;
  border: none;
  background: var(--yellow);
  color: #000;
  font-weight: 900;
  cursor: pointer;
}

.play-button:hover {
  filter: brightness(1.05);
}
</style> 