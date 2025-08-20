<script>
import { useRouter } from 'vue-router'
import HeroSection from '@/views/home-components/HeroSection.vue'
import GameModesSection from '@/views/home-components/GameModesSection.vue'
import FeaturesSection from '@/views/home-components/FeaturesSection.vue'
import LeaderboardSection from '@/views/home-components/LeaderboardSection.vue'
import TestimonialsSection from '@/views/home-components/TestimonialsSection.vue'
import LobbyBrowser from '@/components/LobbyBrowser.vue'
import CreateLobbyModal from '@/components/CreateLobbyModal.vue'
import JoinLobbyModal from '@/components/JoinLobbyModal.vue'

export default {
  components: {
    HeroSection,
    GameModesSection,
    FeaturesSection,
    LeaderboardSection,
    TestimonialsSection,
    LobbyBrowser,
    CreateLobbyModal,
    JoinLobbyModal
  },

  data() {
    return {
      router: useRouter(),
      isLoaded: false,
      showGeese: true,
      showLobbyBrowser: false,
      showCreateModal: false,
      showJoinModal: false
    }
  },

  methods: {
    evaluateGeeseVisibility() {
      const w = window.innerWidth
      const h = window.innerHeight
      const ratio = w / Math.max(h, 1)

      const tooWide = ratio >= 1.9
      const tooShort = h <= 720
      const ultraWide = w >= 1800 && ratio >= 1.7
      const extremeZoomOut = w >= 1600 && h <= 850

      this.showGeese = !(tooWide || tooShort || ultraWide || extremeZoomOut)
    },

    openLobbyBrowser() {
      this.showLobbyBrowser = true
    },

    closeLobbyBrowser() {
      this.showLobbyBrowser = false
    },

    openCreateModal() {
      this.showCreateModal = true
    },

    closeCreateModal() {
      this.showCreateModal = false
    },

    openJoinModal() {
      this.showJoinModal = true
    },

    closeJoinModal() {
      this.showJoinModal = false
    },

    handleLobbyCreated(lobby) {
      this.closeCreateModal()
      this.closeLobbyBrowser()
      // Navigate to the lobby
      this.router.push({
        name: 'lobby',
        query: {
          gameMode: 'multiplayer',
          lobbyId: lobby.id,
          lobbyCode: lobby.lobbyCode
        }
      })
    },

    handleLobbyJoined(lobby) {
      this.closeJoinModal()
      this.closeLobbyBrowser()
      // Navigate to the lobby
      this.router.push({
        name: 'lobby',
        query: {
          gameMode: 'multiplayer',
          lobbyId: lobby.id,
          lobbyCode: lobby.lobbyCode
        }
      })
    },

    async handleGoogleOAuthCallback() {
      const urlParams = new URLSearchParams(window.location.search)
      console.log([...urlParams.entries()])


      if (urlParams.get('google_auth') === 'true') {
        try {
          const email = urlParams.get('email')
          const name = urlParams.get('name')
          const picture = urlParams.get('picture')

          if (email && name) {
            // Create user account from Google credentials
            await this.$store.dispatch('user/signUpWithGoogle', {
              email,
              name,
              picture: picture || null
            })

            // Clear URL parameters
            window.history.replaceState({}, document.title, window.location.pathname)
          }
        } catch (error) {
          console.error('Google OAuth signup failed:', error)
          // Handle error - could show a toast notification
        }
      }
    }
  },

  mounted() {
    setTimeout(() => {
      this.isLoaded = true
    }, 100)

    this.evaluateGeeseVisibility()
    window.addEventListener('resize', this.evaluateGeeseVisibility)

    // Handle Google OAuth callback
    this.handleGoogleOAuthCallback()
  },

  unmounted() {
    window.removeEventListener('resize', this.evaluateGeeseVisibility)
  }
}
</script>

