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
        <span v-if="player.status === 'ended'" class="player-status completed">✓</span>
        <span v-else-if="player.status === 'playing'" class="player-status playing">●</span>
        <span v-else-if="player.status === 'ready'" class="player-status ready">⏳</span>
        <span v-else-if="player.status === 'disconnected'" class="player-status disconnected">❌</span>
      </div>
      <div class="player-score-bar-container">
        <div
          class="player-score-bar"
          :style="{ width: getScorePercentage(player.score) + '%' }"
          :class="{ 'completed': player.status === 'ended' }"
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
      const player = this.multiplayerGame_getPlayers[playerId];
      return player?.username || `Player`;
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

.player-status {
  font-size: 16px;
  font-weight: bold;
  margin-left: 8px;
}

.player-status.completed {
  color: #4CAF50; /* Green checkmark */
}

.player-status.playing {
  color: #FF9F1C; /* Orange dot for playing */
  animation: pulse 2s infinite;
}

.player-status.ready {
  color: #2196F3; /* Blue hourglass for ready */
}

.player-status.disconnected {
  color: #f44336; /* Red X for disconnected */
  animation: fadeOut 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

@keyframes fadeOut {
  0% { opacity: 1; }
  50% { opacity: 0.3; }
  100% { opacity: 1; }
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

.player-score-bar.completed {
  background: linear-gradient(to right, #4CAF50, #45a049);
}

/* Responsive styling for multiplayer score tracker */
@media (max-width: 1200px) {
  .multiplayer-score-tracker {
    width: 18vw;
    bottom: 8.5%;
  }
  
  .player-score-row {
    padding: 10px;
    gap: 8px;
  }
  
  .player-name,
  .player-points {
    font-size: 13px;
  }
  
  .player-status {
    font-size: 15px;
  }
}

@media (max-width: 1024px) {
  .multiplayer-score-tracker {
    width: 16vw;
    bottom: 8%;
  }
  
  .player-score-row {
    padding: 8px;
    gap: 6px;
  }
  
  .player-name,
  .player-points {
    font-size: 12px;
  }
  
  .player-status {
    font-size: 14px;
  }
}

@media (max-width: 900px) {
  .multiplayer-score-tracker {
    width: 15vw;
    bottom: 7.5%;
  }
  
  .player-score-row {
    padding: 6px;
    gap: 5px;
  }
  
  .player-name,
  .player-points {
    font-size: 11px;
  }
  
  .player-status {
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  .multiplayer-score-tracker {
    width: 14vw;
    bottom: 7%;
  }
  
  .player-score-row {
    padding: 5px;
    gap: 4px;
  }
  
  .player-name,
  .player-points {
    font-size: 10px;
  }
  
  .player-status {
    font-size: 12px;
  }
}

@media (max-width: 600px) {
  .multiplayer-score-tracker {
    width: 13vw;
    bottom: 6.5%;
  }
  
  .player-score-row {
    padding: 4px;
    gap: 3px;
  }
  
  .player-name,
  .player-points {
    font-size: 9px;
  }
  
  .player-status {
    font-size: 11px;
  }
}

@media (max-width: 480px) {
  .multiplayer-score-tracker {
    width: 12vw;
    bottom: 6%;
  }
  
  .player-score-row {
    padding: 3px;
    gap: 2px;
  }
  
  .player-name,
  .player-points {
    font-size: 8px;
  }
  
  .player-status {
    font-size: 10px;
  }
}
</style>
