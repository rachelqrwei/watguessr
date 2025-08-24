<template>
  <div class="search-container">
    <div class="search-bar">
      <input
        v-model="searchQuery"
        @input="onSearchInput"
        @focus="showSearchResults = true"
        @blur="onSearchBlur"
        type="text"
        placeholder="Search for users..."
        class="search-input"
      />
      <font-awesome-icon icon="search" class="search-icon" />
    </div>

    <div v-if="showSearchResults && searchResults.length > 0" class="search-dropdown">
      <div
        v-for="user in searchResults"
        :key="user.id"
        @mousedown="selectUser(user)"
        class="search-result-item"
      >
        <div class="result-avatar" :style="{ background: colorFor(user.username).bg, color: colorFor(user.username).fg }">{{ user.username.charAt(0).toUpperCase() }}</div>
        <div class="result-info">
          <div class="result-username">{{ user.username }}</div>
          <div class="result-elo">{{ user.elo }} ELO</div>
        </div>
      </div>
    </div>

    <div
      v-if="showSearchResults && searchResults.length === 0 && hasSearched && searchQuery.trim()"
      class="search-dropdown"
    >
      <div class="no-results-item">
        <font-awesome-icon icon="search" class="no-results-icon" />
        <div class="no-results-text">No users found</div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapMutations } from 'vuex'
import { colorPairFromName } from '@/utils/color'

