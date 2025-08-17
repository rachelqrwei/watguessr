<template>
  <div class="leaderboard-page">
    <!-- Authentication Required Message -->
    <div v-if="!isAuthenticated || !userToken || !hasUserRole" class="auth-required">
      <div class="auth-message">
        <font-awesome-icon icon="lock" class="lock-icon" />
        <h2>Authentication Required</h2>
        <p v-if="!isAuthenticated">You must be logged in to view the leaderboard</p>
        <p v-else-if="!userToken">Your session has expired. Please log in again.</p>
        <p v-else-if="!hasUserRole">You don't have permission to access this resource.</p>
        <div class="auth-actions">
          <button @click="showLoginModal = true" class="login-btn">Log In</button>
          <button @click="showSignupModal = true" class="signup-btn">Sign Up</button>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-else-if="isLoading" class="loading-state">
      <div class="loading-spinner"></div>
      <p>Loading leaderboard...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="hasError" class="error-state">
      <font-awesome-icon icon="exclamation-triangle" class="error-icon" />
      <p>{{ error }}</p>
      <div class="error-actions">
        <button @click="retryFetch" class="retry-button">Try Again</button>
        <button v-if="isAuthError" @click="handleReauth" class="reauth-button">Re-authenticate</button>
      </div>
    </div>

    <!-- Leaderboard Content (only shown when fully authenticated) -->
    <div v-else-if="isAuthenticated && userToken && hasUserRole" class="leaderboard-content">
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

      <!-- Leaderboard Table -->
      <div v-if="leaderboard && leaderboard.length > 0" class="leaderboard-table">
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
          @click="goToProfile(player.id)"
          style="cursor: pointer;"
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

      <!-- Empty State -->
      <div v-else class="empty-state">
        <font-awesome-icon icon="trophy" class="empty-icon" />
        <p>No players found</p>
        <p class="empty-subtitle">Try adjusting your search or filters</p>
      </div>

      <!-- Pagination -->
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
          :disabled="!hasNextPage"
          class="pagination-button"
        >
          Next
          <font-awesome-icon icon="chevron-right" />
        </button>
      </div>
    </div>

    <!-- Auth Modals -->
    <AuthModalManager
      :showLogin="showLoginModal"
      :showSignUp="showSignupModal"
      @closeLogin="showLoginModal = false"
      @closeSignUp="showSignupModal = false"
      @openLogin="() => { showLoginModal = true; showSignupModal = false }"
      @openSignUp="() => { showSignupModal = true; showLoginModal = false }"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useStore } from 'vuex'
import type { LeaderboardRequest } from '@/stores/modules/leaderboard/types.ts'
import { RouterLink, useRouter } from 'vue-router'
import AuthModalManager from '@/views/auth/AuthModalManager.vue'

const store = useStore()
const router = useRouter()

// Modal state
const showLoginModal = ref(false)
const showSignupModal = ref(false)

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
const hasNextPage = computed(() => store.getters['leaderboard/hasNextPage'])

// User authentication state
const isAuthenticated = computed(() => store.getters['user/isAuthenticated'])
const currentUser = computed(() => store.getters['user/getCurrentUser'])
const userToken = computed(() => store.getters['user/getToken'])

// Role checking - extract role from JWT token
const userRole = computed(() => {
  if (!userToken.value) return null

  try {
    // Decode JWT token to extract role information
    const tokenParts = userToken.value.split('.')
    if (tokenParts.length === 3) {
      const payload = JSON.parse(atob(tokenParts[1]))
      // Look for 'role' claim (single string) instead of 'authorities' (array)
      return payload.role || null
    }
  } catch (error) {
    console.error('Error decoding JWT token:', error)
  }
  return null
})

// Check if user has ROLE_USER
const hasUserRole = computed(() => {
  return userRole.value === 'ROLE_USER' || userRole.value === 'USER'
})

// Check if error is authentication-related
const isAuthError = computed(() => {
  const errorMsg = error.value?.toLowerCase() || ''
  return errorMsg.includes('authentication') ||
    errorMsg.includes('unauthorized') ||
    errorMsg.includes('forbidden') ||
    errorMsg.includes('login') ||
    errorMsg.includes('403') ||
    errorMsg.includes('401')
})

