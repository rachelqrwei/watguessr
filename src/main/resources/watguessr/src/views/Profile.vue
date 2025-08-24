<template>

  <div class="profile-background" aria-hidden="true"></div>
  <div class="profile-view">


    <SearchBar />

    <div v-if="!getProfileUserId && !isLoading && !errorMessage" class="profile-hint">
      Search for a user to view their profile...
    </div>

    <div v-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading profile...</p>
    </div>


    <div v-else-if="errorMessage" class="error">{{ errorMessage }}</div>


    <ProfileStats v-if="getProfileUserId" />

    <MatchHistory v-if="getProfileUserId" @open-match="onOpenMatch" />

    <MatchDetailsModal
      v-if="selectedMatch"
      :visible="modalVisible"
      :game-id="selectedMatch ? selectedMatch.gameId : ''"
      @close="modalVisible = false"
    />
  </div>
</template>

<script>
import { mapGetters, mapActions, mapMutations } from 'vuex'
import { RouterLink } from 'vue-router'
import SearchBar from './profile-components/SearchBar.vue'
import MatchHistory from './profile-components/MatchHistory.vue'
import ProfileStats from './profile-components/ProfileStats.vue'
import MatchDetailsModal from './profile-components/MatchDetailsModal.vue'

export default {
  components: {
    RouterLink,
    SearchBar,
    MatchHistory,
    ProfileStats,
    MatchDetailsModal
  },
  props: {
    userId: {
      type: String,
      required: false
    }
  },
  emits: ['closeLogin', 'closeSignUp', 'openLogin', 'openSignUp'],
  data() {
    return {

      isLoading: false,
      errorMessage: null,
      modalVisible: false,
      selectedMatch: null
    }
  },
  computed: {
    ...mapGetters('user', ['getCurrentUser', 'getUserById']),
    ...mapGetters('profile', ['getProfileUserId']),

  },

  watch: {
    getCurrentUser() {
      if (this.getCurrentUser) {
        if (!this.getProfileUserId && !this.userId) {
          this.SET_PROFILE_USER_ID(this.getCurrentUser.id)
        }
      }
    },
    '$route.params.userId': {
      handler(newId) {
        if (newId) {
          this.SET_PROFILE_USER_ID(newId)
        }
      },
      immediate: true
    }
  },

  methods: {
    ...mapActions('user', [
      'fetchUserById'
    ]),
    ...mapMutations('profile', ['SET_PROFILE_USER_ID']),
    onOpenMatch(item) {
      this.selectedMatch = item
      this.modalVisible = true
    }
  },
  mounted() {
    if (this.userId) {
      this.SET_PROFILE_USER_ID(this.userId)
    } else if (this.getCurrentUser) {
      this.SET_PROFILE_USER_ID(this.getCurrentUser.id)
    }
    // For guest users without a specific userId, we just show the search interface
  }
}
</script>



<style scoped>
.profile-background {
  position: fixed;
  inset: 0;
  background: var(--dark-grey);
  z-index: -1;
}

.profile-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/ProfilePage.png') center top / cover no-repeat;
  opacity: 0.8;
  pointer-events: none;
}

.profile-view {
  max-width: 880px;
  margin: 0 auto;
  padding: 40px 20px 40px;
  color: var(--white);
  position: relative;
  min-height: calc(100vh - 80px);
}

.loading,
.error,
.empty {
  text-align: center;
  color: var(--light-grey);
}

.profile-hint {
  margin: 8px 0 16px 0;
  text-align: center;
  color: var(--light-grey);
  font-style: italic;
}

.loading-spinner {
  width: 48px;
  height: 48px;
  border: 3px solid rgba(255, 203, 59, 0.3);
  border-top: 3px solid var(--yellow);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 12px;
}

.loading-spinner.small {
  width: 28px;
  height: 28px;
  border-width: 3px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
}

/* Responsive Design */
@media (max-width: 768px) {
  .profile-view {
    padding: 30px 24px 30px;
    margin-top: 80px;
  }
  
  .profile-hint {
    margin: 12px 0 20px 0;
    font-size: 0.9rem;
    color: rgba(255, 255, 255, 0.8);
  }
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border-width: 2px;
  }
  
  .loading-spinner.small {
    width: 24px;
    height: 24px;
    border-width: 2px;
  }
}

@media (max-width: 600px) {
  .profile-view {
    padding: 24px 20px 24px;
    margin-top: 80px;
  }
  
  .profile-hint {
    margin: 16px 0 24px 0;
    font-size: 0.85rem;
    color: rgba(255, 255, 255, 0.8);
  }
  
  .loading-spinner {
    width: 36px;
    height: 36px;
    border-width: 2px;
  }
  
  .loading-spinner.small {
    width: 20px;
    height: 20px;
    border-width: 2px;
  }
}

@media (max-width: 480px) {
  .profile-view {
    padding: 20px 16px 20px;
    margin-top: 80px;
  }
  
  .profile-hint {
    margin: 20px 0 28px 0;
    font-size: 0.8rem;
    color: rgba(255, 255, 255, 0.8);
  }
  
  .loading-spinner {
    width: 32px;
    height: 32px;
    border-width: 2px;
  }
  
  .loading-spinner.small {
    width: 18px;
    height: 18px;
    border-width: 2px;
  }
}

@media (max-width: 360px) {
  .profile-view {
    padding: 16px 12px 16px;
    margin-top: 80px;
  }
  
  .profile-hint {
    margin: 24px 0 32px 0;
    font-size: 0.75rem;
  }
  
  .loading-spinner {
    width: 28px;
    height: 28px;
    border-width: 2px;
  }
  
  .loading-spinner.small {
    width: 16px;
    height: 16px;
    border-width: 2px;
  }
}

/* Landscape orientation adjustments for mobile */
@media (max-height: 500px) and (orientation: landscape) {
  .profile-view {
    padding: 20px 20px;
    margin-top: 60px;
  }
  
  .profile-hint {
    margin: 8px 0 12px 0;
  }
}

/* High DPI displays */
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
  .loading-spinner {
    image-rendering: -webkit-optimize-contrast;
    image-rendering: crisp-edges;
  }
}

/* Reduced motion for accessibility */
@media (prefers-reduced-motion: reduce) {
  .loading-spinner {
    animation: none;
  }
}

/* Print styles */
@media print {
  .profile-background {
    display: none;
  }
  
  .profile-view {
    padding: 0;
    margin: 0;
    min-height: auto;
    color: black;
  }
  
  .profile-hint {
    color: #666;
  }
}
</style>
