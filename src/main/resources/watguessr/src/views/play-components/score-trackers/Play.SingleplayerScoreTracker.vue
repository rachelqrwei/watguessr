<template>
  <div class="player-score-tracker-container">
    <div class="player-score-tracker-1">
      <div class="player-score-text-container">
        <span class="player-name">{{ player1Name }}</span>
        <span class="player-points">{{ displayedPoints }} PTS</span>
      </div>
      <div class="player-score-progress-container">
        <div
          class="player-score-progress-bar"
          :style="{
            width: player1ScorePercentage + '%',
            background: 'var(--player-1-gradient)'
          }"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "PlayerSingleplayerScoreTracker",
  props: {
    player1Name: {
      type: String,
      default: "YOU"
    }
  },
  computed: {
    ...mapGetters("singleplayerGame", [
      "singleplayerGame_getSingleplayerDisplayedScore"
    ]),
    displayedPoints() {
      return this.singleplayerGame_getSingleplayerDisplayedScore ?? 1000;
    },
    player1ScorePercentage() {
      const remaining = this.singleplayerGame_getSingleplayerDisplayedScore ?? 1000;
      return Math.floor((remaining * 100) / 1000);
    }
  }
};
</script>

<style scoped>
.player-score-tracker-container {
  position: fixed;
  bottom: 9%;
  left: 50%;
  transform: translateX(-50%);
  width: 99vw;
  color: white;
  padding: 0 16px;
  z-index: 5;
  display: flex;
}

.player-score-tracker-1 {
  display: flex;
  flex-direction: column;
}

.player-score-text-container {
  background: var(--dark-grey);
  padding: 12px 25px 0 25px;
  display: flex;
  gap: 28px;
  align-items: center;
  width: fit-content;
}

.player-score-tracker-1 .player-score-text-container {
  border-radius: 25px 25px 0 0px;
}

.player-score-progress-container {
  background: var(--dark-grey);
  padding: 12px 12px 15px 25px;
  width: 400px;
  height: 40px;
  box-shadow: 0 10px 10px rgba(0, 0, 0, 0.2);
  position: relative;
  border-radius: 0 25px 25px 25px;
}

/* Responsive styling for score trackers */
@media (max-width: 1200px) {
  .player-score-progress-container {
    width: 350px;
    height: 35px;
    padding: 10px 10px 12px 20px;
  }
  
  .player-score-text-container {
    padding: 10px 20px 0 20px;
    gap: 20px;
  }
  
  .player-name,
  .player-points {
    font-size: 13px;
  }
}

@media (max-width: 1024px) {
  .player-score-progress-container {
    width: 300px;
    height: 32px;
    padding: 8px 8px 10px 18px;
  }
  
  .player-score-text-container {
    padding: 8px 18px 0 18px;
    gap: 18px;
  }
  
  .player-name,
  .player-points {
    font-size: 12px;
  }
}

@media (max-width: 900px) {
  .player-score-progress-container {
    width: 280px;
    height: 30px;
    padding: 6px 6px 8px 16px;
  }
  
  .player-score-text-container {
    padding: 6px 16px 0 16px;
    gap: 16px;
  }
  
  .player-name,
  .player-points {
    font-size: 11px;
  }
}

@media (max-width: 768px) {
  .player-score-tracker-container {
    bottom: 8%;
    padding: 0 12px;
  }
  
  .player-score-progress-container {
    width: 260px;
    height: 28px;
    padding: 5px 5px 7px 14px;
  }
  
  .player-score-text-container {
    padding: 5px 14px 0 14px;
    gap: 14px;
  }
  
  .player-name,
  .player-points {
    font-size: 10px;
  }
}

@media (max-width: 600px) {
  .player-score-tracker-container {
    bottom: 7%;
    padding: 0 10px;
  }
  
  .player-score-progress-container {
    width: 240px;
    height: 26px;
    padding: 4px 4px 6px 12px;
  }
  
  .player-score-text-container {
    padding: 4px 12px 0 12px;
    gap: 12px;
  }
  
  .player-name,
  .player-points {
    font-size: 9px;
  }
}

@media (max-width: 480px) {
  .player-score-tracker-container {
    bottom: 6%;
    padding: 0 8px;
  }
  
  .player-score-progress-container {
    width: 220px;
    height: 24px;
    padding: 3px 3px 5px 10px;
  }
  
  .player-score-text-container {
    padding: 3px 10px 0 10px;
    gap: 10px;
  }
  
  .player-name,
  .player-points {
    font-size: 8px;
  }
}

.player-score-progress-container::before {
  content: '';
  position: absolute;
  top: 12px;
  left: 25px;
  right: 12px;
  bottom: 15px;
  background: #474F54;
  border-radius: 12px;
}

.player-score-progress-bar {
  height: 100%;
  border-radius: 12px;
  transition: width 0.3s ease;
  position: relative;
  z-index: 1;
}

.player-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--white);
}

.player-points {
  font-size: 14px;
  font-weight: 600;
  color: var(--light-grey);
}
</style>
