<template>
  <section class="hero-section" :class="{ loaded: isLoaded }">
    <div class="hero-content">
      <div class="hero-image">
        <div class="image-placeholder">
          <div class="image-overlay">
            <font-awesome-icon icon="map-marker-alt" class="hero-icon" />
          </div>
        </div>
      </div>
      
      <div class="hero-text">
        <h1 class="main-title">
          <span class="title-wat">WATGUESSR</span><span class="title-io">.IO</span>
        </h1>
        <p class="subtitle">
          The hit game Geoguessr, built specifically for the University of Waterloo.
        </p>
        <div class="title-underline" />
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  isLoaded: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.hero-section {
  padding: 20px 40px 30px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.hero-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.hero-content {
  display: flex;
  align-items: center;
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
  margin-left: calc(50% - 455px);
}

.hero-image {
  flex: 1;
  max-width: 250px;
}

.image-placeholder {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.1) 0%, rgba(255, 203, 59, 0.05) 100%);
  border: 2px solid rgba(255, 203, 59, 0.3);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.image-placeholder::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 203, 59, 0.1) 50%, transparent 70%);
  animation: shimmer 2s infinite;
}

.image-overlay {
  color: var(--yellow);
  z-index: 1;
}

.hero-icon {
  width: 80px;
  height: 80px;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.hero-text {
  flex: 1;
}

.main-title {
  font-size: 3.2rem;
  font-weight: 900;
  margin-bottom: 10px;
  line-height: 1.1;
  letter-spacing: -1px;
  text-transform: uppercase;
}

.title-wat {
  color: var(--yellow);
  text-shadow: 
    0 0 20px rgba(255, 203, 59, 0.5),
    0 0 40px rgba(255, 203, 59, 0.3),
    0 2px 4px rgba(0, 0, 0, 0.5);
  filter: drop-shadow(0 0 10px rgba(255, 203, 59, 0.4));
}

.title-io {
  color: var(--light-grey);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  font-weight: 700;
}

.subtitle {
  font-size: 1.2rem;
  color: var(--light-grey);
  line-height: 1.6;
  margin-bottom: 15px;
  font-weight: 400;
  font-style: italic;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.title-underline {
  width: 100px;
  height: 4px;
  background: linear-gradient(90deg, var(--yellow) 0%, transparent 100%);
  border-radius: 2px;
}

@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
    gap: 30px;
    margin-left: auto;
  }
  
  .hero-image {
    max-width: 250px;
  }
  
  .image-placeholder {
    width: 200px;
    height: 200px;
  }
  
  .main-title {
    font-size: 2.5rem;
  }
  
  .subtitle {
    font-size: 1.1rem;
  }
}
</style> 