<template>
  <div class="home-container">
    <div class="home-content-scale">
      <HeroSection :isLoaded="isLoaded" />
      <GameModesSection :isLoaded="isLoaded" @open-lobby-browser="openLobbyBrowser" />
     <FeaturesSection :isLoaded="isLoaded" />

     <div class="section-with-goose goose1-wrapper">
       <LeaderboardSection :isLoaded="isLoaded" />
       <img
         class="goose-decor goose1"
         src="/Goose1.png"
         alt=""
         aria-hidden="true"
         v-if="showGeese"
       />
     </div>

     <div class="section-with-goose goose2-wrapper">
       <TestimonialsSection :isLoaded="isLoaded" />
       <img
         class="goose-decor goose2"
         src="/Goose2.png"
         alt=""
         aria-hidden="true"
         v-if="showGeese"
/>
     </div>
    </div>

    <!-- Lobby Browser Modal -->
    <Transition name="modal-fade">
      <div v-if="showLobbyBrowser" class="lobby-browser-overlay" @click="closeLobbyBrowser">
        <div class="lobby-browser-container" @click.stop>
          <button class="close-lobby-browser" @click="closeLobbyBrowser">&times;</button>
          <LobbyBrowser
            @open-create-modal="openCreateModal"
            @open-join-modal="openJoinModal"
            @lobby-created="handleLobbyCreated"
            @lobby-joined="handleLobbyJoined"
          />
        </div>
      </div>
    </Transition>

    <!-- Create Lobby Modal -->
    <CreateLobbyModal
      v-if="showCreateModal"
      :isVisible="showCreateModal"
      @close="closeCreateModal"
      @lobby-created="handleLobbyCreated"
    />

    <!-- Join Private Lobby Modal -->
    <JoinLobbyModal
      v-if="showJoinModal"
      :isVisible="showJoinModal"
      @close="closeJoinModal"
      @lobby-joined="handleLobbyJoined"
    />
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: url('/HomePage.png') center top / 100% auto no-repeat, var(--dark-grey);
  padding-top: 100px;
}

.home-content-scale {
  transform: none;
  transform-origin: top center;
}

.section-with-goose {
  position: relative;
}

/* Ensure the section content stacks above the decorative image */
.section-with-goose > :first-child {
  position: relative;
  z-index: 1;
}

.goose-decor {
  position: absolute;
  pointer-events: none;
  user-select: none;
  filter: drop-shadow(0 6px 18px rgba(0, 0, 0, 0.35));
  z-index: 0;
}

.goose1-wrapper {
  overflow: visible;
}
.goose1 {
  width: 24vw;
  top: -150px;
  right: 0px;
}


.goose2-wrapper {
  overflow: visible;
}
.goose2 {
  width: 16vw;
  top: -10px;
  left: 0px;
}

@media (max-width: 1200px) {
  .goose1 { width: 220px; right: 0; }
  .goose2 { width: 160px; left: 0; }
}

@media (max-width: 992px) {
  .goose1 { width: 180px; right: 0; }
  .goose2 { width: 130px; left: 0; }
}

@media (max-width: 768px) {
  .home-container {
    padding-top: 80px;
    background: url('/HomePage.png') center top / 100% auto no-repeat, var(--dark-grey);
  }

  .home-content-scale {
    transform: none;
  }

  /* Hide decorative geese on small screens for cleanliness */
  .goose-decor {
    display: none;
  }
}

/* Lobby Browser Modal Styles */
.lobby-browser-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5000;
  padding: 20px;
}

.lobby-browser-container {
  position: relative;
  width: 100%;
  max-width: 1000px;
  max-height: 85vh;
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.close-lobby-browser {
  position: absolute;
  top: -40px;
  right: 0;
  background: none;
  border: none;
  color: white;
  font-size: 32px;
  cursor: pointer;
  padding: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-lobby-browser:hover {
  background: rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .lobby-browser-overlay {
    padding: 10px;
  }

  .lobby-browser-container {
    width: 95vw;
  }

  .close-lobby-browser {
    top: -50px;
    right: 10px;
  }
}


</style>
