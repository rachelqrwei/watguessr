<template>
  <div class="leaderboard-page">
    <div class="leaderboard-header">
      <h1>LEADERBOARD</h1>
      <p>Compete with the best WatGuessr players worldwide</p>
    </div>

    <div class="leaderboard-controls">
      <div class="search-section">
        <div class="search-input-wrapper">
          <font-awesome-icon icon="search" class="search-icon" />
          <input
            v-model="searchTerm"
            type="text"
            placeholder="Search players..."
            class="search-input"
            @input="handleSearch"
          />
        </div>
      </div>

      <div class="sort-section">
        <label for="sort-select">Sort by:</label>
        <select
          id="sort-select"
          v-model="sortBy"
          class="sort-select"
          @change="handleSort"
        >
          <option value="elo">Highest ELO</option>
          <option value="streakDesc">Highest Streak</option>
          <option value="gamesWonDesc">Most Games Won</option>
          <option value="gamesPlayedDesc">Most Games Played</option>
          <option value="gamesLostDesc">Most Games Lost</option>
          <option value="winRateDesc">Highest Win Rate</option>
          <option value="winRateAsc">Lowest Win Rate</option>
          <option value="createdAtAsc">Oldest Players</option>
          <option value="createdAtDesc">Newest Players</option>
        </select>
      </div>
    </div>

    <div class="leaderboard-content">
      <div v-if="isLoading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>Loading leaderboard...</p>
      </div>

      <div v-else-if="hasError" class="error-state">
        <font-awesome-icon icon="exclamation-triangle" class="error-icon" />
        <p>{{ error }}</p>
        <button @click="retryFetch" class="retry-button">Try Again</button>
      </div>

      <div v-else-if="leaderboard && leaderboard.length === 0" class="empty-state">
        <font-awesome-icon icon="trophy" class="empty-icon" />
        <p>No players found</p>
        <p class="empty-subtitle">Try adjusting your search or filters</p>
      </div>

      <div v-else class="leaderboard-table">
        <div class="table-header">
          <div class="rank-col">Rank</div>
          <div class="player-col">Player</div>
          <div class="elo-col">ELO</div>
          <div class="streak-col">Streak</div>
          <div class="games-col">Games</div>
          <div class="winrate-col">Win Rate</div>
        </div>

        <div
          v-for="(player, index) in leaderboard"
          :key="player.id"
          class="table-row"
          :class="{ 'top-player': getRank(index) <= 3 }"
        >
          <div class="rank-col">
            <div class="rank-badge" :class="getRankClass(getRank(index))">
              {{ getRank(index) }}
            </div>
            <div v-if="getRank(index) <= 3" class="rank-medal">
              {{ getRankMedal(getRank(index)) }}
            </div>
          </div>

          <div class="player-col">
            <div class="player-info">
              <div class="player-name">{{ player.username }}</div>
              <div class="player-id">#{{ player.id.substring(0, 8) }}</div>
            </div>
          </div>

          <div class="elo-col">
            <div class="elo-value">{{ player.elo }}</div>
          </div>

          <div class="streak-col">
            <div class="streak-value" :class="{ 'hot-streak': player.streak >= 5 }">
              <font-awesome-icon v-if="player.streak >= 5" icon="fire" class="streak-icon" />
              {{ player.streak }}
            </div>
          </div>

          <div class="games-col">
            <div class="games-stats">
              <div class="games-played">{{ player.gamesPlayed }} played</div>
              <div class="games-record">
                <span class="wins">{{ player.gamesWon }}W</span>
                <span class="losses">{{ player.gamesLost }}L</span>
              </div>
            </div>
          </div>

          <div class="winrate-col">
            <div class="winrate-value">
              {{ getWinRate(player) }}%
            </div>
            <div class="winrate-bar">
              <div
                class="winrate-fill"
                :style="{ width: `${getWinRate(player)}%` }"
              ></div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="leaderboard && leaderboard.length > 0" class="pagination">
        <button
          @click="previousPage"
          :disabled="currentPage <= 1"
          class="pagination-button"
        >
          <font-awesome-icon icon="chevron-left" />
          Previous
        </button>

        <div class="page-info">
          Page {{ currentPage }}
        </div>

        <button
          @click="nextPage"
          :disabled="!leaderboard || leaderboard.length < (currentQuery.limit || 20)"
          class="pagination-button"
        >
          Next
          <font-awesome-icon icon="chevron-right" />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import type { LeaderboardRequest } from '@/stores/modules/leaderboard'

const store = useStore()

// Local reactive state for form inputs
const searchTerm = ref('')
const sortBy = ref('elo')

// Computed properties from store
const leaderboard = computed(() => store.getters['leaderboard/leaderboard'])
const isLoading = computed(() => store.getters['leaderboard/isLoading'])
const hasError = computed(() => store.getters['leaderboard/hasError'])
const error = computed(() => store.getters['leaderboard/error'])
const currentQuery = computed(() => store.getters['leaderboard/currentQuery'])
const currentPage = computed(() => store.getters['leaderboard/currentPage'])

// Methods
const handleSearch = () => {
  const query: LeaderboardRequest = {
    ...currentQuery.value,
    searchTerm: searchTerm.value,
    offset: 0 // Reset to first page when searching
  }
  store.dispatch('leaderboard/updateQuery', query)
  store.dispatch('leaderboard/fetchLeaderboard', query)
}

const handleSort = () => {
  const query: LeaderboardRequest = {
    ...currentQuery.value,
    sortBy: sortBy.value,
    offset: 0 // Reset to first page when sorting
  }
  store.dispatch('leaderboard/updateQuery', query)
  store.dispatch('leaderboard/fetchLeaderboard', query)
}

