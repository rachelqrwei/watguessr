<template>
  <div class="round-end-container">
    <div class="round-header">
      <span class="round-label">ROUND</span>
      <span class="round-number">#{{ roundNumber }}</span>
    </div>
    
    <div class="points-section">
      <span class="points-label">POINTS LOST</span>
      <span class="points-value">{{ pointsLost }}</span>
    </div>
    
    <div class="stats-section">
      <div class="stat-item">
        <span class="stat-label">TIME TAKEN</span>
        <span class="stat-value">{{ timeTaken }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">DISTANCE</span>
        <span class="stat-value">{{ distance }}</span>
      </div>
    </div>
    
    <div class="map-section">
      <svg width="100%" height="220" viewBox="0 0 400 220">
        <defs>
          <radialGradient id="guessGlow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stop-color="#ff4136" stop-opacity="0.7"/>
            <stop offset="100%" stop-color="#ff4136" stop-opacity="0"/>
          </radialGradient>
          <radialGradient id="actualGlow" cx="50%" cy="50%" r="50%">
            <stop offset="0%" stop-color="#ffe066" stop-opacity="0.7"/>
            <stop offset="100%" stop-color="#ffe066" stop-opacity="0"/>
          </radialGradient>
        </defs>
        <line x1="80" y1="110" x2="320" y2="170" stroke="#ffe066" stroke-width="6" stroke-dasharray="10,8" />
        <circle cx="80" cy="110" r="22" fill="url(#actualGlow)" />
        <circle cx="80" cy="110" r="12" fill="#ffe066" stroke="#fff" stroke-width="3" />
        <text x="40" y="105" font-size="16" fill="#ffe066" font-weight="bold">Actual</text>
        <circle cx="320" cy="170" r="22" fill="url(#guessGlow)" />
        <circle cx="320" cy="170" r="12" fill="#ff4136" stroke="#fff" stroke-width="3" />
        <text x="330" y="175" font-size="16" fill="#ff4136" font-weight="bold">Guess</text>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
// Placeholder props/data
const roundNumber = 2
const pointsLost = 37
const timeTaken = '00:23.45'
const distance = '142m'

</script>

<style scoped>
.round-end-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #1a1a1c;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  z-index: 1;
}

.round-header {
  text-align: center;
}

.round-label {
  display: block;
  font-size: 16px;
  color: var(--yellow);
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 0px;
}

.round-number {
  display: block;
  font-size: 42px;
  font-weight: 900;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.points-section {
  text-align: center;
  padding: 5px 28px;
  background: transparent;
}

.points-label {
  display: block;
  font-size: 13px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 0px;
}

.points-value {
  display: block;
  font-size: 28px;
  font-weight: 900;
  color: #fff;
}

.stats-section {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 5px 10px;
  min-width: 80px;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 0px;
}

.stat-value {
  display: block;
  font-size: 16px;
  font-weight: 900;
  color: var(--yellow);
}

.map-section {
  width: 90%;
  max-width: 600px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 15px;
  padding: 15px;
}
</style> 