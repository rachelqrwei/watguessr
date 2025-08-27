<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
      <div class="welcome-modal-content" @click.stop>
        

        
        <!-- Navigation Arrows -->
        <button 
          v-if="currentSlide > 0" 
          class="nav-arrow nav-arrow-left" 
          @click="previousSlide"
        >
          <svg width="42" height="55" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="m15 18-6-6 6-6"/>
          </svg>
        </button>
        
        <button 
          v-if="currentSlide < totalSlides - 1" 
          class="nav-arrow nav-arrow-right" 
          @click="nextSlide"
        >
          <svg width="42" height="55" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="m9 18 6-6-6-6"/>
          </svg>
        </button>

        <!-- Carousel Container -->
        <div class="carousel-container">
          <div class="carousel-track" :style="{ transform: `translateX(-${currentSlide * 100}%)` }">
            
            <!-- Slide 1: Welcome -->
            <div class="carousel-slide">
              <div class="slide-header">
                <h1 class="slide-title">WELCOME TO WATGUESSR.IO!</h1>
              </div>
              <div class="slide-content-wrapper" :class="{ 'slide-1': currentSlide === 0, 'slide-2': currentSlide === 1 }">
                <div class="slide-content">
                  <div class="how-to-play-cards">
                    <div class="play-card">
                      <div class="card-image">
                        <img src="/welcomeImage.png" alt="Example game image" />
                      </div>
                      <h3>RECEIVE AN IMAGE</h3>
                      <p>You'll see a photo taken somewhere on the University of Waterloo campus</p>
                    </div>
                    <div class="play-card">
                      <div class="card-image">
                        <img src="/welcomeGuess.png" alt="Example guess on map" />
                      </div>
                      <h3>MAKE A GUESS!</h3>
                      <p>Click on the map where you think the photo was taken and submit your guess</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Slide 2: Keybinds -->
            <div class="carousel-slide">
              <div class="slide-header">
                <h1 class="slide-title">Controls & Keybinds</h1>
              </div>
              <div class="slide-content-wrapper" :class="{ 'slide-1': currentSlide === 0, 'slide-2': currentSlide === 1 }">
                <div class="slide-content">
                  <div class="keybinds-grid">
                    <div class="keybind-item">
                      <div class="keybind-keys">
                        <span class="key">A</span>
                        <span class="key">D</span>
                      </div>
                      <div class="keybind-description">
                        Switch between map and picture view
                      </div>
                    </div>
                    <div class="keybind-item">
                      <div class="keybind-keys">
                        <span class="key">W</span>
                        <span class="key">S</span>
                      </div>
                      <div class="keybind-description">
                        Switch between floors
                      </div>
                    </div>
                    <div class="keybind-item">
                      <div class="keybind-keys">
                        <span class="key space-key">SPACE</span>
                      </div>
                      <div class="keybind-description">
                        Submit your guess
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>



          </div>
        </div>

        <!-- Progress Dots -->
        <div class="progress-dots" v-if="currentSlide !== totalSlides - 1">
          <button 
            v-for="(slide, index) in totalSlides" 
            :key="index"
            class="progress-dot"
            :class="{ active: index === currentSlide }"
            @click="goToSlide(index)"
          ></button>
        </div>

        <!-- Footer - only show "Let's Go!" on last slide -->
        <div class="welcome-footer" v-if="currentSlide === totalSlides - 1">
          <button class="get-started-btn" @click="$emit('close')">
            Let's Go!
          </button>
        </div>

      </div>
    </div>
  </Transition>
</template>

<script>
export default {
  name: 'WelcomeModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close'],
  data() {
    return {
      currentSlide: 0,
      totalSlides: 2
    };
  },
  watch: {
    visible(newVal) {
      console.log('WelcomeModal visibility changed:', newVal);
      // Reset to first slide when modal is opened
      if (newVal) {
        this.currentSlide = 0;
      }
    }
  },
  methods: {
    handleOverlayClick() {
      this.$emit('close');
    },
    nextSlide() {
      if (this.currentSlide < this.totalSlides - 1) {
        this.currentSlide++;
      }
    },
    previousSlide() {
      if (this.currentSlide > 0) {
        this.currentSlide--;
      }
    },
    goToSlide(index) {
      this.currentSlide = index;
    }
  },
  mounted() {
    // Add keyboard navigation
    this.handleKeydown = (event) => {
      if (!this.visible) return;
      
      if (event.key === 'ArrowLeft') {
        this.previousSlide();
      } else if (event.key === 'ArrowRight') {
        this.nextSlide();
      } else if (event.key === 'Escape') {
        this.$emit('close');
      }
    };
    
    document.addEventListener('keydown', this.handleKeydown);
  },
  
  beforeUnmount() {
    // Clean up event listener
    if (this.handleKeydown) {
      document.removeEventListener('keydown', this.handleKeydown);
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 8000;
  padding: 20px;
}

.welcome-modal-content {
  background: rgba(42, 42, 44, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  border-radius: 18px;
  width: 80%;
  max-width: 1000px;
  min-width: 600px;
  height: 600px;
  color: var(--white);
  font-family: inherit;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);
  position: relative;
  animation: slideIn 0.3s ease-out;
  overflow: hidden;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}



/* Navigation Arrows */
.nav-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
  padding: 12px;
}

