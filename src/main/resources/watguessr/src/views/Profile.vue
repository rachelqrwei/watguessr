<template>

  <div class="profile-background" aria-hidden="true"></div>
  <div class="profile-view">


    <SearchBar />

    <div v-if="!getProfileUserId && !isLoading && !errorMessage" class="profile-hint">
      Search for a user or log in to view profile...
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
        if (!this.getProfileUserId) {
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
  }
}
</script>

<style>
/* Global custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 227, 127, 0.05);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #FFE37F;
  border-radius: 4px;
  opacity: 0.8;
}

::-webkit-scrollbar-thumb:hover {
  background: #FFE37F;
  opacity: 1;
}
</style>

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
  min-height: 100vh;
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

</style>
