<script setup>
import { ref, onMounted } from 'vue'
import HeroSection from '@/views/home-components/HeroSection.vue'
import GameModesSection from '@/views/home-components/GameModesSection.vue'
import FeaturesSection from '@/views/home-components/FeaturesSection.vue'
import LeaderboardSection from '@/views/home-components/LeaderboardSection.vue'
import TestimonialsSection from '@/views/home-components/TestimonialsSection.vue'

const isLoaded = ref(false)

onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)
})
</script>

<template>
  <div class="home-container">
    <HeroSection :isLoaded="isLoaded" />
    <GameModesSection :isLoaded="isLoaded" />
    <FeaturesSection :isLoaded="isLoaded" />
    <LeaderboardSection :isLoaded="isLoaded" />
    <TestimonialsSection :isLoaded="isLoaded" />
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: var(--dark-grey);
  padding-top: 100px;
}

@media (max-width: 768px) {
  .home-container {
    padding-top: 80px;
  }
}
</style>
