<template>
  <section class="hero-section" :class="{ loaded: isLoaded }">
    <div class="hero-content">
      <div class="hero-image">
        <div class="logo-spacer" aria-hidden="true"></div>
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
.hero-section {
  padding: 18px 50px 72px;
  margin-top: -30px;
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
  gap: 23px;
  max-width: 960px;
  margin: 0 auto;
}

.hero-image {
  flex: 1;
  max-width: 225px;
}

.logo-spacer {
  width: 180px;
  height: 180px;
}

.hero-text {
  flex: 1;
}

.main-title {
  font-size: 2.88rem;
  font-weight: 900;
  margin-bottom: 9px;
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
  color: #d6d6d6;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  font-weight: 700;
}

.subtitle {
  font-family: "Red Hat Text", sans-serif;
  font-style: italic;
  font-weight: 400;
  font-size: 1.08rem;
  letter-spacing: 1.0px;
  color: #d9d9d9;
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-bottom: 14px;
}

.title-underline {
  width: 90px;
  height: 3px;
  background: linear-gradient(90deg, var(--yellow) 0%, transparent 100%);
  border-radius: 2px;
}

@media (max-width: 768px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
    gap: 27px;
  }

  .hero-image {
    max-width: 225px;
  }

  .logo-spacer {
    width: 180px;
    height: 180px;
  }

  .main-title {
    font-size: 2.25rem;
  }

  .subtitle {
    font-size: 1rem;
  }

  .hero-section {
    margin-top: -27px;
  }
}
</style>
