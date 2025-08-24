<template>
  <div class="leaderboard-background" aria-hidden="true"></div>
  <div class="leaderboard-page">
    <div class="leaderboard-header">
      <h1>LEADERBOARD</h1>
    </div>

    <div class="leaderboard-controls">
      <div class="search-section">
        <div class="search-input-wrapper">
          <font-awesome-icon icon="search" class="search-icon" />
          <input
            v-model="searchTerm"
            type="text"
            placeholder="Search for users..."
            class="search-input"
            @input="handleInput"
            @keyup.enter="handleSearch"
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

      <div v-else>
        <div class="leaderboard-table">
        <div class="table-header">
          <div class="rank-col">Rank</div>
          <div class="player-col">Player</div>
          <div class="elo-col">ELO</div>
          <div class="streak-col">Streak</div>
          <div class="games-col">Games</div>
          <div class="winrate-col">Win Rate</div>
        </div>

        <div class="table-body">
          <div
          v-for="(player, index) in limitedLeaderboard"
          :key="player.id"
          class="table-row"
          :class="{ 'top-player': getRank(index) <= 3 }"
          @click="goToProfile(player.id)"
          style="cursor: pointer;"
          >
          <div class="rank-col">
            <div class="rank-badge" :class="getRankClass(getRank(index))">
              {{ getRank(index) }}
            </div>
          </div>

          <div class="player-col">
            <div class="player-info">
              <div class="player-name">{{ player.username }}</div>
            </div>
          </div>

          <div class="elo-col">
            <div class="elo-value">{{ player.elo }}</div>
          </div>

          <div class="streak-col">
            <div class="streak-value" :class="{ 'hot-streak': player.streak >= 5 }">
              <img v-if="player.streak >= 5" src="../assets/images/Header/streak-icon.png" alt="Streak" class="streak-icon" />
              {{ player.streak }}
            </div>
          </div>

          <div class="games-col">
            <div class="games-stats">
              <div class="games-played">{{ player.gamesPlayed }} played</div>
              <div class="games-record">
                <span class="wins">{{ player.gamesWon }}W</span>
                /
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
        </div>

        <div v-if="limitedLeaderboard && limitedLeaderboard.length > 0" class="pagination">
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
            :disabled="!limitedLeaderboard || limitedLeaderboard.length < 50"
            class="pagination-button"
          >
            Next
            <font-awesome-icon icon="chevron-right" />
          </button>
        </div>
      </div>


    </div>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { RouterLink } from 'vue-router'

export default {
  components: {
    RouterLink
  },

  data() {
    return {
      router: this.$router,
      searchTerm: '',
      sortBy: 'elo',
      debounceTimer: null
    }
  },

  computed: {
    ...mapGetters({
      leaderboard: 'leaderboard/leaderboard',
      isLoading: 'leaderboard/isLoading',
      hasError: 'leaderboard/hasError',
      error: 'leaderboard/error',
      currentQuery: 'leaderboard/currentQuery',
      currentPage: 'leaderboard/currentPage'
    }),
    limitedLeaderboard() {
      if (!this.leaderboard) return []
      return this.leaderboard.slice(0, 50)
    }
  },

  methods: {
    ...mapActions({
      updateQuery: 'leaderboard/updateQuery',
      fetchLeaderboard: 'leaderboard/fetchLeaderboard',
      clearError: 'leaderboard/clearError'
    }),

    handleInput() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }
      this.debounceTimer = setTimeout(() => {
        this.handleSearch()
      }, 400)
    },

    handleSearch() {
      const query = {
        ...this.currentQuery,
        searchTerm: this.searchTerm,
        offset: 0,
        limit: 50
      }
      this.updateQuery(query)
      this.fetchLeaderboard(query)
    },

    handleSort() {
      const query = {
        ...this.currentQuery,
        sortBy: this.sortBy,
        offset: 0, // Reset to first page when sorting
        limit: 50
      }
      this.updateQuery(query)
      this.fetchLeaderboard(query)
    },

    retryFetch() {
      this.clearError()
      this.fetchLeaderboard(this.currentQuery)
    },

    previousPage() {
      const pageSize = 50
      const newOffset = Math.max(0, (this.currentPage - 2) * pageSize)
      const query = {
        ...this.currentQuery,
        offset: newOffset,
        limit: pageSize
      }
      this.updateQuery(query)
      this.fetchLeaderboard(query)
    },

    nextPage() {
      const pageSize = 50
      const newOffset = this.currentPage * pageSize
      const query = {
        ...this.currentQuery,
        offset: newOffset,
        limit: pageSize
      }
      this.updateQuery(query)
      this.fetchLeaderboard(query)
    },

    goToProfile(userId) {
      if (!userId) return
      this.router.push({ name: 'profile', params: { userId } })
    },

    getRank(index) {
      const pageSize = 50
      return (this.currentPage - 1) * pageSize + index + 1
    },

    getRankClass(rank) {
      if (rank === 1) return 'rank-1'
      if (rank === 2) return 'rank-2'
      if (rank === 3) return 'rank-3'
      return ''
    },



    getWinRate(player) {
      if (!player || player.gamesPlayed === 0) return 0
      return Math.round((player.gamesWon / player.gamesPlayed) * 100)
    }
  },

  mounted() {
    // Initial fetch
    this.fetchLeaderboard(this.currentQuery)
  }
}
</script>

