<template>
  <div class="player-score-tracker-container">
    <!-- Player 1 (YOU) -->
    <div class="player-score-tracker-1">
      <div class="player-score-text-container">
        <div class="player-info">
          <span class="player-name">{{ player1Name }}</span>
          <span class="player-elo">{{ player1Elo }} ELO</span>
        </div>
        <span class="player-points">{{ player1Score }} PTS</span>
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

    <!-- Player 2 (OPPONENT) -->
    <div class="player-score-tracker-2">
      <div class="player-score-text-container">
        <div class="player-info">
          <span class="player-name">{{ player2Name }}</span>
          <span class="player-elo">{{ player2Elo }} ELO</span>
        </div>
        <span class="player-points">{{ player2Score }} PTS</span>
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

    <!-- Round indicator -->
    <div class="round-indicator">
      <span class="round-text">ROUND {{ currentRound }}/5</span>
    </div>
  </div>
</template>

<script>
import {mapGetters} from "vuex";

export default {
  name: "RankedScoreTracker",
  data() {
    return {
      // Hardcoded values for now
      currentRound: 1,
      player1Elo: 1250,
      player2Elo: 1180,
      player1Score: 0,
      player2Score: 0
    };
  },
  computed: {
    ...mapGetters('gameInfo', [
      'getGameMode'
    ]),
    ...mapGetters('singleplayerGame', [
      'singleplayerGame_getScores',
      'singleplayerGame_getSingleplayerDisplayedScore',
    ]),
    ...mapGetters('guess', [
      'getUserId',
    ]),
    player1Name() {
      return 'YOU';
    },
    player2Name() {
      return 'OPPONENT';
    },
    player1ScorePercentage() {
      const total = this.player1Score + this.player2Score || 1;
      return Math.floor((this.player1Score * 100) / total);
    },
    player2ScorePercentage() {
      const total = this.player1Score + this.player2Score || 1;
      return Math.floor((this.player2Score * 100) / total);
    }
  },
  methods: {
    // Method to update scores (will be called from parent component)
    updateScores(player1Score, player2Score) {
      this.player1Score = player1Score;
      this.player2Score = player2Score;
    },
    // Method to update current round
    updateRound(round) {
      this.currentRound = round;
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
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
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

.player-score-tracker-1 .player-score-progress-container {
  border-radius: 0 25px 25px 25px;
}

.player-score-tracker-2 .player-score-progress-container {
  border-radius: 25px 0 25px 25px;
  direction: rtl;
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

.player-elo {
  font-size: 12px;
  font-weight: 500;
  color: var(--light-grey);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.player-points {
  font-size: 14px;
  font-weight: 600;
  color: var(--light-grey);
}

.round-indicator {
  position: absolute;
  top: -40px;
  left: 50%;
  transform: translateX(-50%);
  background: var(--dark-grey);
  padding: 8px 20px;
  border-radius: 20px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.round-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--white);
  text-transform: uppercase;
  letter-spacing: 1px;
}
</style>
