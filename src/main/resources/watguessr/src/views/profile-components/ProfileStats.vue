<template>
  <transition name="fade-slide" mode="out-in">
    <div v-if="leaderboardUser && !isLoading && !errorMessage" :key="leaderboardUser.id" class="profile">
      <div class="profile-content">
        <div class="left-column">
          <div class="hero">
            <div class="avatar-wrap">
              <div class="avatar" :style="{ background: avatarColors.bg, color: avatarColors.fg }" aria-hidden="true">{{
                (leaderboardUser?.username || 'G').charAt(0).toUpperCase() }}</div>
            </div>
            <div class="hero-info">
              <h1 class="name">{{ leaderboardUser.username }}</h1>
              <div class="meta">
                <span class="elo-pill"><span class="label">ELO</span> {{ leaderboardUser.elo }}</span>
              </div>
            </div>
          </div>

          <div class="stats-cards">
            <div class="card">
              <div class="card-label">Streak</div>
              <div class="streak-display">
                <div class="streak-glow">
                  <img src="../../assets/images/Header/streak-icon.png" alt="Streak" />
                </div>
                <div class="streak-number">{{ leaderboardUser.streak }}</div>
              </div>
            </div>

            <div class="card">
              <div class="card-label">All Games Played</div>
              <div class="card-value">{{ leaderboardUser?.gamesPlayed || 0 }}</div>
            </div>
          </div>
        </div>

        <div class="right-column">
          <div class="card wins-losses-card">
            <div class="wins-losses-content">
              <div class="left-legend-section">
                <div class="card-label">Ranked Winrate</div>
                <div class="chart-legend">
                  <div class="legend-item">
                    <div class="legend-color wins-color"></div>
                    <div class="legend-text">
                      <span class="legend-value">{{ leaderboardUser?.gamesWon || 0 }}</span>
                      <span class="legend-label">wins</span>
                    </div>
                  </div>
                  <div class="legend-item">
                    <div class="legend-color losses-color"></div>
                    <div class="legend-text">
                      <span class="legend-value">{{ leaderboardUser?.gamesLost || 0 }}</span>
                      <span class="legend-label">losses</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="donut-chart-container">
                <svg class="donut-chart" viewBox="0 0 100 100" style="transform: rotate(-90deg)">
                  <!-- Background circle (red for empty) -->
                  <circle cx="50" cy="50" r="35" fill="none" stroke="#FF7F7F" stroke-width="8" />
                  <!-- Win rate circle (green) -->
                  <circle cx="50" cy="50" r="35" fill="none"
                    :stroke="rankedWinRate > 0 ? '#B6FF7F' : 'rgba(255, 255, 255, 0.1)'" stroke-width="8"
                    :stroke-dasharray="strokeDashArray" stroke-dashoffset="0" stroke-linecap="round" />
                </svg>
                <div class="donut-center">
                  <div class="win-percentage">{{ rankedWinRate }}%</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading profile...</p>
    </div>
    <div v-else-if="errorMessage" class="error">{{ errorMessage }}</div>
    <div v-else class="profile-empty-section">
      <div class="profile-empty">
        <div class="profile-empty-header">
          <h2>Profile Stats</h2>
        </div>
        <div class="profile-empty-body">No profile data found.</div>
      </div>
    </div>
  </transition>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { colorPairFromName } from '@/utils/color'

