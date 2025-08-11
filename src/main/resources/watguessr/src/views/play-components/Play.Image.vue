<script setup lang="ts">
//TODO: fetch image from database
</script>

<template>
  <div class="image-root">
    <div id='image'></div>
  </div>
</template>

<style scoped>
body { margin: 0; padding: 0; }

.image-root {
  position: relative;
  width: 100%;
  height: 100%;
}

#image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}
</style>
