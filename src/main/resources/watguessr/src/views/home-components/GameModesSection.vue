<template>
  <!-- Error Message -->
  <div v-if="showError" class="error-overlay" @click="hideError">
    <div class="error-modal" @click.stop>
      <div class="error-icon">⚠️</div>
      <h3>Login Required</h3>
      <p>Log in to enter game</p>
      <button class="error-close-btn" @click="hideError">OK</button>
    </div>
  </div>

  <section class="play-section" :class="{ loaded: isLoaded }">
    <div class="play-content">
      <div class="game-modes-wrapper">
        <div class="section-header">
          <h2>PLAY WATGUESSR</h2>
        </div>

        <div class="game-modes">
          <div class="trapezoid-pair">
            <div class="player-1-trapezoid" @click="goSolo">
              <div class="play-option-container">
                <h3>SOLO</h3>
                <p>PLAY BY YOURSELF UNTIL YOUR POINTS RUN OUT!</p>
              </div>
            </div>

            <div class="player-2-trapezoid" @click="openLobbyBrowser">
              <div class="play-option-container">
                <h3>PARTY</h3>
                <p>JOIN OR CREATE LOBBIES TO PLAY WITH FRIENDS!</p>
              </div>
            </div>
          </div>

          <div class="ranked-button" @click="goRanked">
            <div class="ranked-content">
              <div class="ranked-progress">
                <img src="/Ranked.png" alt="Ranked Progress" class="ranked-image" />
              </div>
              <div class="ranked-text">
                <h3>Rank Up</h3>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
import { mapGetters } from 'vuex';

export default {
  props: {
    isLoaded: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      showError: false
    };
  },
  computed: {
    ...mapGetters('user', ['isAuthenticated'])
  },
  methods: {
    goSolo() {
      if (!this.isAuthenticated) {
        this.showError = true;
        return;
      }
      this.$router.push({ name: 'lobby', query: { gameMode: 'singleplayer' } });
    },
    goRanked() {
      if (!this.isAuthenticated) {
        this.showError = true;
        return;
      }
      this.$router.push({ name: 'lobby', query: { gameMode: 'ranked' } });
    },
    openLobbyBrowser() {
      if (!this.isAuthenticated) {
        this.showError = true;
        return;
      }
      this.$emit('open-lobby-browser');
    },
    hideError() {
      this.showError = false;
    }
  }
};
</script>

<style scoped>
.play-section {
  padding: 18px 36px 36px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 0.2s;
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
  overflow-x: hidden;
}

.play-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.play-content {
  max-width: 960px;
  margin: 0 auto;
}

.game-modes-wrapper {
  width: fit-content;
  margin: 0 auto;
  max-width: 100%;
  overflow: hidden;
}

.section-header {
  text-align: left;
  margin-bottom: 6px;
}

.section-header h2 {
  font-size: 0.81rem;
  font-weight: 600;
  color: var(--white);
  margin-bottom: 7px;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.game-modes {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 9px;
  max-width: 100%;
  flex-wrap: wrap;
}

.trapezoid-pair {
  position: relative;
  width: 576px; /* scaled from 640 */
  height: 162px; /* scaled from 180 */
}

.player-1-trapezoid,
.player-2-trapezoid {
  position: absolute;
  top: 0;
  width: 324px; /* scaled from 360 */
  height: 162px; /* scaled from 180 */
  border-radius: 10px 5px 20px 10px;
  padding: 27px; /* scaled from 30 */
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
}

.player-1-trapezoid {
  left: 0;
  z-index: 2;
  clip-path: polygon(0 0, 100% 0, 75% 100%, 0% 100%);
}

.player-2-trapezoid {
  left: 252px; /* scaled from 280 */
  z-index: 1;
  padding-left: 90px; /* scaled from 100 */
  clip-path: polygon(25% 0%, 100% 0%, 100% 100%, 0% 100%);
}

.player-1-trapezoid::before,
.player-2-trapezoid::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--player-1-gradient);
  opacity: 0.75;
  z-index: -1;
  transition: opacity 0.3s ease;
}

.player-1-trapezoid::before {
  background-image: var(--player-1-gradient);
}