<style scoped>
.leaderboard-background {
  position: fixed;
  inset: 0;
  background: var(--dark-grey);
  z-index: -1;
}

.leaderboard-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/ProfilePage.png') center top / cover no-repeat;
  opacity: 0.8;
  pointer-events: none;
}

.leaderboard-page {
  min-height: calc(100vh - 80px);
  position: relative;
  padding: 40px 20px;
}

.leaderboard-header {
  text-align: center;
  margin-bottom: 40px;
}

.leaderboard-header h1 {
  font-size: 1.8rem;
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
  font-family: "Red Hat Text", sans-serif;
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

/* search button removed */

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

.leaderboard-table .table-body {
  display: flex;
  flex-direction: column;
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
  font-family: "Red Hat Text", sans-serif;
}

.leaderboard-table {
  background: rgba(42, 42, 44, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow-x: auto;
  backdrop-filter: blur(8px);
  padding: 16px;
  width: 100%;
}

.table-header {
  display: grid;
  grid-template-columns: 80px 1fr 100px 140px 140px 120px;
  gap: 12px;
  padding: 16px 12px 20px 12px;
  color: var(--white);
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 8px;
  align-items: center;
  font-weight: 900;
  width: 100%;
}

.table-row {
  display: grid;
  grid-template-columns: 80px 1fr 100px 140px 140px 120px;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
  margin: 8px 0;
  width: 100%;
}

.table-row:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.07);
}





.rank-col {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 80px;
}

.table-header .rank-col {
  justify-content: center;
  padding-left: 0;
}

.table-header .player-col {
  justify-content: flex-start;
  padding-left: 0;
}

.table-header .elo-col {
  justify-content: center;
}

.table-header .streak-col {
  justify-content: center;
}

.table-header .games-col {
  justify-content: flex-start;
  padding-left: 0;
}

.table-header .winrate-col {
  justify-content: center;
}

.rank-badge {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  color: var(--white);
  background: rgba(255, 255, 255, 0.2);
  margin-left: 8px;
}

.rank-badge.rank-1 {
  background: #FFD700;
  color: var(--dark-grey);
}

.rank-badge.rank-2 {
  background: #C0C0C0;
  color: var(--dark-grey);
}

.rank-badge.rank-3 {
  background: #CD7F32;
  color: var(--white);
}



.player-col {
  display: flex;
  align-items: center;
  min-width: 150px;
}

.player-name {
  font-size: 1rem;
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
  justify-content: center;
  min-width: 100px;
}

.elo-value {
  font-size: 1.1rem;
  font-weight: 900;
  color: var(--yellow);
}

.streak-col {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 140px;
}

