<script>
import { mapGetters } from 'vuex'

export default {
  emits: ['image-loaded', 'image-error'],
  computed: {
    ...mapGetters({
      imageUrl: 'round/getImageUrl'
    })
  },
  methods: {
    handleImageLoad() {
      this.$emit('image-loaded')
    },
    handleImageError() {
      this.$emit('image-error')
    }
  }
}
</script>

<template>
  <div class="image-root">
    <img 
      v-if="imageUrl" 
      :src="imageUrl" 
      alt="round scene" 
      id="image" 
      @load="handleImageLoad" 
      @error="handleImageError"
    />
    <div v-else id='image'></div>
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
  object-fit: cover;
}
</style>
