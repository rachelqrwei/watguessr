<template>
  <section class="play-section" :class="{ loaded: isLoaded }">
    <div class="play-content">
      <div class="section-header">
        <h2>PLAY WATGUESSR</h2>
      </div>
      
      <div class="game-modes">
        <div class="player-1-trapezoid">
          <div class="play-option-container">
            <h3>SOLO</h3>
            <p>PLAY BY YOURSELF UNTIL YOUR POINTS RUN OUT!</p>
          </div>
        </div>

        <div class="player-2-trapezoid">
          <div class="play-option-container">
            <h3>DUELS</h3>
            <p>PLAY AGAINST A FRIEND AND COMPETE FOR POINTS!</p>
          </div>
        </div>

        <div class="daily-mini-button">
          <div class="daily-mini-content">
            <div class="daily-mini-progress">
              <img src="/DailyMini.png" alt="Daily Mini Progress" class="daily-mini-image" />
            </div>
            <div class="daily-mini-text">
              <h3>DAILY</h3>
              <h3>MINI</h3>
            </div>
          </div>
        </div>
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
.play-section {
  padding: 20px 40px 40px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 0.2s;
  max-width: 1200px;
  margin: 0 auto;
}

.play-section.loaded {
  opacity: 1;
  transform: translateY(0);
}

.play-content {
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: left;
  margin-bottom: 15px;
  margin-left: calc(50% - 455px);
}

.section-header h2 {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--light-grey);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  text-shadow: none;
}

.game-modes {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.player-1-trapezoid {
  position: relative;
  width: 360px;
  height: 180px;
  clip-path: polygon(0 0, 100% 0, 75% 100%, 0% 100%);
  border-radius: 10px 5px 20px 10px;
  padding: 30px;
  overflow: hidden;
  z-index: 2;
  transition: all 0.3s ease;
  cursor: pointer;
}

.player-1-trapezoid::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--player-1-gradient);
  opacity: 0.8;
  z-index: -1;
  transition: opacity 0.3s ease;
}

.player-1-trapezoid:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.player-1-trapezoid:hover::before {
  opacity: 1;
}

.player-2-trapezoid {
  position: relative;
  width: 360px;
  height: 180px;
  left: -80px;
  clip-path: polygon(25% 0%, 100% 0%, 100% 100%, 0% 100%);
  border-radius: 10px 5px 20px 10px;
  padding: 30px;
  padding-left: 100px;
  overflow: hidden;
  z-index: 1;
  transition: all 0.3s ease;
  cursor: pointer;
}

.player-2-trapezoid::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: var(--player-2-gradient);
  opacity: 0.8;
  z-index: -1;
  transition: opacity 0.3s ease;
}

.player-2-trapezoid:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

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
  margin-bottom: 12px;
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(255, 255, 255, 0.2);
  letter-spacing: -1px;
  text-transform: uppercase;
}

.play-option-container p {
  font-size: 12px;
  font-weight: 700;
  color: var(--white);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  line-height: 1.3;
  letter-spacing: 0.5px;
  margin-bottom: 10px;
}

.daily-mini-button {
  position: relative;
  width: 250px;
  height: 180px;
  border-radius: 20px;
  padding: 0;
  overflow: hidden;
  z-index: 0;
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  align-items: center;
  background: #4A4A4C;
  border: none;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  margin-left: -70px;
}

.daily-mini-button::before {
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
  border-radius: 20px;
}

.daily-mini-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.4);
}

.daily-mini-button:hover::before {
  opacity: 1;
}

.daily-mini-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 20px 0 0;
  gap: 0;
  z-index: 1;
}

.daily-mini-progress {
  flex-shrink: 0;
  width: 140px;
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 0;
  margin: 0;
  border: none;
}

.daily-mini-image {
  width: 80%;
  height: 80%;
  object-fit: contain;
  margin-left: -0.7vw;
}

.daily-mini-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: flex-start;
  gap: -5px;
  padding-left: 0;
  padding-top: 10px;
  padding-bottom: 55px;
  padding-right: 20px;
}

.daily-mini-text h3 {
  font-weight: 900;
  font-size: 32px;
  color: var(--white);
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.5),
    0 0 20px rgba(255, 255, 255, 0.2);
  letter-spacing: -0.5px;
  text-transform: uppercase;
  margin: 0;
  line-height: 0.8;
}

@media (max-width: 768px) {
  .section-header h2 {
    font-size: 1.8rem;
  }
  
  .game-modes {
    flex-direction: column;
    gap: 15px;
  }
  
  .player-1-trapezoid,
  .player-2-trapezoid {
    width: 280px;
    height: 130px;
    left: 0;
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
  
  .daily-mini-button {
    width: 240px;
    height: 140px;
    margin-left: 0;
  }
  
  .daily-mini-content {
    padding: 15px 15px 15px 0;
    gap: 15px;
  }
  
  .daily-mini-progress {
    width: 100px;
    height: 100px;
  }
  
  .daily-mini-text h3 {
    font-size: 20px;
  }
}
</style> 