// Methods
const handleSearch = () => {
  if (!isAuthenticated.value || !userToken.value || !hasUserRole.value) return

  const query: LeaderboardRequest = {
    ...currentQuery.value,
    searchTerm: searchTerm.value,
    offset: 0
  }
  store.dispatch('leaderboard/updateQuery', query)
  store.dispatch('leaderboard/fetchLeaderboard', query)
}

const handleSort = () => {
  if (!isAuthenticated.value || !userToken.value || !hasUserRole.value) return

  const query: LeaderboardRequest = {
    ...currentQuery.value,
    sortBy: sortBy.value,
    offset: 0
  }
  store.dispatch('leaderboard/updateQuery', query)
  store.dispatch('leaderboard/fetchLeaderboard', query)
}

const retryFetch = () => {
  if (!isAuthenticated.value || !userToken.value || !hasUserRole.value) return

  store.dispatch('leaderboard/clearError')
  store.dispatch('leaderboard/fetchLeaderboard', currentQuery.value)
}

const handleReauth = () => {
  store.dispatch('user/logout')
  showLoginModal.value = true
}

const handleLogout = () => {
  store.dispatch('user/logout')
  router.push('/')
}

const nextPage = () => {
  if (!isAuthenticated.value || !userToken.value || !hasUserRole.value) return
  store.dispatch('leaderboard/nextPage')
}

const previousPage = () => {
  if (!isAuthenticated.value || !userToken.value || !hasUserRole.value) return
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

const goToProfile = (userId: string) => {
  if (!userId || !isAuthenticated.value || !userToken.value || !hasUserRole.value) return
  router.push({ name: 'profile', params: { userId: userId } })
}

// Watch for authentication changes
watch([isAuthenticated, userToken, hasUserRole], ([newAuth, newToken, newRole]) => {
  if (newAuth && newToken && newRole) {
    // User is fully authenticated with proper role, fetch leaderboard
    store.dispatch('leaderboard/fetchLeaderboard')
  }
})

// Initialize
onMounted(() => {
  // Only initialize if user is fully authenticated with proper role
  if (isAuthenticated.value && userToken.value && hasUserRole.value) {
    const currentState = store.getters['leaderboard/currentQuery']
    searchTerm.value = currentState.searchTerm || ''
    sortBy.value = currentState.sortBy || 'elo'

    store.dispatch('leaderboard/fetchLeaderboard')
  }
})
</script>

<style scoped>
.leaderboard-page {
  min-height: 100vh;
  background: var(--dark-grey);
  padding: 40px 20px;
}

.auth-required {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--dark-grey);
  padding: 40px 20px;
}

.auth-message {
  text-align: center;
  padding: 40px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--white);
  max-width: 600px;
  width: 100%;
}

.lock-icon {
  font-size: 3rem;
  color: var(--yellow);
  margin-bottom: 20px;
}

.auth-message h2 {
  font-size: 2rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.auth-message p {
  font-size: 1.1rem;
  color: var(--light-grey);
  margin-bottom: 20px;
}

.auth-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.login-btn, .signup-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  background: var(--yellow);
  color: var(--dark-grey);
}

.login-btn:hover, .signup-btn:hover {
  background: #e6b800;
  transform: translateY(-2px);
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
}

.search-input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--white);
  font-size: 1rem;
}

.search-input::placeholder {
  color: var(--light-grey);
}

.search-input:focus {
  outline: none;
  border-color: var(--yellow);
  box-shadow: 0 0 0 2px rgba(255, 193, 7, 0.2);
}

.sort-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-section label {
  color: var(--white);
  font-weight: 600;
}

.sort-select {
  padding: 10px 15px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--white);
  font-size: 0.9rem;
  cursor: pointer;
}

.sort-select:focus {
  outline: none;
  border-color: var(--yellow);
}

.leaderboard-content {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--light-grey);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top: 3px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--white);
}

