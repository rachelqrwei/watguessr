<template>
  <div class="notfound-background" aria-hidden="true"></div>
  <section class="notfound-page">
    <div class="content">
      <div class="error-container">
        <h1 class="title">404</h1>
      </div>
      <div class="text-content">
        <p class="subtitle">Page Not Found</p>
        <p class="description">Oops! The page you're looking for seems to have wandered off campus.</p>
        <div class="actions">
          <button class="back-button secondary" @click="goBack">
            <span class="link-icon">←</span>
            Go Back
          </button>
        </div>
      </div>
    </div>
    <div class="goose-decoration">
      <img src="/Goose1.png" alt="" class="goose-image" />
    </div>
  </section>
</template>

<script>
export default {
  name: 'NotFound',
  mounted() {
    // Store original overflow value
    this.originalOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
  },
  beforeUnmount() {
    // Restore original overflow
    document.body.style.overflow = this.originalOverflow || '';
  },
  beforeRouteLeave(to, from, next) {
    // Ensure overflow is restored when navigating away
    document.body.style.overflow = this.originalOverflow || '';
    next();
  },
  methods: {
    goBack() {
      // Restore overflow before navigation
      document.body.style.overflow = this.originalOverflow || '';
      if (window.history.length > 1) {
        this.$router.go(-1);
      } else {
        this.$router.push({ name: 'home' });
      }
    }
  }
}
</script>

<style scoped>
.notfound-background {
  position: fixed;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 203, 59, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 203, 59, 0.05) 0%, transparent 50%),
    var(--dark-grey);
  z-index: -1;
}



.notfound-page {
  height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 120px 20px 40px;
  position: relative;
  overflow: hidden;
}

.content {
  width: 100%;
  max-width: 800px;
  margin: 0 0 0 -200px;
  text-align: left;
}

.error-container {
  position: relative;
  margin-bottom: 40px;
}

.title {
  font-size: 8rem;
  font-weight: 900;
  color: var(--yellow);
  margin: 0;
  line-height: 0.9;
  letter-spacing: -6px;
  text-transform: uppercase;
  position: relative;
  z-index: 2;
  text-shadow:
    0 0 8px rgba(255, 203, 59, 0.3),
    0 0 15px rgba(255, 203, 59, 0.2),
    0 2px 4px rgba(0, 0, 0, 0.6);
  filter: drop-shadow(0 0 4px rgba(255, 203, 59, 0.2));
}



.text-content {
  max-width: 600px;
  margin: 0;
}

.subtitle {
  font-size: 1.8rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 10px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.description {
  font-family: "Red Hat Text", sans-serif;
  font-style: italic;
  font-weight: 400;
  font-size: 1.08rem;
  letter-spacing: 1.0px;
  color: #d9d9d9;
  line-height: 1.6;
  margin: 0 0 40px 0;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.actions {
  display: flex;
  gap: 16px;
  justify-content: flex-start;
  flex-wrap: wrap;
}

.home-link,
.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  border-radius: 12px;
  font-weight: 600;
  font-size: 1rem;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  border: none;
  font-family: inherit;
  position: relative;
  overflow: hidden;
}

.home-link.primary {
  background: linear-gradient(135deg, var(--yellow) 0%, #e6b800 100%);
  color: var(--dark-grey);
  box-shadow: 
    0 4px 15px rgba(255, 203, 59, 0.4),
    0 2px 4px rgba(0, 0, 0, 0.2);
}

.home-link.primary:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 6px 20px rgba(255, 203, 59, 0.6),
    0 4px 8px rgba(0, 0, 0, 0.3);
}

.back-button.secondary {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  font-weight: bold !important;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.back-button.secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.link-icon {
  font-size: 1.1rem;
  display: flex;
  align-items: center;
}

@media (max-width: 768px) {
  .title {
    font-size: 7.5rem;
    letter-spacing: -4px;
  }

  .subtitle {
    font-size: 1.4rem;
  }

  .description {
    font-size: 1rem;
  }

  .actions {
    flex-direction: column;
    align-items: center;
  }

  .home-link,
  .back-button {
    width: 100%;
    max-width: 280px;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .title {
    font-size: 5.5rem;
  }
  
  .notfound-page {
    padding: 20px;
  }
}

.goose-decoration {
  position: fixed;
  bottom: 150px;
  right: 0;
  z-index: 10;
  pointer-events: none;
}

.goose-image {
  width: 650px;
  height: auto;
  opacity: 0.8;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

@media (max-width: 768px) {
  .goose-image {
    width: 400px;
  }
}

@media (max-width: 480px) {
  .goose-image {
    width: 300px;
  }
  
  .goose-decoration {
    bottom: 60px;
    right: 0;
  }
}
</style>