export default {
  name: 'SearchBar',
  data() {
    return {
      searchQuery: '',
      searchResults: [],
      showSearchResults: false,
      searchTimeout: null,
      hasSearched: false,
    }
  },
  methods: {
    ...mapMutations('profile', ['SET_PROFILE_USER_ID']),
    colorFor(username) {
      return colorPairFromName(username, {
        bgSaturation: 90,
        bgLightness: 80,
        fgSaturation: 100,
        fgLightness: 30,
        fgHueShift: -12,
      })
    },

    async searchUsers(query) {
      if (!query.trim()) {
        return []
      }

      try {
        const params = new URLSearchParams()
        params.set('searchTerm', query.trim())
        params.set('limit', '5')

        const response = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/user/leaderboard?${params.toString()}`,
          {
            credentials: 'include',
          },
        )
        if (!response.ok) throw new Error(`Search failed: ${response.status}`)

        const data = await response.json()
        return data.results || []
      } catch (err) {
        console.error('Search error:', err)
        return []
      }
    },

    async handleSearchUsers(query) {
      if (!query.trim()) {
        this.searchResults = []
        this.hasSearched = false
        return
      }

      try {
        const results = await this.searchUsers(query)
        this.searchResults = results
        this.hasSearched = true
      } catch (err) {
        console.error('Search error:', err)
        this.searchResults = []
        this.hasSearched = true
      }
    },

    onSearchInput() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }

      this.searchTimeout = setTimeout(() => {
        this.handleSearchUsers(this.searchQuery)
      }, 300)
    },

    onSearchBlur() {
      setTimeout(() => {
        this.showSearchResults = false
      }, 150)
    },

    selectUser(user) {
      this.searchQuery = user.username
      this.showSearchResults = false
      this.SET_PROFILE_USER_ID(user.id)
    },
  },

  beforeUnmount() {
    if (this.searchTimeout) {
      clearTimeout(this.searchTimeout)
    }
  },
}
</script>

<style scoped>
/* Search Bar Styles */
.search-container {
  position: relative;
  width: 300px;
  margin: 0 auto 32px auto;
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(8px);
  z-index: 900;
}

.search-bar {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px 16px;
  color: var(--white);
}

.search-input {
  flex: 1;
  background: none;
  border: none;
  outline: none;
  color: var(--white);
  font-size: 0.9rem;
}

.search-input::placeholder {
  color: var(--light-grey);
  opacity: 0.7;
}

.search-icon {
  color: var(--light-grey);
  font-size: 1rem;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: rgba(42, 42, 44, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  margin-top: 5px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 950;
  max-height: 200px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.search-result-item:hover {
  background: rgba(255, 255, 255, 0.07);
}

.result-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--yellow);
  color: var(--dark-grey);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 1rem;
  margin-right: 12px;
}

.result-info {
  display: flex;
  flex-direction: column;
}

.result-username {
  font-weight: 800;
  color: var(--white);
  font-size: 0.9rem;
}

.result-elo {
  font-family: 'Red Hat Text', sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.7rem;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  opacity: 0.9;
}

/* Responsive Design */
@media (max-width: 768px) {
  .search-container {
    width: 100%;
    max-width: 400px;
    margin: 0 auto 24px auto;
  }

  .search-bar {
    padding: 10px 14px;
  }

  .search-input {
    font-size: 0.85rem;
  }

  .search-icon {
    font-size: 0.9rem;
  }

  .result-avatar {
    width: 28px;
    height: 28px;
    font-size: 0.9rem;
    margin-right: 10px;
  }

  .result-username {
    font-size: 0.85rem;
  }

  .result-elo {
    font-size: 0.65rem;
  }
}

@media (max-width: 600px) {
  .search-container {
    max-width: 350px;
    margin: 0 auto 20px auto;
  }

  .search-bar {
    padding: 8px 12px;
  }

  .search-input {
    font-size: 0.8rem;
  }

  .search-icon {
    font-size: 0.85rem;
  }

  .search-result-item {
    padding: 8px 10px;
  }

  .result-avatar {
    width: 24px;
    height: 24px;
    font-size: 0.8rem;
    margin-right: 8px;
  }

  .result-username {
    font-size: 0.8rem;
  }

  .result-elo {
    font-size: 0.6rem;
  }
}

@media (max-width: 480px) {
  .search-container {
    max-width: 300px;
    margin: 0 auto 16px auto;
  }

  .search-bar {
    padding: 6px 10px;
  }

  .search-input {
    font-size: 0.75rem;
  }

  .search-icon {
    font-size: 0.8rem;
  }

  .search-dropdown {
    max-height: 150px;
  }

  .search-result-item {
    padding: 6px 8px;
  }

  .result-avatar {
    width: 20px;
    height: 20px;
    font-size: 0.7rem;
    margin-right: 6px;
  }

  .result-username {
    font-size: 0.75rem;
  }

  .result-elo {
    font-size: 0.55rem;
  }
}

@media (max-width: 360px) {
  .search-container {
    max-width: 280px;
    margin: 0 auto 12px auto;
  }

  .search-bar {
    padding: 5px 8px;
  }

  .search-input {
    font-size: 0.7rem;
  }

  .search-icon {
    font-size: 0.75rem;
  }

  .result-avatar {
    width: 18px;
    height: 18px;
    font-size: 0.65rem;
    margin-right: 5px;
  }

  .result-username {
    font-size: 0.7rem;
  }

  .result-elo {
    font-size: 0.5rem;
  }
}

/* No Results Indicator */
.no-results-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 12px;
  color: var(--light-grey);
  opacity: 0.8;
}

.no-results-icon {
  font-size: 1rem;
  margin-right: 8px;
  opacity: 0.6;
}

.no-results-text {
  font-size: 0.9rem;
  font-weight: 500;
}

@media (max-width: 768px) {
  .no-results-item {
    padding: 14px 10px;
  }

  .no-results-icon {
    font-size: 0.9rem;
    margin-right: 7px;
  }

  .no-results-text {
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .no-results-item {
    padding: 12px 8px;
  }

  .no-results-icon {
    font-size: 0.8rem;
    margin-right: 6px;
  }

  .no-results-text {
    font-size: 0.8rem;
  }
}

@media (max-width: 360px) {
  .no-results-item {
    padding: 10px 6px;
  }

  .no-results-icon {
    font-size: 0.75rem;
    margin-right: 5px;
  }

  .no-results-text {
    font-size: 0.75rem;
  }
}
</style>