.player-2-trapezoid::before {
  background-image: var(--player-2-gradient);
}

.player-1-trapezoid:hover,
.player-2-trapezoid:hover {
  transform: translateY(-3px);

}

.player-1-trapezoid:hover::before,
.player-2-trapezoid:hover::before {
  opacity: 1;
}

.play-option-container {
  width: 80%;
  font-size: 14px;
  position: relative;
  z-index: 1;
}

.play-option-container h3 {
  font-weight: 900;
  font-size: 38px;
  color: var(--white);
  margin-bottom: 10px;
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(255, 255, 255, 0.2);
  letter-spacing: -1px;
  text-transform: uppercase;
}

.play-option-container p {
  font-size: 12px;
  font-weight: 700;
  color: var(--white);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  line-height: 1.3;
  letter-spacing: 0.5px;
  margin-bottom: 10px;
}

.ranked-button {
  position: relative;
  width: 225px; /* scaled down from 250 */
  height: 162px; /* scaled down from 180 */
  border-radius: 18px;
  padding: 0;
  overflow: hidden;
  z-index: 0;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  background: rgba(74, 74, 76, 0.65);
  border: none;
  backdrop-filter: blur(8px);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.12), 0 4px 20px rgba(0, 0, 0, 0.3);
  margin-left: 0;
}

.ranked-button::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.1);
  opacity: 0;
  z-index: -1;
  transition: opacity 0.3s ease;
  border-radius: inherit;
}

.ranked-button:hover {
  transform: translateY(-5px);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.12), 0 10px 30px rgba(0, 0, 0, 0.4);

}

.ranked-button:hover::before {
  opacity: 1;
}

.ranked-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 18px 0 0;
  gap: 0;
  z-index: 1;
}

.ranked-progress {
  flex-shrink: 0;
  width: 126px;
  height: 162px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0;
  margin: 0;
  border: none;
}

.ranked-image {
  width: 80%;
  height: 80%;
  object-fit: contain;
  margin-left: -0.8vw;
}

.ranked-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: center;
  gap: 0;
  padding: 0 18px 0 0;
  transform: translateY(-16px);
}

.ranked-text h3 {
  font-weight: 900;
  font-size: 28.8px;
  color: var(--white);
  text-shadow:
    0 2px 4px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(255, 255, 255, 0.2);
  letter-spacing: -0.5px;
  text-transform: uppercase;
  margin: 0;
  line-height: 1.1;
  text-align: right;
}

/* Error Modal Styles */
.error-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  animation: fadeIn 0.3s ease-out;
}

.error-modal {
  background: rgba(42, 42, 44, 0.7);
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  position: relative;
  overflow: hidden;
  animation: slideIn 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  font-family: 'Segoe UI', sans-serif;
}

.error-icon {
  font-size: 48px;
  margin-bottom: 15px;
  color: var(--yellow);
  text-shadow: 0 0 30px rgba(255, 215, 0, 0.5);
  animation: iconBounce 0.6s ease-out 0.2s both;
}

.error-modal h3 {
  color: var(--white);
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease-out 0.3s both;
}

.error-modal p {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.9rem;
  letter-spacing: 1.0px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-bottom: 24px;
  animation: slideUp 0.5s ease-out 0.4s both;
}

.error-close-btn {
  background: rgba(255, 255, 255, 0.08);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  padding: 10px 28px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  backdrop-filter: blur(8px);
  animation: slideUp 0.5s ease-out 0.5s both;
}

.error-close-btn:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.error-close-btn:active {
  transform: translateY(0);
}

/* Animation Keyframes */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes iconBounce {
  0% {
    opacity: 0;
    transform: scale(0.3) rotate(-180deg);
  }
  50% {
    transform: scale(1.1) rotate(-90deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) rotate(0deg);
  }
}

