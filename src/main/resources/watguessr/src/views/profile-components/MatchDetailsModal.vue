<template>
  <Transition name="modal-fade">
    <div v-if="visible" class="modal-backdrop" @click.self="close">
      <div class="modal">
      <div class="modal-header">
        <h3>Match Details</h3>
        <button class="close-btn" @click="close">✕</button>
      </div>

      <div class="modal-content" v-if="!isLoading && !errorMessage">
        <div v-if="rounds.length === 0" class="empty">No rounds found.</div>
        <div v-else class="round-list">
          <div v-for="(round, idx) in rounds" :key="round.id" class="round-item">
            <div class="round-header">Round {{ idx + 1 }}</div>
            <div class="guesses">
              <div v-for="g in guessesByRound[round.id] || []" :key="g.id" class="guess-row" :class="userClass(g.userId)">
                <button type="button" class="guess-user" :class="{ disabled: isCurrentUser(g.userId) }" :disabled="isCurrentUser(g.userId)" @click="goToUser(g.userId)">{{ usernameFor(g.userId) }}</button>
                <div class="guess-metrics">
                  <span class="pill" :class="getPointsClass(g.points, round.id)">{{ g.points ?? 0 }} pts</span>
                  <span class="pill">{{ timeDisplay(g.time) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <span>Loading match…</span>
      </div>

      <div v-else class="error">{{ errorMessage }}</div>
    </div>
  </div>
  </Transition>

</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'MatchDetailsModal',
  props: {
    visible: { type: Boolean, default: false },
    gameId: { type: String, required: true },
    userDirectory: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      rounds: [],
      guessesByRound: {},
      isLoading: false,
      errorMessage: null,
      localUserDirectory: {},
      userIndex: {}
    }
  },
  computed: {
    ...mapGetters('profile', ['getProfileUserId']),
    gameIdComputed() {
      return this.gameId || null
    }
  },
  watch: {
    visible(immediatelyOpen) {
      if (immediatelyOpen && this.gameIdComputed) {
        this.loadData()
      }
    },
    gameIdComputed: {
      handler(val) {
        if (this.visible && val) {
          this.loadData()
        }
      },
      immediate: true
    }
  },
  methods: {
    close() {
      this.$emit('close')
    },
    goToUser(userId) {
      if (!userId) return
      if (this.isCurrentUser(userId)) return
      this.$router.push({ name: 'profile', params: { userId } })
      this.$emit('close')
    },
    isCurrentUser(userId) {
      if (!userId || !this.getProfileUserId) return false
      return String(userId) === String(this.getProfileUserId)
    },
    async loadData() {
      if (!this.gameId) return
      this.isLoading = true
      this.errorMessage = null
      this.rounds = []
      this.guessesByRound = {}
      this.localUserDirectory = {}
      try {
        // Single fetch that returns all rounds with their guesses
        const res = await fetch(`/api/round/by-game-with-guesses?gameId=${this.gameId}`)
        if (!res.ok) throw new Error('Failed to fetch match details')
        const payload = await res.json()
        const list = Array.isArray(payload) ? payload : []
        // Build rounds array and map of guesses by round
        this.rounds = list.map(item => ({ id: item.roundId }))
        list.forEach(item => {
          this.guessesByRound[item.roundId] = Array.isArray(item.guesses) ? item.guesses : []
        })

        // 3) build user directory for display (best-effort)
        const userIds = new Set()
        Object.values(this.guessesByRound).forEach((arr) => {
          ;(arr || []).forEach((g) => {
            if (g && g.userId) userIds.add(g.userId)
          })
        })
        const fetches = Array.from(userIds).map(async (id) => {
          try {
            const resp = await fetch(`/api/user/${id}`)
            if (resp.ok) {
              const u = await resp.json()
              if (u && u.id) this.localUserDirectory[u.id] = (u.username || u.id)
            }
          } catch (_) {}
        })
        await Promise.all(fetches)

        // Build a stable index for users to alternate row colors consistently
        const orderedIds = Array.from(userIds)
        this.userIndex = {}
        orderedIds.forEach((id, i) => {
          this.userIndex[id] = i
        })
      } catch (e) {
        this.errorMessage = e instanceof Error ? e.message : 'Failed to load match details'
      } finally {
        this.isLoading = false
      }
    },
    timeDisplay(ms) {
      if (typeof ms !== 'number') return '0s'
      const seconds = Math.round(ms / 1000)
      return `${seconds}s`
    },
    usernameFor(userId) {
      if (!userId) return 'Unknown'
      if (this.localUserDirectory && this.localUserDirectory[userId]) return this.localUserDirectory[userId]
      if (this.userDirectory && this.userDirectory[userId]) return this.userDirectory[userId]
      return userId
    },
    userClass(userId) {
      const idx = this.userIndex && typeof this.userIndex[userId] === 'number' ? this.userIndex[userId] : 0
      return idx % 2 === 0 ? 'user-a' : 'user-b'
    },
    getPointsClass(points, roundId) {
      const roundGuesses = this.guessesByRound[roundId] || []
      const pointsValue = points ?? 0
      
      // Find the highest points in this round
      const highestPoints = Math.max(...roundGuesses.map(g => g.points ?? 0))
      
      // If this guess has the highest points, show as positive (green)
      // Otherwise show as negative (red)
      if (pointsValue === highestPoints && pointsValue > 0) {
        return { positive: true }
      } else {
        return { negative: true }
      }
    }
  }
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5000;
}

.modal {
  width: clamp(520px, 52vw, 820px);
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border-radius: 16px;
  color: var(--white);

}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 15px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: sticky;
  top: 0;
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  z-index: 1;
}

.modal-header h3 {
  color: var(--white);
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  color: #999;
  font-size: 20px;
  font-weight: 300;
  cursor: pointer;
  padding: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  margin: 4px -4px 0 0;
  line-height: 1;
}

.close-btn:hover {
  color: white;
  transform: scale(1.05);
}

.close-btn:active {
  transform: scale(0.95);
}

.modal-content {
  padding: 16px 18px 22px 18px;
  overflow: auto;
  width: 100%;
  flex: 1;
  border-radius: 0;
  border: none;
}

.loading,
.error,
.empty {
  padding: 18px;
  color: var(--light-grey);
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-top: 2px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  display: inline-block;
  margin-right: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.round-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.round-item {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 14px;
}

.round-header {
  font-weight: 800;
  margin-bottom: 10px;
}

.guesses {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.guess-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.guess-row.user-a {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 6px;
  padding: 8px 10px;
}

.guess-row.user-b {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  padding: 8px 10px;
}

.guess-user {
  font-weight: 700;
  min-width: 140px;
  cursor: pointer;
  background: transparent;
  border: none;
  color: var(--white);
  padding: 0;
  text-align: left;
}

.guess-user:disabled,
.guess-user.disabled {
  cursor: default;
}

.guess-metrics {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  font-weight: 700;
}

.pill.positive {
  background: rgba(182, 255, 127, 0.15);
  color: #B6FF7F;
  border-color: rgba(182, 255, 127, 0.35);
}

.pill.negative {
  background: rgba(255, 127, 127, 0.15);
  color: #FF7F7F;
  border-color: rgba(255, 127, 127, 0.35);
}


</style>


