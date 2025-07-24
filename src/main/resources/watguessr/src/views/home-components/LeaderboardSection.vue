<template>
  <section class="leaderboard-preview" :class="{ loaded: isLoaded }">
    <div class="leaderboard-content">
      <h2>TOP PLAYERS THIS WEEK</h2>
      <div class="leaderboard-list">
        <div class="leaderboard-item rank-1">
          <div class="rank-badge">1</div>
          <div class="player-info">
            <div class="player-name">UWaterloo_Legend</div>
            <div class="player-score">24,750 pts</div>
          </div>
          <div class="player-medal">🥇</div>
        </div>
        <div class="leaderboard-item rank-2">
          <div class="rank-badge">2</div>
          <div class="player-info">
            <div class="player-name">CampusExplorer</div>
            <div class="player-score">23,180 pts</div>
          </div>
          <div class="player-medal">🥈</div>
        </div>
        <div class="leaderboard-item rank-3">
          <div class="rank-badge">3</div>
          <div class="player-info">
            <div class="player-name">GeographyMaster</div>
            <div class="player-score">21,940 pts</div>
          </div>
          <div class="player-medal">🥉</div>
        </div>
        <div class="leaderboard-item">
          <div class="rank-badge">4</div>
          <div class="player-info">
            <div class="player-name">MapWizard2024</div>
            <div class="player-score">19,850 pts</div>
          </div>
        </div>
        <div class="leaderboard-item">
          <div class="rank-badge">5</div>
          <div class="player-info">
            <div class="player-name">CampusGuru</div>
            <div class="player-score">18,720 pts</div>
          </div>
        </div>
      </div>
      <div class="leaderboard-cta">
        <button class="view-full-leaderboard">View Full Leaderboard</button>
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
.leaderboard-preview {
  padding: 80px 40px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 1s;
  background: var(--dark-grey);
}

.leaderboard-preview.loaded {
  opacity: 1;
  transform: translateY(0);
}

.leaderboard-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.leaderboard-content h2 {
  font-size: 1.8rem;
  font-weight: 800;
  color: var(--white);
  margin-bottom: 30px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.leaderboard-list {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.leaderboard-item:hover {
  transform: translateY(-3px);
  background: rgba(255, 255, 255, 0.08);
}

.rank-1 {
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.2) 0%, rgba(255, 203, 59, 0.1) 100%);
  border: 1px solid rgba(255, 203, 59, 0.3);
  box-shadow: 0 0 20px rgba(255, 203, 59, 0.3);
}

.rank-2 {
  background: linear-gradient(135deg, rgba(150, 150, 150, 0.2) 0%, rgba(150, 150, 150, 0.1) 100%);
  border: 1px solid rgba(150, 150, 150, 0.3);
  box-shadow: 0 0 10px rgba(150, 150, 150, 0.2);
}

.rank-3 {
  background: linear-gradient(135deg, rgba(100, 100, 100, 0.2) 0%, rgba(100, 100, 100, 0.1) 100%);
  border: 1px solid rgba(100, 100, 100, 0.3);
  box-shadow: 0 0 5px rgba(100, 100, 100, 0.1);
}

.rank-badge {
  font-size: 1.2rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.player-info {
  text-align: left;
  flex: 1;
}

.player-name {
  font-size: 1rem;
  font-weight: 700;
  color: var(--white);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 0.3px;
}

.player-score {
  font-size: 1.1rem;
  font-weight: 900;
  color: var(--yellow);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  margin-top: 5px;
}

.player-medal {
  font-size: 1.5rem;
  color: var(--yellow);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.leaderboard-cta {
  margin-top: 30px;
}

.view-full-leaderboard {
  background: var(--yellow);
  color: var(--dark-grey);
  padding: 12px 25px;
  border-radius: 10px;
  font-size: 1rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 203, 59, 0.3);
}

.view-full-leaderboard:hover {
  background: var(--dark-yellow);
  box-shadow: 0 6px 20px rgba(255, 203, 59, 0.4);
}

@media (max-width: 768px) {
  .leaderboard-list {
    flex-direction: column;
    gap: 15px;
  }

  .leaderboard-item {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    padding: 10px 15px;
  }

  .rank-badge {
    width: 25px;
    height: 25px;
    font-size: 1rem;
  }

  .player-info {
    text-align: right;
    flex: none;
  }

  .player-name {
    font-size: 0.9rem;
  }

  .player-score {
    font-size: 1rem;
  }

  .player-medal {
    font-size: 1.2rem;
  }
}
</style> 