.error-icon {
  font-size: 3rem;
  color: #ff4757;
  margin-bottom: 20px;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 20px;
}

.retry-button, .reauth-button {
  background: var(--yellow);
  color: var(--dark-grey);
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-button:hover, .reauth-button:hover {
  background: #e6b800;
  transform: translateY(-2px);
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: var(--light-grey);
}

.empty-icon {
  font-size: 4rem;
  color: var(--yellow);
  margin-bottom: 20px;
}

.empty-subtitle {
  font-size: 0.9rem;
  margin-top: 10px;
}

.leaderboard-table {
  background: rgba(255, 255, 255, 0.02);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.table-header {
  display: grid;
  grid-template-columns: 80px 2fr 1fr 1fr 1fr 1fr;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  font-weight: 700;
  color: var(--white);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.table-row {
  display: grid;
  grid-template-columns: 80px 2fr 1fr 1fr 1fr 1fr;
  gap: 20px;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.3s ease;
  color: var(--white);
}

.table-row:hover {
  background: rgba(255, 255, 255, 0.02);
  transform: translateX(5px);
}

.table-row.top-player {
  background: linear-gradient(135deg, rgba(255, 193, 7, 0.1), rgba(255, 193, 7, 0.05));
  border-left: 4px solid var(--yellow);
}

.rank-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.rank-badge {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.1rem;
  background: rgba(255, 255, 255, 0.1);
  color: var(--white);
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: var(--dark-grey);
}

.rank-2 {
  background: linear-gradient(135deg, #c0c0c0, #e5e5e5);
  color: var(--dark-grey);
}

.rank-3 {
  background: linear-gradient(135deg, #cd7f32, #daa520);
  color: var(--white);
}

.rank-medal {
  font-size: 1.5rem;
}

.player-col {
  display: flex;
  align-items: center;
}

.player-name {
  font-weight: 600;
  font-size: 1.1rem;
}

.elo-col, .streak-col, .games-col, .winrate-col {
  display: flex;
  align-items: center;
  justify-content: center;
}

.elo-value {
  font-weight: 700;
  color: var(--yellow);
  font-size: 1.1rem;
}

.streak-value {
  display: flex;
  align-items: center;
  gap: 5px;
  font-weight: 600;
}

.streak-value.hot-streak {
  color: #ff6b35;
}

.streak-icon {
  color: #ff6b35;
}

.games-stats {
  text-align: center;
}

.games-played {
  font-size: 0.9rem;
  color: var(--light-grey);
  margin-bottom: 5px;
}

.games-record {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.wins {
  color: #4caf50;
  font-weight: 600;
}

.losses {
  color: #f44336;
  font-weight: 600;
}

.winrate-value {
  font-weight: 700;
  margin-bottom: 5px;
}

.winrate-bar {
  width: 60px;
  height: 4px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
  overflow: hidden;
}

.winrate-fill {
  height: 100%;
  background: var(--yellow);
  transition: width 0.3s ease;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 40px;
}

.pagination-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: var(--white);
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination-button:hover:not(:disabled) {
  background: var(--yellow);
  color: var(--dark-grey);
  border-color: var(--yellow);
  transform: translateY(-2px);
}

.pagination-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: var(--light-grey);
  font-weight: 600;
}

@media (max-width: 768px) {
  .leaderboard-page {
    padding: 20px 10px;
  }

  .leaderboard-header h1 {
    font-size: 2rem;
  }

  .leaderboard-controls {
    flex-direction: column;
    gap: 15px;
  }

  .search-section {
    max-width: 100%;
  }

  .table-header,
  .table-row {
    grid-template-columns: 60px 1.5fr 1fr 1fr 1fr 1fr;
    gap: 10px;
    padding: 15px 10px;
  }

  .rank-badge {
    width: 30px;
    height: 30px;
    font-size: 0.9rem;
  }

  .player-name {
    font-size: 1rem;
  }

  .elo-value,
  .streak-value {
    font-size: 0.9rem;
  }

  .games-stats {
    font-size: 0.8rem;
  }

  .winrate-bar {
    width: 40px;
  }
}
</style>
