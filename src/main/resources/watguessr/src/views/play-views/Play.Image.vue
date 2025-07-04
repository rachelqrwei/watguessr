<script setup lang="ts">
//TODO: fetch image from database
</script>

<template>
  <div>
    <image id='image'></image>
  </div>
</template>

<style scoped>
body { margin: 0; padding: 0; }

#image {
  position: absolute;
  top: 100px;
  left: 25px;
  width: calc(100vw - 50px);
  height: calc(100vh - 150px);
}
</style>
