<template>
  <transition name="fade-slide" mode="out-in">
    <div v-if="getProfileUserId" :key="'history-' + getProfileUserId" class="match-history-section">
      <div class="history">
        <div class="history-header">
          <h2>Recent Match History</h2>
          <div class="pager">
            <button class="pager-btn" :disabled="historyLoading || historyOffset === 0"
              @click="previousHistoryPage">Prev</button>
            <button class="pager-btn" :disabled="historyLoading || !historyHasNext"
              @click="nextHistoryPage">Next</button>
          </div>
        </div>

        <div v-if="historyLoading" class="history-loading">
          <div class="loading-spinner small"></div>
          <span>Loading history...</span>
        </div>

        <div v-else-if="historyError" class="history-error">{{ historyError }}</div>
        <div v-else-if="matchHistory.length === 0" class="history-empty">No matches found.</div>
        <ul v-else class="history-list">
          <li v-for="item in matchHistory" :key="item.gameId" class="history-item" @click="openMatch(item)">
            <div class="left">
              <div class="mode">{{ item.gameMode }}</div>
              <div class="date">{{ formatDate(item.playedAt) }}</div>
            </div>
            <div class="right">
              <template v-if="!item.finished">
                <span class="chip chip-grey">Unfinished</span>
              </template>
              <template v-else-if="item.gameMode === 'Singleplayer'">
                <span class="chip chip-green">Survived {{ item.roundsSurvived }} rounds</span>
              </template>
              <template v-else>
                <span class="players-badge">
                  <font-awesome-icon icon="user-group" class="players-icon" />
                  <span class="players-count">{{ item.numPlayers }}</span>
                </span>
                <span class="chip chip-result" :class="item.won ? 'chip-green' : 'chip-red'">{{ item.won ? 'Win' : 'Loss' }}</span>
              </template>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div v-else class="empty">No user selected.</div>
  </transition>
</template>

<script>
import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'MatchHistory',
  emits: ['open-match'],

  data() {
    return {
      matchHistory: [],
      historyLoading: false,
      historyError: null,
      historyLimit: 10,
      historyOffset: 0,
      historyHasNext: false
    }
  },
  computed: {
    ...mapGetters('profile', ['getProfileUserId'])
  },
  watch: {
    getProfileUserId: {
      handler(newUserId) {
        if (newUserId) {
          this.historyOffset = 0
          this.fetchMatchHistory()
        } else {
          this.matchHistory = []
          this.historyHasNext = false
        }
      },
      immediate: true
    }
  },
  methods: {
    ...mapActions('user', ['fetchUserMatchHistory']),

    openMatch(item) {
      this.$emit('open-match', item)
    },

    formatDate(iso) {
      const d = new Date(iso)
      return d.toLocaleString()
    },

    async fetchMatchHistory() {
      this.historyLoading = true
      this.historyError = null
      try {
        if (!this.getProfileUserId) {
          this.matchHistory = []
          this.historyHasNext = false
          return
        }

        const { results, hasNext } = await this.fetchUserMatchHistory({
          userId: this.getProfileUserId,
          offset: this.historyOffset,
          limit: this.historyLimit
        })

        this.matchHistory = results
        this.historyHasNext = hasNext
      } catch (e) {
        this.historyError = e instanceof Error ? e.message : 'Failed to fetch match history'
      } finally {
        this.historyLoading = false
      }
    },

    nextHistoryPage() {
      this.historyOffset += this.historyLimit
      this.fetchMatchHistory()
    },

    previousHistoryPage() {
      this.historyOffset = Math.max(0, this.historyOffset - this.historyLimit)
      this.fetchMatchHistory()
    }
  }
}
</script>

<style scoped>
/* Match History Section */
.match-history-section {
  background: rgba(42, 42, 44, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(8px);
}

/* History */
.history {
  padding: 22px 22px 22px 22px;
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.history-header h2 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
}

.pager {
  display: flex;
  gap: 10px;
}

.pager-btn {
  background: rgba(255, 255, 255, 0.08);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
}

.pager-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.history-loading,
.history-error,
.history-empty {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--light-grey);
}

.history-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: transform 0.2s ease, background 0.2s ease;
}

.history-item:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.07);
}

.history-item .left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-item .right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.history-item .mode {
  font-weight: 700;
}

