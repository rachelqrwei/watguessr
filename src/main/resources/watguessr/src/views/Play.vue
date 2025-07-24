<script setup>
import { ref } from 'vue'
import PlayStopwatch from '@/views/play-components/Play.Stopwatch.vue'
import PlayMapView from '@/views/play-components/Play.Map.vue'
import PlayImageView from '@/views/play-components/Play.Image.vue'
import PlayScoreTracker from '@/views/play-components/Play.ScoreTracker.vue'

const currentView = ref('Map')

const changeView = (nextView) => {
  currentView.value = nextView
}
</script>

<template>
  <PlayStopwatch />

  <div v-if="currentView === 'Map'">
    <button class="view-change-button" @click="changeView('Image')">
      VIEW IMAGE
    </button>
    <PlayMapView />
  </div>

  <div v-if="currentView === 'Image'">
    <button class="view-change-button" @click="changeView('Map')">
      VIEW MAP
    </button>
    <PlayImageView />
  </div>

  <PlayScoreTracker />
</template>

<style scoped>
.view-change-button {
  position: absolute;
  top: 125px;
  left: 50px;
  z-index: 10;
  background: var(--dark-grey);
  padding: 20px;
  border: none;
  border-radius: 40px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  width: 200px;
  font-weight: bold;
  font-size: 18px;
  cursor: pointer;
  color: var(--white);
  transition: all 0.2s ease;
}

.view-change-button:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}
</style>
