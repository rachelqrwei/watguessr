<template>
  <section class="leaderboard-preview" :class="{ loaded: isLoaded }">
    <div class="leaderboard-content">
      <h2>TOP PLAYERS THIS WEEK</h2>
      <div class="leaderboard-list">
        <div
          v-for="(player, index) in leaderboard"
          :key="player.id || player.username + index"
          class="leaderboard-item"
          :class="getRankClass(index + 1)"
          @click="goToProfile(player.id)"
          style="cursor: pointer;"
        >
          <div class="rank-badge">{{ index + 1 }}</div>
          <div class="player-info">
            <div class="player-name">{{ player.username }}</div>
            <div class="player-score">{{ player.elo }} pts</div>
          </div>
          <div v-if="index + 1 <= 3" class="player-medal">{{ getRankMedal(index + 1) }}</div>
        </div>
      </div>
      <div class="leaderboard-cta">
        <button class="view-full-leaderboard" @click="router.push({ name: 'leaderboard' })">View Full Leaderboard</button>
      </div>
    </div>
  </section>
</template>

<script>
import { useRouter } from 'vue-router'
import { mapGetters, mapActions } from 'vuex'

export default {
  props: {
    isLoaded: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      router: useRouter()
    }
  },

  computed: {
    ...mapGetters({
      leaderboard: 'leaderboard/leaderboard'
    })
  },

  methods: {
    ...mapActions({
      fetchLeaderboard: 'leaderboard/fetchLeaderboard'
    }),

    getRankClass(rank) {
      if (rank === 1) return 'rank-1'
      if (rank === 2) return 'rank-2'
      if (rank === 3) return 'rank-3'
      return ''
    },

    getRankMedal(rank) {
      if (rank === 1) return '🥇'
      if (rank === 2) return '🥈'
      if (rank === 3) return '🥉'
      return ''
    },

    goToProfile(userId) {
      if (!userId) return
      this.router.push({ name: 'profile', params: { userId } })
    }
  },

  mounted() {
    // Fetch only the top 5 by elo for the preview
    this.fetchLeaderboard({ limit: 5, sortBy: 'elo', offset: 0 })
  }
}
</script>

<style scoped>
.leaderboard-preview {
  padding: 72px 36px;
  opacity: 0;
  transform: translateY(30px);
  transition: all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1);
  transition-delay: 1s;
}

.leaderboard-preview.loaded {
  opacity: 1;
  transform: translateY(0);
}

.leaderboard-content {
  max-width: 1080px;
  margin: 0 auto;
  text-align: center;
}

.leaderboard-content h2 {
  font-weight: 600;
  font-size: 1.6rem;
  color: var(--white);
  text-transform: uppercase;
  margin-bottom: 1.5rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 1.5px;
  line-height: 1.5;
}

.leaderboard-list {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 18px;
  margin-bottom: 27px;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: visible;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-x: contain;
  padding-bottom: 6px; /* space for scrollbar */
  padding-top: 6px; /* space for hover lift */
  /* hide scrollbar cross-browser */
  -ms-overflow-style: none; /* IE and old Edge */
  scrollbar-width: none; /* Firefox */
}

.leaderboard-list::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 9px 18px;
  background: rgba(42, 42, 44, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  backdrop-filter: blur(6px);
  transition: all 0.3s ease;
  flex: 0 0 auto; /* prevent shrinking so items stays in one row */
  position: relative;
  overflow: hidden;
}

.leaderboard-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
  z-index: 1;
}

.leaderboard-item:hover {
  transform: translateY(-3px);
  background: rgba(42, 42, 44, 0.5);
  z-index: 2;
}

.leaderboard-item:hover::before {
  left: 100%;
}


.leaderboard-list::-webkit-scrollbar {
  height: 6px;
}
.leaderboard-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 3px;
}
.leaderboard-list::-webkit-scrollbar-thumb {
  background: rgba(255, 203, 59, 0.4);
  border-radius: 3px;
}

.rank-1 {
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.2) 0%, rgba(42, 42, 44, 0.5) 100%);
  border: 1px solid rgba(255, 203, 59, 0.3);
  box-shadow: 0 0 20px rgba(255, 203, 59, 0.3);
}

.rank-1::before {
  background: linear-gradient(90deg, transparent, rgba(255, 203, 59, 0.3), transparent);
}

.rank-2 {
  background: linear-gradient(135deg, rgba(150, 150, 150, 0.2) 0%, rgba(42, 42, 44, 0.5) 100%);
  border: 1px solid rgba(150, 150, 150, 0.3);
  box-shadow: 0 0 10px rgba(150, 150, 150, 0.2);
}

.rank-2::before {
  background: linear-gradient(90deg, transparent, rgba(150, 150, 150, 0.3), transparent);
}

.rank-3 {
  background: linear-gradient(135deg, rgba(100, 100, 100, 0.2) 0%, rgba(42, 42, 44, 0.5) 100%);
  border: 1px solid rgba(100, 100, 100, 0.3);
  box-shadow: 0 0 5px rgba(100, 100, 100, 0.1);
}

.rank-3::before {
  background: linear-gradient(90deg, transparent, rgba(100, 100, 100, 0.3), transparent);
}

.rank-1:hover,
.rank-2:hover,
.rank-3:hover {
  background: rgba(42, 42, 44, 0.5) !important;
}

.rank-badge {
  font-size: 1.08rem;
  font-weight: 900;
  color: var(--white);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  width: 27px;
  height: 27px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(5px);
}

.player-info {
  text-align: left;
  flex: 1;
}

.player-name {
  margin-top: 2px;
  font-weight: 600;
  font-size: 1.1rem;
  color: var(--white);
  margin-bottom: 3px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  letter-spacing: 1.5px;
  line-height: 1.2;
}

.player-score {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 500;
  font-size: 0.9rem;
  color: var(--yellow);
  letter-spacing: 1.2px;
  line-height: 1.6;
  margin-top: 3px;
}

.player-medal {
  font-size: 1.35rem;
  color: var(--yellow);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.leaderboard-cta {
  margin-top: 27px;
}

.view-full-leaderboard {
  background: rgba(255, 235, 59, 0.15);
  color: var(--yellow);
  padding: 14px 28px;
  border-radius: 10px;
  font-size: 0.95rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border: 0.5px solid var(--yellow);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 4px 15px rgba(255, 235, 59, 0.1);
  backdrop-filter: blur(8px);
  position: relative;
  overflow: hidden;
}

.view-full-leaderboard::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.view-full-leaderboard:hover {
  background: rgba(255, 235, 59, 0.2);
  border-color: rgba(255, 235, 59, 0.8);
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 235, 59, 0.2);
}

.view-full-leaderboard:hover::before {
  left: 100%;
}

.view-full-leaderboard:active {
  transform: translateY(-1px);
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
