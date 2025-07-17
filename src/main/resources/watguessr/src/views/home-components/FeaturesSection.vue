<template>
  <section class="features-section" :class="{ loaded: isLoaded }">
    <div class="features-grid">
      <div class="feature-card">
        <div class="feature-icon">
          <font-awesome-icon icon="star" />
        </div>
        <h4>Campus Focused</h4>
        <p>Explore every corner of the University of Waterloo campus</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">
          <font-awesome-icon icon="clipboard" />
        </div>
        <h4>Real-Time Scoring</h4>
        <p>Track your progress with dynamic scoring and leaderboards</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">
          <font-awesome-icon icon="users" />
        </div>
        <h4>Multiplayer Fun</h4>
        <p>Challenge friends and compete with other students</p>
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
.features-section {
  padding: 80px 40px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 0.4s;
}

.features-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 40px;
  max-width: 1000px;
  margin: 0 auto;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.08);
}

.feature-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.2) 0%, rgba(255, 203, 59, 0.1) 100%);
  color: var(--yellow);
}

.feature-card h4 {
  font-size: 1.4rem;
  font-weight: 800;
  color: var(--white);
  margin-bottom: 12px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: -0.3px;
}

.feature-card p {
  color: var(--light-grey);
  line-height: 1.6;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style> 