.streak-value {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 1rem;
  font-weight: 700;
  background: var(--player-1-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.streak-value.hot-streak {
  background: var(--player-1-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.streak-icon {
  height: 16px;
  width: 14px;
}

.games-col {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  min-width: 140px;
}

.games-stats {
  text-align: left;
}

.games-played {
  font-size: 0.9rem;
  color: var(--light-grey);
  margin-bottom: 2px;
  font-family: "Red Hat Text", sans-serif;
}

.games-record {
  display: flex;
  gap: 8px;
}

.wins {
  color: #B6FF7F;
  font-weight: 700;
}

.losses {
  color: #FF7F7F;
  font-weight: 700;
}

.winrate-col {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-width: 120px;
}

.winrate-value {
  font-size: 1rem;
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

/* Desktop/Laptop screens - restore original styling */
@media (min-width: 1024px) {
  .leaderboard-page {
    padding: 40px 20px;
    margin-top: 0;
  }
  
  .leaderboard-header h1 {
    font-size: 1.8rem;
    letter-spacing: 1px;
  }
  
  .leaderboard-controls {
    flex-direction: row;
    align-items: center;
    gap: 20px;
    margin-bottom: 30px;
  }
  
  .search-section {
    max-width: 400px;
  }
  
  .sort-section {
    justify-content: flex-end;
    gap: 10px;
  }
  
  .sort-section label {
    font-size: 1rem;
  }
  
  .sort-select {
    padding: 12px 40px 12px 15px;
    font-size: 1rem;
  }
  
  .table-header {
    gap: 12px;
    padding: 16px 12px 20px 12px;
    font-size: 0.85rem;
    letter-spacing: 1px;
  }
  
  .table-row {
    gap: 12px;
    padding: 12px;
  }
  
  .rank-badge {
    width: 28px;
    height: 28px;
    font-size: 0.85rem;
    margin-left: 8px;
  }
  
  .player-name {
    font-size: 1rem;
  }
  
  .elo-value {
    font-size: 1.1rem;
  }
  
  .streak-value {
    font-size: 1rem;
  }
  
  .streak-icon {
    height: 16px;
    width: 14px;
  }
  
  .games-record {
    gap: 8px;
    font-size: 0.9rem;
  }
  
  .winrate-value {
    font-size: 1rem;
  }
  
  .winrate-bar {
    width: 100%;
    height: 4px;
  }
  
  .pagination {
    flex-direction: row;
    gap: 20px;
    margin-top: 12px;
  }
  
  .pagination-button {
    padding: 12px 20px;
    font-size: 1rem;
    min-width: auto;
    height: auto;
  }
  
  .page-info {
    font-size: 1rem;
    order: 0;
    margin-bottom: 0;
  }
}

.winrate-fill {
  height: 100%;
  background: linear-gradient(90deg, #B6FF7F, var(--yellow));
  transition: width 0.3s ease;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 12px;
  padding: 12px;
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

  .search-input {
    background: rgba(60, 60, 62, 0.8);
  }

  .sort-select {
    background: rgba(60, 60, 62, 0.8);
  }

  .sort-section {
    justify-content: space-between;
  }

  .table-header,
  .table-row {
    grid-template-columns: 60px 1fr 80px 100px 100px 80px;
    gap: 10px;
    padding: 12px;
  }

  .leaderboard-header h1 {
    font-size: 1.8rem;
  }

  .games-played {
    display: none;
  }
}

/* Enhanced Responsive Design */
@media (max-width: 768px) {
  .leaderboard-page {
    padding: 30px 16px;
    margin-top: 80px;
  }

  .leaderboard-header {
    margin-bottom: 30px;
  }

  .leaderboard-header h1 {
    font-size: 1.6rem;
    letter-spacing: 0.8px;
  }

  .leaderboard-controls {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
    margin-bottom: 24px;
  }

  .search-section {
    max-width: none;
  }

  .sort-section {
    justify-content: space-between;
    gap: 12px;
  }

  .sort-section label {
    font-size: 0.9rem;
  }

  .sort-select {
    padding: 10px 35px 10px 12px;
    font-size: 0.9rem;
  }

  .table-header,
  .table-row {
    gap: 8px;
    padding: 10px 8px;
  }

    .table-header {
    font-size: 0.75rem;
    letter-spacing: 0.5px;
    padding: 12px 8px 16px 8px;
    width: fit-content;
  }
  
  .table-row {
    width: fit-content;
  }
  
  .rank-badge {
    width: 24px;
    height: 24px;
    font-size: 0.8rem;
    margin-left: 4px;
  }

  .player-name {
    font-size: 0.9rem;
  }

  .elo-value {
    font-size: 1rem;
  }

  .streak-value {
    font-size: 0.9rem;
  }

  .streak-icon {
    height: 14px;
    width: 12px;
  }

  .games-record {
    gap: 6px;
    font-size: 0.8rem;
  }

    .winrate-value {
    font-size: 0.9rem;
  }
  
  .winrate-bar {
    width: 60%;
    height: 3px;
  }
  
  .pagination {
    gap: 16px;
    margin-top: 16px;
  }

  .pagination-button {
    padding: 10px 16px;
    font-size: 0.9rem;
  }

  .page-info {
    font-size: 0.9rem;
  }

  /* Pagination responsive adjustments */
  .pagination-button {
    min-width: 100px;
    justify-content: center;
  }
}

@media (max-width: 600px) {
  .leaderboard-page {
    padding: 24px 12px;
    margin-top: 80px;
  }

  .leaderboard-header h1 {
    font-size: 1.4rem;
    letter-spacing: 0.6px;
  }

  .leaderboard-controls {
    gap: 12px;
    margin-bottom: 20px;
  }

  .search-input {
    padding: 10px 15px 10px 40px;
    font-size: 0.9rem;
  }

  .search-icon {
    left: 12px;
    font-size: 0.9rem;
  }

  .sort-select {
    padding: 8px 30px 8px 10px;
    font-size: 0.85rem;
  }

  .table-header,
  .table-row {
    gap: 6px;
    padding: 8px 6px;
  }

    .table-header {
    font-size: 0.7rem;
    letter-spacing: 0.3px;
    padding: 10px 6px 12px 6px;
    width: fit-content;
  }
  
  .table-row {
    width: fit-content;
  }
  
  .rank-badge {
    width: 22px;
    height: 22px;
    font-size: 0.75rem;
    margin-left: 2px;
  }

  .player-name {
    font-size: 0.85rem;
  }

  .elo-value {
    font-size: 0.9rem;
  }

  .streak-value {
    font-size: 0.85rem;
  }

  .streak-icon {
    height: 12px;
    width: 10px;
  }

  .games-record {
    gap: 4px;
    font-size: 0.75rem;
  }

    .winrate-value {
    font-size: 0.85rem;
  }
  
  .winrate-bar {
    width: 60%;
    height: 3px;
  }
  
  .pagination {
    gap: 12px;
    margin-top: 12px;
  }

  .pagination-button {
    padding: 8px 12px;
    font-size: 0.85rem;
    min-width: 90px;
  }

  .page-info {
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .leaderboard-page {
    padding: 20px 8px;
    margin-top: 80px;
  }

  .leaderboard-header {
    margin-bottom: 24px;
  }

  .leaderboard-header h1 {
    font-size: 1.2rem;
    letter-spacing: 0.5px;
  }

  .leaderboard-controls {
    gap: 10px;
    margin-bottom: 16px;
  }

  .search-input {
    padding: 8px 12px 8px 35px;
    font-size: 0.85rem;
    border-radius: 8px;
  }

  .search-icon {
    left: 10px;
    font-size: 0.85rem;
  }

  .sort-section {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .sort-section label {
    font-size: 0.85rem;
    text-align: center;
  }

  .sort-select {
    padding: 8px 25px 8px 8px;
    font-size: 0.8rem;
    border-radius: 8px;
  }

  .table-header,
  .table-row {
    gap: 4px;
    padding: 6px 4px;
  }

    .table-header {
    font-size: 0.65rem;
    letter-spacing: 0.2px;
    padding: 8px 4px 10px 4px;
    width: fit-content;
  }
  
  .table-row {
    width: fit-content;
  }
  
  .rank-badge {
    width: 20px;
    height: 20px;
    font-size: 0.7rem;
    margin-left: 0;
  }

  .player-name {
    font-size: 0.8rem;
  }

  .elo-value {
    font-size: 0.85rem;
  }

  .streak-value {
    font-size: 0.8rem;
  }

  .streak-icon {
    height: 10px;
    width: 8px;
  }

  .games-record {
    gap: 3px;
    font-size: 0.7rem;
  }

    .winrate-value {
    font-size: 0.8rem;
  }
  
  .winrate-bar {
    width: 60%;
    height: 2px;
  }
  
  .pagination {
    flex-direction: row;
    gap: 12px;
    margin-top: 16px;
  }

  .pagination-button {
    padding: 10px 16px;
    font-size: 0.8rem;
    justify-content: center;
    min-width: 100px;
    height: 44px;
  }

  .page-info {
    font-size: 0.8rem;
    order: 0;
    margin-bottom: 0;
  }
}

@media (max-width: 360px) {
  .leaderboard-page {
    padding: 16px 6px;
    margin-top: 80px;
  }

  .leaderboard-header h1 {
    font-size: 1.1rem;
    letter-spacing: 0.4px;
  }

  .search-input {
    padding: 6px 10px 6px 30px;
    font-size: 0.8rem;
  }

  .search-icon {
    left: 8px;
    font-size: 0.8rem;
  }

  .sort-select {
    padding: 6px 20px 6px 6px;
    font-size: 0.75rem;
  }

  .table-header,
  .table-row {
    gap: 3px;
    padding: 5px 3px;
  }

    .table-header {
    font-size: 0.6rem;
    padding: 6px 3px 8px 3px;
    width: fit-content;
  }
  
  .table-row {
    width: fit-content;
  }
  
  .rank-badge {
    width: 18px;
    height: 18px;
    font-size: 0.65rem;
  }

  .player-name {
    font-size: 0.75rem;
  }

  .elo-value {
    font-size: 0.8rem;
  }

  .streak-value {
    font-size: 0.75rem;
  }

  .streak-icon {
    height: 8px;
    width: 7px;
  }

  .games-record {
    font-size: 0.65rem;
  }

    .winrate-value {
    font-size: 0.75rem;
  }
  
  .winrate-bar {
    width: 60%;
    height: 2px;
  }
  
  .pagination-button {
    padding: 8px 12px;
    font-size: 0.75rem;
    min-width: 100px;
    height: 40px;
  }

  .page-info {
    font-size: 0.75rem;
  }
}

/* Landscape orientation adjustments for mobile */
@media (max-height: 500px) and (orientation: landscape) {
  .leaderboard-page {
    padding: 16px 20px;
  }

  .leaderboard-header {
    margin-bottom: 20px;
  }

  .leaderboard-header h1 {
    font-size: 1.4rem;
    margin-bottom: 5px;
  }

  .leaderboard-controls {
    margin-bottom: 16px;
  }

  .table-header,
  .table-row {
    padding: 8px 6px;
  }

  .pagination {
    margin-top: 8px;
    gap: 16px;
  }

  .pagination-button {
    min-width: 90px;
    height: 40px;
  }
}

/* High DPI displays */
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
  .search-icon,
  .streak-icon {
    image-rendering: -webkit-optimize-contrast;
    image-rendering: crisp-edges;
  }
}

/* Dark mode support for systems that prefer it */
@media (prefers-color-scheme: dark) {
  /* Keep original colors and design */
}

/* Reduced motion for accessibility */
@media (prefers-reduced-motion: reduce) {
  .table-row,
  .search-input,
  .sort-select,
  .pagination-button,
  .winrate-fill {
    transition: none;
  }

  .table-row:hover {
    transform: none;
  }

  .loading-spinner {
    animation: none;
  }
}

/* Print styles */
@media print {
  .leaderboard-background,
  .leaderboard-controls,
  .pagination {
    display: none;
  }

  .leaderboard-page {
    padding: 0;
    min-height: auto;
  }

  .leaderboard-table {
    background: white;
    border: 1px solid #ccc;
    box-shadow: none;
  }

  .table-header,
  .table-row {
    color: black;
    border: 1px solid #ddd;
  }

  .rank-badge {
    background: #f0f0f0;
    color: black;
  }

  .elo-value {
    color: #333;
  }

  .streak-value {
    color: #333;
    -webkit-text-fill-color: #333;
  }
}
</style>