export default {
  name: 'ProfileStats',
  data() {
    return {
      leaderboardUser: null,
      isLoading: false,
      errorMessage: null
    }
  },
  computed: {
    ...mapGetters('profile', ['getProfileUserId']),

    avatarColors() {
      const name = this.leaderboardUser?.username || 'Guest'
      return colorPairFromName(name, { bgSaturation: 90, bgLightness: 80, fgSaturation: 100, fgLightness: 30, fgHueShift: -12 })
    },

    rankedGamesPlayed() {
      return (this.leaderboardUser?.gamesWon || 0) + (this.leaderboardUser?.gamesLost || 0)
    },

    rankedWinRate() {
      if (this.rankedGamesPlayed === 0) return 0
      return Math.round((this.leaderboardUser.gamesWon / this.rankedGamesPlayed) * 100)
    },

    rankedLossRate() {
      if (this.rankedGamesPlayed === 0) return 0
      return Math.round((this.leaderboardUser.gamesLost / this.rankedGamesPlayed) * 100)
    },

    // Donut chart circumference calculations
    totalCircumference() {
      const radius = 35
      return 2 * Math.PI * radius
    },

    winRateCircumference() {
      return (this.rankedWinRate / 100) * this.totalCircumference
    },

    strokeDashArray() {
      const dash = this.winRateCircumference  // Green portion
      const gap = this.totalCircumference - this.winRateCircumference  // Gap portion
      return `${dash} ${gap}`
    }
  },

  watch: {
    getProfileUserId: {
      handler(newUserId) {
        if (newUserId) {
          this.fetchUserStats()
        } else {
          this.leaderboardUser = null
          this.errorMessage = 'No user selected.'
        }
      },
      immediate: true
    }
  },

  methods: {
    ...mapActions('user', ['fetchLeaderboardForUserId']),

    async fetchUserStats() {
      if (!this.getProfileUserId) return

      this.isLoading = true
      this.errorMessage = null
      this.leaderboardUser = null

      try {
        const id = this.getProfileUserId
        if (id) {
          // Use the existing leaderboard endpoint which has all the same data
          this.leaderboardUser = await this.fetchLeaderboardForUserId(id)
          if (!this.leaderboardUser) {
            throw new Error('Failed to fetch user stats')
          }
        } else {
          this.errorMessage = 'No user selected.'
        }
      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to load user stats'
      } finally {
        this.isLoading = false
      }
    }
  }
}
</script>

<style scoped>
.profile-empty-section {
  background: rgba(42, 42, 44, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(8px);
  margin-bottom: 15px;
}

.profile-empty {
  padding: 22px 22px 22px 22px;
}

.profile-empty-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.profile-empty-header h2 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
}

.profile-empty-body {
  color: var(--light-grey);
}

.profile {
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(8px);
  margin-bottom: 15px;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
  padding: 22px 22px 22px 22px;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-column {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: stretch;
}

.hero {
  display: grid;
  grid-template-columns: 84px 1fr;
  gap: 16px;
  background: none;
}

.avatar-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--yellow);
  color: var(--dark-grey);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.6rem;
  box-shadow: none;
}

.hero-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.name {
  margin: 0 0 6px 0;
  font-size: 1.8rem;
  font-weight: 800;
  letter-spacing: 0.2px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--light-grey);
}

.meta .dot {
  opacity: 0.6;
}

.elo-pill {
  background: rgba(255, 203, 59, 0.12);
  border: 1px solid rgba(255, 203, 59, 0.3);
  color: var(--yellow);
  padding: 4px 10px;
  border-radius: 999px;
  font-weight: 700;
}

.elo-pill .label {
  opacity: 0.9;
  margin-right: 6px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 14px 16px;
  transition: none;
}

.card:hover {
  transform: none;
  background: rgba(255, 255, 255, 0.05);
}

.card-label {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.78rem;
  letter-spacing: 0.9px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  margin-bottom: 6px;
}

.card-value {
  font-size: 1.3rem;
  font-weight: 800;
}

.card-value.accent {
  color: var(--yellow);
}

/* Streak display */
.streak-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.streak-glow {
  position: relative;
  display: inline-block;
  border-radius: 50%;
}

.streak-glow img {
  height: 28px;
  width: 24px;
  display: block;
  position: relative;
  z-index: 1;
}