const retryFetch = () => {
  store.dispatch('leaderboard/clearError')
  store.dispatch('leaderboard/fetchLeaderboard', currentQuery.value)
}

const nextPage = () => {
  store.dispatch('leaderboard/nextPage')
}

const previousPage = () => {
  store.dispatch('leaderboard/previousPage')
}

const getRank = (index: number) => {
  return (currentQuery.value.offset || 0) + index + 1
}

const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-1'
  if (rank === 2) return 'rank-2'
  if (rank === 3) return 'rank-3'
  return ''
}

const getRankMedal = (rank: number) => {
  if (rank === 1) return '🥇'
  if (rank === 2) return '🥈'
  if (rank === 3) return '🥉'
  return ''
}

const getWinRate = (player: any) => {
  if (player.gamesPlayed === 0) return 0
  return Math.round((player.gamesWon / player.gamesPlayed) * 100)
}

// Initialize
onMounted(() => {
  // Initialize local state from store
  const currentState = store.getters['leaderboard/currentQuery']
  searchTerm.value = currentState.searchTerm || ''
  sortBy.value = currentState.sortBy || 'elo'

  store.dispatch('leaderboard/fetchLeaderboard')
})
</script>

<style scoped>
.leaderboard-page {
  min-height: 100vh;
  background: var(--dark-grey);
  padding: 40px 20px;
}

.leaderboard-header {
  text-align: center;
  margin-bottom: 40px;
}

.leaderboard-header h1 {
  font-size: 3rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.leaderboard-header p {
  font-size: 1.2rem;
  color: var(--light-grey);
  margin: 0;
}

.leaderboard-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto 30px;
  gap: 20px;
}

.search-section {
  flex: 1;
  max-width: 400px;
}

.search-input-wrapper {
  position: relative;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--light-grey);
  font-size: 1rem;
}

.search-input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: var(--white);
  font-size: 1rem;
  transition: all 0.3s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--yellow);
  box-shadow: 0 0 0 2px rgba(255, 203, 59, 0.2);
}

.search-input::placeholder {
  color: var(--light-grey);
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-section label {
  color: var(--white);
  font-weight: 600;
  white-space: nowrap;
}

.sort-select {
  padding: 12px 40px 12px 15px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: var(--white);
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  appearance: none;
  background-image: url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6,9 12,15 18,9'%3e%3c/polyline%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px;
}

.sort-select:focus {
  outline: none;
  border-color: var(--yellow);
  box-shadow: 0 0 0 2px rgba(255, 203, 59, 0.2);
}

.leaderboard-content {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: var(--white);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 203, 59, 0.3);
  border-top: 3px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-icon,
.empty-icon {
  font-size: 3rem;
  color: var(--yellow);
  margin-bottom: 20px;
}

.retry-button {
  background: var(--yellow);
  color: var(--dark-grey);
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 20px;
}

.retry-button:hover {
  background: var(--yellow);
}

.empty-subtitle {
  color: var(--light-grey);
  font-size: 1rem;
  margin-top: 10px;
}

.leaderboard-table {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 15px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.table-header {
  display: grid;
  grid-template-columns: 80px 1fr 100px 100px 140px 120px;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  font-weight: 700;
  color: var(--white);
  text-transform: uppercase;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
}

.table-row {
  display: grid;
  grid-template-columns: 80px 1fr 100px 100px 140px 120px;
  gap: 20px;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.table-row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.table-row:last-child {
  border-bottom: none;
}

.top-player {
  background: rgba(255, 203, 59, 0.1);
  border-left: 4px solid var(--yellow);
}

.rank-col {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rank-badge {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: var(--white);
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border-color: #FFD700;
  color: var(--dark-grey);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
  border-color: #C0C0C0;
  color: var(--dark-grey);
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #CD7F32, #A0522D);
  border-color: #CD7F32;
  color: var(--white);
}

.rank-medal {
  font-size: 1.2rem;
}

.player-col {
  display: flex;
  align-items: center;
}

.player-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--white);
  margin-bottom: 2px;
}

.player-id {
  font-size: 0.8rem;
  color: var(--light-grey);
  font-family: monospace;
}

.elo-col {
  display: flex;
  align-items: center;
}

.elo-value {
  font-size: 1.2rem;
  font-weight: 900;
  color: var(--yellow);
}

.streak-col {
  display: flex;
  align-items: center;
}

.streak-value {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--white);
}

.streak-value.hot-streak {
  color: #ff6b35;
}

.streak-icon {
  color: #ff6b35;
  font-size: 1rem;
}

.games-col {
  display: flex;
  align-items: center;
}

.games-stats {
  text-align: left;
}

.games-played {
  font-size: 0.9rem;
  color: var(--light-grey);
  margin-bottom: 2px;
}

.games-record {
  display: flex;
  gap: 8px;
}

.wins {
  color: #4CAF50;
  font-weight: 700;
}

.losses {
  color: #f44336;
  font-weight: 700;
}

.winrate-col {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.winrate-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--white);
  margin-bottom: 5px;
}

.winrate-bar {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
}

.winrate-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, var(--yellow));
  transition: width 0.3s ease;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
  padding: 20px;
}

.pagination-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  color: var(--white);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-button:hover:not(:disabled) {
  background: var(--yellow);
  color: var(--dark-grey);
  border-color: var(--yellow);
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: var(--white);
  font-weight: 600;
}

@media (max-width: 768px) {
  .leaderboard-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .search-section {
    max-width: none;
  }

  .sort-section {
    justify-content: space-between;
  }

  .table-header,
  .table-row {
    grid-template-columns: 60px 1fr 80px 80px 100px 80px;
    gap: 10px;
    padding: 15px;
  }

  .leaderboard-header h1 {
    font-size: 2rem;
  }

  .games-played {
    display: none;
  }
}
</style>