/* Responsive styling for smaller laptop screens */
@media (max-width: 1200px) {
  .play-section {
    padding: 18px 24px 36px;
  }

  .trapezoid-pair {
    width: 520px;
    height: 150px;
  }

  .player-1-trapezoid,
  .player-2-trapezoid {
    width: 300px;
    height: 150px;
    padding: 24px;
  }

  .player-2-trapezoid {
    left: 250px;
    padding-left: 50px;
  }

  .ranked-button {
    width: 200px;
    height: 150px;
  }

  .ranked-progress {
    width: 110px;
    height: 150px;
  }

  .ranked-text h3 {
    font-size: 26px;
  }
}

@media (max-width: 1024px) {
  .play-section {
    padding: 18px 20px 36px;
  }

  .trapezoid-pair {
    width: 480px;
    height: 140px;
  }

  .player-1-trapezoid,
  .player-2-trapezoid {
    width: 280px;
    height: 140px;
    padding: 20px;
  }

  .player-2-trapezoid {
    left: 200px;
    padding-left: 70px;
  }

  .ranked-button {
    width: 180px;
    height: 140px;
  }

  .ranked-progress {
    width: 100px;
    height: 140px;
  }

  .ranked-text h3 {
    font-size: 24px;
  }

  .play-option-container h3 {
    font-size: 34px;
  }

  .play-option-container p {
    font-size: 11px;
  }
}

@media (max-width: 900px) {
  .play-section {
    padding: 18px 16px 36px;
  }

  .trapezoid-pair {
    width: 440px;
    height: 130px;
  }

  .player-1-trapezoid,
  .player-2-trapezoid {
    width: 260px;
    height: 130px;
    padding: 18px;
  }

  .player-2-trapezoid {
    left: 260px;
    padding-left: 65px;
  }

  .ranked-button {
    width: 160px;
    height: 130px;
  }

  .ranked-progress {
    width: 90px;
    height: 130px;
  }

  .ranked-text h3 {
    font-size: 22px;
  }

  .play-option-container h3 {
    font-size: 30px;
  }

  .play-option-container p {
    font-size: 10px;
  }
}

@media (max-width: 800px) {
  .play-section {
    padding: 18px 12px 36px;
  }

  .trapezoid-pair {
    width: 400px;
    height: 120px;
  }

  .player-1-trapezoid,
  .player-2-trapezoid {
    width: 240px;
    height: 120px;
    padding: 16px;
  }

  .player-2-trapezoid {
    left: 240px;
    padding-left: 60px;
  }

  .ranked-button {
    width: 140px;
    height: 120px;
  }

  .ranked-progress {
    width: 80px;
    height: 120px;
  }

  .ranked-text h3 {
    font-size: 20px;
  }

  .play-option-container h3 {
    font-size: 26px;
  }

  .play-option-container p {
    font-size: 9px;
  }
}

/* Mobile responsive for error modal */
@media (max-width: 768px) {
  .error-modal {
    padding: 24px 20px;
    margin: 20px;
    border-radius: 8px;
  }

  .error-icon {
    font-size: 40px;
    margin-bottom: 16px;
  }

  .error-modal h3 {
    font-size: 20px;
    letter-spacing: 1px;
  }

  .error-modal p {
    font-size: 14px;
    margin-bottom: 20px;
  }

  .error-close-btn {
    padding: 10px 24px;
    font-size: 12px;
    border-radius: 8px;
  }

  .section-header h2 {
    font-size: 1.8rem;
  }

  .game-modes {
    display: none;
  }

  .trapezoid-pair {
    position: static;
    width: auto;
    height: auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
  }

  .player-1-trapezoid,
  .player-2-trapezoid {
    position: static;
    width: 280px;
    height: 130px;
    padding: 20px;
  }

  .player-2-trapezoid {
    clip-path: polygon(0 0, 100% 0, 100% 100%, 0% 100%);
    padding-left: 20px;
  }

  .play-option-container h3 {
    font-size: 28px;
  }

  .play-option-container p {
    font-size: 12px;
  }

  .ranked-button {
    width: 240px;
    height: 140px;
    margin-left: 0;
  }

  .ranked-content {
    padding: 15px 15px 15px 0;
    gap: 15px;
  }

  .ranked-progress {
    width: 100px;
    height: 100px;
  }

  .ranked-text h3 {
    font-size: 20px;
  }
  .ranked-text {
    transform: translateY(-4px);
  }
}
</style>
