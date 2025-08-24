<script>
import { mapGetters } from 'vuex'

export default {
  emits: ['image-loaded', 'image-error'],
  computed: {
    ...mapGetters({
      imageUrl: 'round/getImageUrl'
    })
  },
  data() {
    return {
      scale: 1,
      translateX: 0,
      translateY: 0,
      isDragging: false,
      startX: 0,
      startY: 0
    }
  },
  methods: {
    handleImageLoad() {
      this.$emit('image-loaded')
    },
    handleImageError() {
      this.$emit('image-error')
    },
    handleWheel(e) {
      e.preventDefault()
      const delta = e.deltaY > 0 ? -0.1 : 0.1
      const newScale = Math.min(Math.max(this.scale + delta, 1), 4) // zoom 1x–4x
      
      // Get the image element and its bounding rectangle
      const imageElement = e.currentTarget.querySelector('#image')
      if (!imageElement) return
      
      const rect = imageElement.getBoundingClientRect()
      
      // Calculate mouse position relative to the image
      const mouseX = e.clientX - rect.left
      const mouseY = e.clientY - rect.top
      
      // Calculate the center of the image
      const imageCenterX = rect.width / 2
      const imageCenterY = rect.height / 2
      
      // Calculate the offset from center
      const offsetX = mouseX - imageCenterX
      const offsetY = mouseY - imageCenterY
      
      // Calculate the scale factor change
      const scaleFactor = newScale / this.scale
      
      // Update translation to keep the mouse position fixed
      this.translateX += offsetX * (1 - scaleFactor)
      this.translateY += offsetY * (1 - scaleFactor)
      
      // Update scale
      this.scale = newScale
    },
    startDrag(e) {
      if (this.scale === 1) return // no dragging if not zoomed in
      this.isDragging = true
      this.startX = e.clientX - this.translateX
      this.startY = e.clientY - this.translateY
    },
    onDrag(e) {
      if (!this.isDragging) return
      this.translateX = e.clientX - this.startX
      this.translateY = e.clientY - this.startY
    },
    endDrag() {
      this.isDragging = false
    },
    handleClick() {
      if (this.scale > 1) {
        // Reset to zoomed-out
        this.scale = 1
        this.translateX = 0
        this.translateY = 0
      }
    }
  }
}
</script>

<template>
  <div
    class="image-root"
    @wheel="handleWheel"
    @mousedown="startDrag"
    @mousemove="onDrag"
    @mouseup="endDrag"
    @mouseleave="endDrag"
    @click="handleClick"
  >
    <img
      v-if="imageUrl"
      :src="imageUrl"
      alt="round scene"
      id="image"
      :style="{
        transform: `translate(${translateX}px, ${translateY}px) scale(${scale})`
      }"
      @load="handleImageLoad"
      @error="handleImageError"
      draggable="false"
    />
    <div v-else id="image"></div>
  </div>
</template>

<style scoped>
body { margin: 0; padding: 0; }

.image-root {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  cursor: grab;
}

.image-root:active {
  cursor: grabbing;
}

#image {
  position: absolute;
  inset: 0;
  width: 100%;
  object-fit: contain;
  transition: transform 0.2s ease; /* smooth zoom reset */
}
</style>
