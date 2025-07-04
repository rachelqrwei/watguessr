<script setup lang="ts">
import { onMounted, ref, computed } from "vue";

//TODO: connect player info to backend
const player1Name = "Name 1";
const player2Name = "Name 2";

const player1Score = 40;
const player2Score = 60;

const player1ScorePercentage = computed(() => {
  return Math.floor((player1Score * 100) / (player1Score + player2Score))
})

const player2ScorePercentage = computed(() => {
  return Math.floor((player2Score * 100) / (player1Score + player2Score))
})
</script>

<template>
  <div class="player-score-tracker-container">
    <div class="player-score-tracker-1">
      <div class="player-score-text">
        <h1 class="player-name">{{ player1Name }}</h1>
        <p>{{ player1Score }} pts</p>
      </div>

      <div class="player-score-progress-bar"
           :style="{
             width: player1ScorePercentage + '%',
             background: 'var(--player-1-gradient)'
           }"
      />
    </div>

    <div class="player-score-tracker-2">
      <div class="player-score-text">
        <h1 class="player-name">{{ player2Name }}</h1>
        <p>{{ player2Score }} pts</p>
      </div>

      <div class="player-score-progress-bar"
           :style="{
             width: player2ScorePercentage + '%',
             background: 'var(--player-2-gradient)'
           }"
      />
    </div>
  </div>
</template>

<style scoped>

.player-score-tracker-container {
  position: fixed;
  bottom: 0;          /* Aligns to bottom */
  left: 0;            /* Aligns to left edge */
  width: 100%;        /* Full width */
  color: white;
  padding: 16px;
  z-index: 5;

  display: flex;
  justify-content: space-between; /* Push children to left & right */
  align-items: center;
}

.player-name {
  font-size: 20px;
  font-weight: bold;
}

.player-score-tracker-1 {
}

.player-score-tracker-2 {
  text-align: right;
  direction: rtl;
}

.player-score-tracker-1,
.player-score-tracker-2 {
  max-width: 45%;
  background: #333;
  padding: 20px;
  min-width: 30%;
  font-size: 18px;
  border-radius: 30px;
}

.player-score-progress-bar {
  height: 10px;
  background-color: #aaa;
  margin-top: 10px;
  border-radius: 5px;
}

.player-score-text {
  direction: ltr;
}
</style>