.nav-arrow:hover {
  color: rgba(255, 255, 255, 1);
  transform: translateY(-50%) scale(1.1);
}

.nav-arrow-left {
  left: -5px;
}

.nav-arrow-right {
  right: -5px;
}

/* Carousel */
.carousel-container {
  height: 100%;
  overflow: hidden;
  position: relative;
}

.carousel-track {
  display: flex;
  height: 100%;
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.carousel-slide {
  min-width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 32px;
  box-sizing: border-box;
}

/* Slide Headers */
.slide-header {
  text-align: center;
  margin-bottom: 8px;
}

.slide-title {
  font-size: 1.8rem;
  font-weight: 900;
  margin: 0 0 10px 0;
  color: var(--white);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
  text-transform: uppercase;
}

.slide-subtitle {
  font-size: 1rem;
  color: var(--light-grey);
  margin: 0;
  font-family: "Red Hat Text", sans-serif;
  font-weight: 400;
  line-height: 1.5;
}

/* Slide Content Wrapper - lighter grey section like Lobby.vue */
.slide-content-wrapper {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 16px 24px 20px;
  margin-left: 24px;
  margin-right: 24px;
  margin-bottom: 70px;
}

.slide-content-wrapper.slide-1 {
  margin-bottom: 35px;
}

.slide-content-wrapper.slide-2 {
  margin-bottom: 70px;
}

/* Slide Content */
.slide-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* How to Play Cards */
.how-to-play-cards {
  display: flex;
  gap: 32px;
  width: 100%;
  max-width: 1000px;
}

.play-card {
  flex: 1;
  text-align: center;
  padding: 24px 20px;
  background: transparent;
  border: none;
  transition: transform 0.2s ease;
  min-width: 320px;
}

.play-card:hover {
  transform: translateY(-2px);
}

.card-image {
  width: 100%;
  height: 220px;
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.play-card:hover .card-image img {
  transform: scale(1.05);
}

.play-card h3 {
  font-size: 1rem;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.play-card p {
  color: var(--light-grey);
  line-height: 1.5;
  margin: 0;
  font-size: 0.9rem;
  font-family: "Red Hat Text", sans-serif;
  font-weight: 400;
}

/* Keybinds Section */
.keybinds-grid {
  display: grid;
  gap: 32px;
  width: 100%;
  max-width: 800px;
}

.keybind-item {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.keybind-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 235, 59, 0.3);
}

.keybind-keys {
  display: flex;
  gap: 12px;
  min-width: 160px;
}

.key {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--white);
  font-weight: 700;
  font-size: 16px;
  text-transform: uppercase;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
}

.key:hover {
  background: rgba(255, 227, 127, 0.15);
  border-color: var(--yellow);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.space-key {
  min-width: 100px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--white);
  font-weight: 700;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
  padding: 0 16px;
}

.space-key:hover {
  background: rgba(255, 227, 127, 0.15);
  border-color: var(--yellow);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.keybind-description {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.9rem;
  letter-spacing: 1.0px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}



/* Progress Dots */
.progress-dots {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  z-index: 10;
}

.progress-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  transition: all 0.2s ease;
}

.progress-dot.active {
  background: var(--yellow);
  transform: scale(1.2);
}

.progress-dot:hover {
  background: rgba(255, 255, 255, 0.5);
}

/* Footer */
.welcome-footer {
  position: absolute;
  bottom:28px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.get-started-btn {
  background: rgba(255, 235, 59, 0.15);
  color: var(--yellow);
  padding: 14px 28px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: 0.5px solid var(--yellow);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4px 15px rgba(255, 235, 59, 0.1);
  backdrop-filter: blur(8px);
  position: relative;
  overflow: hidden;
}

.get-started-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.get-started-btn:hover {
  background: rgba(255, 235, 59, 0.2);
  border-color: rgba(255, 235, 59, 0.8);
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 235, 59, 0.2);
}

.get-started-btn:hover::before {
  left: 100%;
}

.get-started-btn:active {
  transform: translateY(-1px);
}

/* Modal fade transition */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .welcome-modal-content {
    width: 95%;
    min-width: unset;
    height: 80vh;
    margin: 10px;
  }
  
  .carousel-slide {
    padding: 20px;
  }
  
  .slide-content-wrapper {
    padding: 12px 32px 20px;
  }
  
  .slide-title {
    font-size: 2rem;
  }
  
  .how-to-play-cards {
    flex-direction: column;
    gap: 24px;
  }
  
  .game-modes-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .keybind-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .keybind-keys {
    min-width: unset;
  }
  
  .nav-arrow-left {
    left: 6px;
  }
  
  .nav-arrow-right {
    right: 6px;
  }
}

@media (max-width: 480px) {
  .welcome-modal-content {
    width: 98%;
    height: 85vh;
  }
  
  .carousel-slide {
    padding: 15px;
  }
  
  .slide-content-wrapper {
    padding: 10px 24px 16px;
  }
  
  .slide-title {
    font-size: 1.6rem;
  }
  
  .card-image {
    height: 150px;
  }
  
  .play-card {
    padding: 30px 20px;
  }
  
  .nav-arrow-left {
    left: 4px;
  }
  
  .nav-arrow-right {
    right: 4px;
  }
}
</style>
