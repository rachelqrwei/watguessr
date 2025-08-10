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
    <div class="home-content-scale">
      <HeroSection :isLoaded="isLoaded" />
      <GameModesSection :isLoaded="isLoaded" />
      <FeaturesSection :isLoaded="isLoaded" />
      <LeaderboardSection :isLoaded="isLoaded" />
      <TestimonialsSection :isLoaded="isLoaded" />
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: url('/HomePage.png') center top / 100% auto no-repeat, var(--dark-grey);
  padding-top: 100px;
}

.home-content-scale {
  transform: none;
  transform-origin: top center;
}

@media (max-width: 768px) {
  .home-container {
    padding-top: 80px;
    background: url('/HomePage.png') center top / 100% auto no-repeat, var(--dark-grey);
  }

  .home-content-scale {
    transform: none;
  }
}
</style>