.streak-number {
  font-weight: 550;
  font-size: 1.3rem;
  background: var(--player-1-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* Win/Loss Chart */
.wins-losses-card {
  display: flex;
  flex-direction: column;
  gap: 13px;
  height: fit-content;
  width: 100%;
}

.right-column>.wins-losses-card {
  margin-top: auto;
}

.wins-losses-content {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 20px;
}

.left-legend-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
  flex: 1;
  align-self: flex-start;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.donut-chart-container {
  position: relative;
  width: 160px;
  height: 160px;
  flex-shrink: 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

.donut-chart {
  position: absolute;
  width: 100%;
  height: 100%;
}

.donut-chart circle {
  fill: none;
  stroke-width: 8;
  stroke-linecap: round;
}

.donut-center {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
}

.win-percentage {
  font-size: 1.6rem;
  font-weight: 800;
  color: var(--white);
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.wins-color {
  background-color: #B6FF7F;
}

.losses-color {
  background-color: #FF7F7F;
}

.legend-text {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.legend-label {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.78rem;
  letter-spacing: 0.9px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  opacity: 0.9;
}

.legend-value {
  font-size: 1.1rem;
  font-weight: 800;
  color: var(--white);
}

/* Loading and error states */
.loading,
.error,
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--light-grey);
  text-align: center;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-top: 3px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

.error {
  color: #FF7F7F;
}

/* Fade + slide up transition */
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 300ms ease, transform 300ms ease;
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 18px 18px 18px 18px;
  }

  .hero {
    grid-template-columns: 64px 1fr;
    gap: 12px;
  }

  .avatar {
    width: 64px;
    height: 64px;
    font-size: 1.4rem;
  }

  .name {
    font-size: 1.6rem;
    letter-spacing: 0.8px;
  }

  .elo-pill {
    font-size: 0.8rem;
    padding: 6px 10px;
  }

  .card-label {
    font-size: 0.75rem;
    letter-spacing: 0.6px;
  }

  .streak-number {
    font-size: 1.2rem;
  }

  .card-value {
    font-size: 1.2rem;
  }

  .win-percentage {
    font-size: 1.3rem;
  }

  .legend-value {
    font-size: 1rem;
  }

  .legend-label {
    font-size: 0.7rem;
    letter-spacing: 0.7px;
  }
}

@media (max-width: 600px) {
  .profile-content {
    padding: 16px 14px;
    gap: 8px;
  }

  .hero {
    gap: 10px;
  }

  .avatar {
    width: 56px;
    height: 56px;
    font-size: 1.2rem;
  }

  .name {
    font-size: 1.4rem;
    letter-spacing: 0.6px;
  }

  .elo-pill {
    font-size: 0.75rem;
    padding: 5px 8px;
  }

  .card-label {
    font-size: 0.7rem;
    letter-spacing: 0.5px;
  }

  .streak-number {
    font-size: 1.1rem;
  }

  .card-value {
    font-size: 1.1rem;
  }

  .win-percentage {
    font-size: 1.1rem;
  }

  .legend-value {
    font-size: 0.9rem;
  }

  .legend-label {
    font-size: 0.65rem;
    letter-spacing: 0.6px;
  }
}

@media (max-width: 480px) {
  .profile-content {
    padding: 14px 10px;
    gap: 6px;
  }

  .hero {
    gap: 8px;
  }

  .avatar {
    width: 48px;
    height: 48px;
    font-size: 1rem;
  }

  .name {
    font-size: 1.2rem;
    letter-spacing: 0.5px;
  }

  .elo-pill {
    font-size: 0.7rem;
    padding: 4px 6px;
  }

  .card-label {
    font-size: 0.65rem;
    letter-spacing: 0.4px;
  }

  .streak-number {
    font-size: 1rem;
  }

  .card-value {
    font-size: 1rem;
  }

  .win-percentage {
    font-size: 1rem;
  }

  .legend-value {
    font-size: 0.8rem;
  }

  .legend-label {
    font-size: 0.6rem;
    letter-spacing: 0.5px;
  }

  .donut-chart-container {
    width: 120px;
    height: 120px;
  }
}

@media (max-width: 360px) {
  .profile-content {
    padding: 12px 8px;
    gap: 5px;
  }

  .hero {
    gap: 6px;
  }

  .avatar {
    width: 40px;
    height: 40px;
    font-size: 0.9rem;
  }

  .name {
    font-size: 1.1rem;
    letter-spacing: 0.4px;
  }

  .elo-pill {
    font-size: 0.65rem;
    padding: 3px 5px;
  }

  .card-label {
    font-size: 0.6rem;
    letter-spacing: 0.3px;
  }

  .streak-number {
    font-size: 0.9rem;
  }

  .card-value {
    font-size: 0.9rem;
  }

  .win-percentage {
    font-size: 0.9rem;
  }

  .legend-value {
    font-size: 0.75rem;
  }

  .legend-label {
    font-size: 0.55rem;
    letter-spacing: 0.4px;
  }

  .donut-chart-container {
    width: 100px;
    height: 100px;
  }
}
</style>
