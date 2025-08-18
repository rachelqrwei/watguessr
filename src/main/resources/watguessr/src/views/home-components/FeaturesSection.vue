<template>
  <section class="features-section" :class="{ loaded: isLoaded }">
    <div class="features-grid">
      <div class="feature-card">
        <div class="feature-card-content">
        <div class="feature-icon">
          <font-awesome-icon icon="star" />
        </div>
        <h4>Campus Focused</h4>
        <p>Explore every corner of the University of Waterloo campus!</p>
        </div>
      </div>

      <div class="feature-card">
        <div class="feature-card-content">
        <div class="feature-icon">
          <font-awesome-icon icon="clipboard" />
        </div>
        <h4>Real-Time Scoring</h4>
        <p>Track your progress with dynamic scoring and leaderboards!</p>
        </div>
      </div>

      <div class="feature-card">
        <div class="feature-card-content">
        <div class="feature-icon">
          <font-awesome-icon icon="users" />
        </div>
        <h4>Multiplayer Fun</h4>
        <p>Challenge friends and compete with other students!</p>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  props: {
    isLoaded: {
      type: Boolean,
      default: false
    }
  }
}
</script>

<style scoped>
.features-section {
  padding: 40px 28px;
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
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 2px;
  max-width: 920px;
  margin: 0 auto;
  justify-items: center;
}

.feature-card {
  text-align: center;
  padding: 26px 12px;
  border-radius: 16px;
  background: linear-gradient(to bottom, rgba(42, 42, 44, 0.2), rgba(42, 42, 44, 0.65));
  backdrop-filter: blur(8px);
  border: 1px solid rgba(100, 100, 100, 0.5);
  transition: all 0.3s ease;
  width: 90%;
  max-width: 280px;
}

.feature-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 90%;
  height: 100%;
  margin: 0 auto;
}

.feature-card:hover {
  transform: translateY(-5px);
  background: rgba(42, 42, 44, 0.5);
}

.feature-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.2) 0%, rgba(255, 203, 59, 0.1) 100%);
  color: var(--yellow);
}

.feature-card h4 {
  font-weight: 600;
  font-size: 1.1rem;
  color: var(--white);
  text-transform: uppercase;
  margin-bottom: 8px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 1.2px;
  line-height: 1.5;
}

.feature-card p {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.9rem;
  letter-spacing: 1.0px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-bottom: 14px;
}

@media (max-width: 768px) {
  .features-grid {
    grid-template-columns: 1fr;
    max-width: 100%;
  }
}
</style>
