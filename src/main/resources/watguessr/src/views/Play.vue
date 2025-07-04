<script setup>
import { onMounted, ref } from 'vue';
import PlayStopwatch from "@/views/play-components/Play.Stopwatch.vue";
import PlayMapView from '@/views/play-components/Play.Map.vue';
import PlayImageView from '@/views/play-components/Play.Image.vue';

const currentView = ref("Map");

function changeView(nextView) {
  currentView.value = nextView;
}
</script>
<template>
  <PlayStopwatch />

  <div v-if="currentView === 'Map'">
    <button class="view-change-button" @click="changeView('Image')">VIEW IMAGE</button>

    <PlayMapView />
  </div>
  <div v-if="currentView === 'Image'">
    <button class="view-change-button" @click="changeView('Map')">VIEW MAP</button>

    <PlayImageView />
  </div>

  <div class="player-score-tracker-container">
    <div class="player-score-tracker-1">
      <h1 class="player-name">Name</h1>
      <p>2000 pts</p>
      <div class="player-score-progress-bar" />
    </div>

    <div class="player-score-tracker-2">
      <h1 class="player-name">Name</h1>
      <p>2000 pts</p>
      <div class="player-score-progress-bar" />
    </div>
  </div>
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
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
  width: 200px;
  font-weight: bold;
  font-size: 18px;
  cursor: pointer;
}

.player-score-tracker-container {
  position: fixed;
  bottom: 0;          /* Aligns to bottom */
  left: 0;            /* Aligns to left edge */
  width: 100%;        /* Full width */
  color: white;
  padding: 16px;
  z-index: 9999;

  display: flex;
  justify-content: space-between; /* Push children to left & right */
  align-items: center;
}

.player-name {
  font-size: 20px;
  font-weight: bold;
}

.player-score-tracker-1 {
}

.player-score-tracker-2 {
  text-align: right;
}

.player-score-tracker-1,
.player-score-tracker-2 {
  max-width: 45%;
  background: #333;
  padding: 20px;
  min-width: 30%;
  font-size: 18px;
  border-radius: 30px;
}

.player-score-progress-bar {
  height: 8px;
  background-color: #aaa;
  margin-top: 8px;
  border-radius: 4px;
}
</style>