.history-item .date {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.9rem;
  letter-spacing: 1.0px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.chip {
  padding: 6px 10px;
  border-radius: 999px;
  font-weight: 700;
  border: 1px solid transparent;
}

.chip-green {
  background: rgba(182, 255, 127, 0.15);
  color: #B6FF7F;
  border-color: rgba(182, 255, 127, 0.35);
}

.chip-red {
  background: rgba(255, 127, 127, 0.15);
  color: #FF7F7F;
  border-color: rgba(255, 127, 127, 0.35);
}

.chip-grey {
  background: rgba(128, 128, 128, 0.15);
  color: #808080;
  border-color: rgba(128, 128, 128, 0.35);
}

/* Fixed width for Win/Loss chip to keep layout stable */
.chip-result {
  width: 84px;
  text-align: center;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.players-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--light-grey);
  font-weight: 700;
  margin-right: 8px;
}

.players-icon {
  color: var(--light-grey);
  font-size: 1rem;
}

.players-count {
  color: var(--light-grey);
}

.empty {
  color: var(--light-grey);
  text-align: center;
  padding: 40px 20px;
}

/* Loading spinner styles */
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top: 2px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-spinner.small {
  width: 16px;
  height: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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

/* Responsive Design */
@media (max-width: 768px) {
  .history-header h2 {
    font-size: 1.4rem;
    letter-spacing: 0.8px;
  }

  .pager-btn {
    padding: 8px 12px;
    font-size: 0.8rem;
  }

  .history-item {
    padding: 10px 12px;
  }

  .history-item .mode {
    font-size: 0.9rem;
  }

  .history-item .date {
    font-size: 0.8rem;
    letter-spacing: 0.8px;
  }

  .chip {
    padding: 5px 8px;
    font-size: 0.6rem;
  }

  .chip-result {
    width: 70px;
  }

  .players-badge {
    font-size: 0.8rem;
  }

  .players-icon {
    font-size: 0.9rem;
  }

  .players-count {
    font-size: 0.8rem;
  }

  .history-loading span {
    font-size: 0.9rem;
  }

  .history-error,
  .history-empty {
    font-size: 0.9rem;
  }
}

@media (max-width: 600px) {
  .history-header h2 {
    font-size: 1.2rem;
    letter-spacing: 0.6px;
  }

  .pager-btn {
    padding: 6px 10px;
    font-size: 0.75rem;
  }

  .history-item {
    padding: 8px 10px;
  }

  .history-item .mode {
    font-size: 0.85rem;
  }

  .history-item .date {
    font-size: 0.75rem;
    letter-spacing: 0.7px;
  }

  .chip {
    padding: 4px 7px;
    font-size: 0.6rem;
  }

  .chip-result {
    width: 65px;
  }

  .players-badge {
    font-size: 0.75rem;
  }

  .players-icon {
    font-size: 0.8rem;
  }

  .players-count {
    font-size: 0.75rem;
  }

  .history-loading span {
    font-size: 0.85rem;
  }

  .history-error,
  .history-empty {
    font-size: 0.85rem;
  }
}

@media (max-width: 480px) {
  .history-header h2 {
    font-size: 1.1rem;
    letter-spacing: 0.5px;
  }

  .pager-btn {
    padding: 5px 8px;
    font-size: 0.7rem;
  }

  .history-item {
    padding: 6px 8px;
  }

  .history-item .mode {
    font-size: 0.8rem;
  }

  .history-item .date {
    font-size: 0.7rem;
    letter-spacing: 0.6px;
  }

  .chip {
    padding: 3px 6px;
    font-size: 0.7rem;
  }

  .chip-result {
    width: 60px;
  }

  .players-badge {
    font-size: 0.7rem;
  }

  .players-icon {
    font-size: 0.75rem;
  }

  .players-count {
    font-size: 0.7rem;
  }

  .history-loading span {
    font-size: 0.8rem;
  }

  .history-error,
  .history-empty {
    font-size: 0.8rem;
  }
}

@media (max-width: 360px) {
  .history-header h2 {
    font-size: 1rem;
    letter-spacing: 0.4px;
  }

  .pager-btn {
    padding: 4px 6px;
    font-size: 0.65rem;
  }

  .history-item {
    padding: 5px 6px;
  }

  .history-item .mode {
    font-size: 0.75rem;
  }

  .history-item .date {
    font-size: 0.65rem;
    letter-spacing: 0.5px;
  }

  .chip {
    padding: 2px 5px;
    font-size: 0.5rem;
  }

  .chip-result {
    width: 55px;
  }

  .players-badge {
    font-size: 0.65rem;
  }

  .players-icon {
    font-size: 0.7rem;
  }

  .players-count {
    font-size: 0.65rem;
  }

  .history-loading span {
    font-size: 0.75rem;
  }

  .history-error,
  .history-empty {
    font-size: 0.75rem;
  }
}
</style>
