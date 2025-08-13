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

    <div class="player-score-tracker-2" v-if="singleplayerGame_getGameMode === 'Multiplayer'">
      <div class="player-score-text-container">
        <span class="player-points">{{ player2Score }} PTS</span>
        <span class="player-name">{{ player2Name }}</span>
      </div>
      <div class="player-score-progress-container">
        <div
          class="player-score-progress-bar"
          :style="{
            width: player2ScorePercentage + '%',
            background: 'var(--player-2-gradient)'
          }"
        />
      </div>
    </div>
  </div>
</template>

<script>
import {mapGetters} from "vuex";

export default {
  name: "PlayerScoreTracker",
  data() {
    return {
      // TODO: connect player info to backend
      player1Name: "YOU",
      player2Name: "NAME 2",
      player1Score: 180,
      player2Score: 300
    };
  },
  computed: {
    ...mapGetters('singleplayer', [
      'singleplayerGame_getScores',
      'singleplayerGame_getGameMode',
      'singleplayerGame_getSingleplayerDisplayedScore',
    ]),
    ...mapGetters('guess', [
      'getUserId',
    ]),
    displayedPoints() {
      if (this.singleplayerGame_getGameMode === 'Singleplayer') {
        return this.singleplayerGame_getSingleplayerDisplayedScore ?? 1000;
      }
      // Multiplayer fallback: show this player's score by id if present
      const key = this.getUserId || 'player';
      return this.singleplayerGame_getScores[key] || 0;
    },
    player1ScorePercentage() {
      if (this.singleplayerGame_getGameMode == "Singleplayer") {
        // percentage of remaining points out of 1000
        const remaining = this.singleplayerGame_getSingleplayerDisplayedScore ?? 1000;
        return Math.floor((remaining * 100) / 1000);
      }
      else {
        const key = this.getUserId || 'player';
        const me = this.singleplayerGame_getScores[key] || 0;
        const other = this.player2Score;
        const denom = me + other || 1;
        return Math.floor((me * 100) / denom);
      }

    },
    player2ScorePercentage() {
      return Math.floor(
        (this.player2Score * 100) /
        (this.player1Score + this.player2Score)
      );
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
  justify-content: space-between;
  align-items: flex-end;
}

.player-score-tracker-1,
.player-score-tracker-2 {
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
  box-shadow: none !important;
  filter: none;
}

.player-score-tracker-1 .player-score-text-container {
  border-radius: 25px 25px 0 0px;
}

.player-score-tracker-2 .player-score-text-container {
  border-radius: 25px 25px 0px 0;
  margin-left: auto;
}

.player-score-progress-container {
  background: var(--dark-grey);
  padding: 12px 12px 15px 25px;
  width: 400px;
  height: 40px;
  box-shadow: 0 10px 10px rgba(0, 0, 0, 0.2);
  position: relative;
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

.player-score-tracker-1 .player-score-progress-container {
  border-radius: 0 25px 25px 25px;
}

.player-score-tracker-2 .player-score-progress-container {
  border-radius: 25px 0 25px 25px;
  direction: rtl;
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
