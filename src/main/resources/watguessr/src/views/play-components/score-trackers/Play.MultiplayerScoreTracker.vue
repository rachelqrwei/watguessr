<template>
  <div class="multiplayer-score-tracker">
    <div
      v-for="(player, id) in multiplayerGame_getPlayers"
      :key="id"
      class="player-score-row"
    >
      <div class="player-score-text">
        <span class="player-name">{{ getPlayerName(id) }}</span>
        <span class="player-points">{{ player.score }} PTS</span>
      </div>
      <div class="player-score-bar-container">
        <div
          class="player-score-bar"
          :style="{ width: getScorePercentage(player.score) + '%' }"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "MultiplayerScoreTracker",
  computed: {
    ...mapGetters("user", [
      "getUsers"
    ]),
    ...mapGetters("multiplayerGame", [
      "multiplayerGame_getPlayers"
    ]),
    maxScore() {
      const scores = Object.values(this.multiplayerGame_getPlayers).map(p => p.score);
      return scores.length ? Math.max(...scores) : 1;
    }
  },
  methods: {
    getPlayerName(playerId) {
      console.log(playerId);
      // Placeholder: Replace with real name lookup if available
      return playerId;
    },
    getScorePercentage(score) {
      return Math.floor((score * 100) / this.maxScore);
    }
  }
};
</script>

<style scoped>
.multiplayer-score-tracker {
  position: fixed;
  z-index: 999;
  bottom: 9%;
  width: 20vw;
  color: white;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.player-score-row {
  background: var(--dark-grey);
  padding: 12px;
  border-radius: 12px;
}

.player-score-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.player-name {
  font-weight: bold;
  font-size: 14px;
}

.player-points {
  font-size: 14px;
  color: var(--light-grey);
}

.player-score-bar-container {
  background: #474F54;
  border-radius: 8px;
  height: 10px;
  overflow: hidden;
}

.player-score-bar {
  height: 100%;
  background: linear-gradient(to right, #FFCB3B, #FF9F1C);
  transition: width 0.3s ease;
}
</style>
