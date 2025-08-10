<template>
  <div class="player-score-tracker-container">
    <!-- Player 1 (fixed on left) -->
    <div class="player-score-tracker-1">
      <div class="player-score-text-container">
        <span class="player-name">{{ player1Name }}</span>
        <span class="player-points">{{ getScores['undefined'] }} PTS</span>
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

    <!-- Other players on the right -->
    <div class="player-score-tracker-others" v-if="getGameMode === 'Singleplayer'">
      <div
        class="other-player"
        v-for="player in otherPlayers"
        :key="player.id"
      >
        <div class="player-score-text-container">
          <span class="player-points">{{ getScores[player.id] ?? 0 }} PTS</span>
          <span class="player-name">{{ player.name }}</span>
          <span v-if="completionStatus[player.id]" class="checkmark">✔️</span>
        </div>
        <div class="player-score-progress-container">
          <div
            class="player-score-progress-bar"
            :style="{
              width: getScorePercentage(player.id) + '%',
              background: player.gradient
            }"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "PlayerScoreTracker",
  data() {
    return {
      player1Name: "NAME 1",  // keep this as is
      getScores: {
        undefined: 450,
        player2: 300,
        player3: 510,
        player4: 270,
      }
    };
  },
  computed: {
    ...mapGetters("game", [
      "getGameMode",
      "getPlayerId",
      "getPlayers",
      "getPlayersCompletionStatus",
    ]),

    player1ScorePercentage() {
      if (this.getGameMode == "Singleplayer") {
        return Math.floor(
          (this.getScores["undefined"] * 100) / 10000
        );
      } else {
        return Math.floor(
          (this.getScores[0] * 100) /
          (this.getScores[0] + this.getScores[1])
        );
      }
    },

    otherPlayers() {
      // Exclude player 1 from the list of players
      const player1Id = this.getPlayers?.[0]?.id; // assuming player1 is first in getPlayers
      return (this.getPlayers || [])
        .filter((p) => p.id !== player1Id)
        .map((player, index) => ({
          ...player,
          gradient:
            index % 2 === 0
              ? "var(--player-2-gradient)"
              : "var(--player-3-gradient)", // extend as needed for more players
        }));
    },

    completionStatus() {
      return this.getPlayersCompletionStatus || {};
    },
  },
  methods: {
    getScorePercentage(playerId) {
      const scores = Object.values(this.getScores || {});
      const total = scores.reduce((acc, val) => acc + val, 0) || 1;
      const playerScore = this.getScores[playerId] || 0;
      return Math.floor((playerScore * 100) / total);
    },
  },
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
  gap: 12px;
}

.player-score-tracker-1 {
  display: flex;
  flex-direction: column;
  max-width: 45%;
}

.player-score-tracker-others {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 45%;
  align-items: flex-end;
}

.other-player {
  width: 100%;
}

.player-score-text-container {
  background: var(--dark-grey);
  padding: 12px 25px 0 25px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  box-shadow: none !important;
  filter: none;
  border-radius: 25px 25px 0 0;
}

.player-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--white);
  white-space: nowrap;
}

.player-points {
  font-size: 14px;
  font-weight: 600;
  color: var(--light-grey);
  white-space: nowrap;
}

.checkmark {
  font-size: 18px;
  color: var(--yellow);
  margin-left: 8px;
  white-space: nowrap;
}

.player-score-progress-container {
  background: var(--dark-grey);
  padding: 12px 12px 15px 25px;
  width: 100%;
  height: 40px;
  box-shadow: 0 10px 10px rgba(0, 0, 0, 0.2);
  position: relative;
  border-radius: 0 0 25px 25px;
}

.player-score-progress-container::before {
  content: "";
  position: absolute;
  top: 12px;
  left: 25px;
  right: 12px;
  bottom: 15px;
  background: #474f54;
  border-radius: 12px;
}

.player-score-progress-bar {
  height: 100%;
  border-radius: 12px;
  transition: width 0.3s ease;
  position: relative;
  z-index: 1;
}
</style>
