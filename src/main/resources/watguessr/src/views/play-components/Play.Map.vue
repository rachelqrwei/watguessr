<template>
  <div>
    <div class="map-placeholder">
      Map placeholder (Mapbox disabled for testing)
    </div>
  </div>
</template>
<script setup>
// Mapbox code disabled for testing
</script>
<style scoped>
body { margin: 0; padding: 0; }

.map-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #222;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: -1;
}

#map {
  position: absolute;
  top: 100px;
  left: 25px;
  width: calc(100vw - 50px);
  height: calc(100vh - 150px);
}
</style>
