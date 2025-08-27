<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-overlay" @click="handleOverlayClick">
      <div class="welcome-modal-content" @click.stop>
        <button class="close-btn" @click="$emit('close')">×</button>
        
        <!-- Header -->
        <div class="welcome-header">
          <h1 class="welcome-title">Welcome to Watguessr!</h1>
          <p class="welcome-subtitle">Let's get you started with the basics</p>
        </div>

        <!-- Content Sections -->
        <div class="welcome-content">
          <!-- Keybinds Section -->
          <div class="section">
            <h2 class="section-title">
              <span class="section-icon">⌨️</span>
              Controls & Keybinds
            </h2>
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
                  <span class="key space-key">SPACE</span>
                </div>
                <div class="keybind-description">
                  Submit your guess
                </div>
              </div>
            </div>
          </div>

          <!-- Game Modes Section -->
          <div class="section">
            <h2 class="section-title">
              <span class="section-icon">🎮</span>
              Game Modes
            </h2>
            <div class="game-modes-grid">
              <div class="game-mode-card">
                <div class="mode-header">
                  <span class="mode-icon">👤</span>
                  <h3>Solo</h3>
                </div>
                <p>Practice on your own at your own pace. Perfect for learning the campus and improving your skills.</p>
              </div>
              
              <div class="game-mode-card">
                <div class="mode-header">
                  <span class="mode-icon">👥</span>
                  <h3>Multiplayer</h3>
                </div>
                <p>Play with friends in casual matches. Create or join lobbies for a fun, relaxed gaming experience.</p>
              </div>
              
              <div class="game-mode-card">
                <div class="mode-header">
                  <span class="mode-icon">🏆</span>
                  <h3>Ranked</h3>
                </div>
                <p>Compete seriously against other players. Climb the leaderboard and prove you're the ultimate Watguessr!</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer -->
        <div class="welcome-footer">
          <button class="get-started-btn" @click="$emit('close')">
            Get Started
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
  watch: {
    visible(newVal) {
      console.log('WelcomeModal visibility changed:', newVal);
    }
  },
  methods: {
    handleOverlayClick() {
      this.$emit('close');
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
  background: rgba(42, 42, 44, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  width: 70%;
  max-width: 900px;
  min-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  color: #fff;
  font-family: 'Segoe UI', sans-serif;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);
  position: relative;
  animation: slideIn 0.3s ease-out;
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

.close-btn {
  position: absolute;
  top: 20px;
  right: 25px;
  font-size: 24px;
  border: none;
  background: transparent;
  color: #ccc;
  cursor: pointer;
  z-index: 10;
  transition: color 0.2s ease;
}

.close-btn:hover {
  color: #fff;
}

.welcome-header {
  text-align: center;
  padding: 40px 40px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, var(--yellow), #ffd54f);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 1.1rem;
  color: #ccc;
  margin: 0;
}

.welcome-content {
  padding: 30px 40px;
}

.section {
  margin-bottom: 40px;
}

.section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.4rem;
  font-weight: 600;
  margin: 0 0 20px 0;
  color: #fff;
}

.section-icon {
  font-size: 1.2rem;
}

/* Keybinds Section */
.keybinds-grid {
  display: grid;
  gap: 16px;
}

.keybind-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.keybind-keys {
  display: flex;
  gap: 8px;
  min-width: 120px;
}

.key {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--yellow);
  text-align: center;
}

.space-key {
  min-width: 60px;
  font-size: 0.8rem;
}

.keybind-description {
  color: #e0e0e0;
  font-size: 1rem;
  line-height: 1.4;
}

/* Game Modes Section */
.game-modes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.game-mode-card {
  padding: 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.game-mode-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 235, 59, 0.3);
  transform: translateY(-2px);
}

.mode-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.mode-icon {
  font-size: 1.5rem;
}

.mode-header h3 {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  color: #fff;
}

.game-mode-card p {
  color: #ccc;
  line-height: 1.5;
  margin: 0;
  font-size: 0.95rem;
}

/* Footer */
.welcome-footer {
  padding: 30px 40px 40px;
  text-align: center;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.get-started-btn {
  padding: 16px 40px;
  background: linear-gradient(135deg, var(--yellow), #ffd54f);
  color: #000;
  border: none;
  font-weight: bold;
  font-size: 1.1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 20px rgba(255, 235, 59, 0.3);
}

.get-started-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 235, 59, 0.4);
  background: linear-gradient(135deg, #ffd54f, var(--yellow));
}

.get-started-btn:active {
  transform: translateY(0);
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
    margin: 10px;
  }
  
  .welcome-header {
    padding: 30px 20px 15px;
  }
  
  .welcome-title {
    font-size: 2rem;
  }
  
  .welcome-content {
    padding: 20px;
  }
  
  .welcome-footer {
    padding: 20px;
  }
  
  .game-modes-grid {
    grid-template-columns: 1fr;
  }
  
  .keybind-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .keybind-keys {
    min-width: unset;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 1.6rem;
  }
  
  .section-title {
    font-size: 1.2rem;
  }
}
</style>
