<script setup>
import { onMounted, ref } from 'vue';
import PlayMapView from './play-views/Play.Map.vue';
import PlayImageView from './play-views/Play.Image.vue';

const currentView = ref("Map");

function changeView(nextView) {
  currentView.value = nextView;
}
</script>
<template>
  <div v-if="currentView === 'Map'">
    <button class="view-change-button" @click="changeView('Image')">VIEW IMAGE</button>

    <PlayMapView />
  </div>
  <div v-if="currentView === 'Image'">
    <button class="view-change-button" @click="changeView('Map')">VIEW MAP</button>

    <PlayImageView />
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
</style>
