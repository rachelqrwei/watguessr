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
