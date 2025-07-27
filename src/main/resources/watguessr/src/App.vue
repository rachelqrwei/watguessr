<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './components/Header.vue'
import AuthModalManager from "@/views/auth/AuthModalManager.vue";

const route = useRoute()
const isHoveringHeader = ref(false)
const showLogin = ref(false) // ✅ reactive state for login modal
const showSignUp = ref(false);

const isHomePage = computed(() => route.path === '/')
const isPlayPage = computed(() => route.path === '/play')
const showHeader = computed(() => (isHomePage.value || isHoveringHeader.value) && !isPlayPage.value)

const navLinks = [
  { path: '/play', label: 'PLAY WATGUESSR', icon: 'play' },
  { path: '/leaderboard', label: 'LEADERBOARD', icon: 'trophy' },
  { path: '/profile', label: 'PROFILE', icon: 'user' },
  { path: '/settings', label: 'SETTINGS', icon: 'cog' }
]
</script>

<template>
  <Header />

  <div class="layout">
    <div
      class="sidebar-trigger"
      @mouseenter="isHoveringHeader = true"
      @mouseleave="isHoveringHeader = false"
    />

    <aside
      class="sidebar"
      :class="{ 'sidebar-hidden': !showHeader }"
      @mouseenter="isHoveringHeader = true"
      @mouseleave="isHoveringHeader = false"
    >
      <div class="logo-section">
        <div class="logo-container">
          <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
          <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
        </div>
      </div>

      <div class="nav-section">
        <h4 class="section-title">MAIN</h4>
        <nav class="nav-menu">
          <RouterLink
            v-for="link in navLinks"
            :key="link.path"
            :to="link.path"
            class="nav-item"
            :class="{ 'nav-item-active': route.path === link.path }"
          >
            <div class="nav-icon">
              <font-awesome-icon :icon="link.icon" />
            </div>
            <span class="nav-label">{{ link.label }}</span>
            <div class="nav-indicator" />
          </RouterLink>
        </nav>
      </div>

      <div class="sidebar-footer">
        <div class="report-bug-sidebar">
          <h4>REPORT A BUG</h4>
          <p>To leave feedback, please
            <span class="link" @click="showLogin = true">LOG IN</span>
          </p>
        </div>
        <div class="version-info">v1.0.0</div>
      </div>
    </aside>

    <main class="main-content" :class="{ 'content-with-sidebar': showHeader, 'content-expanded': !showHeader }">
      <div class="content-wrapper">
        <RouterView />
      </div>
    </main>

    <AuthModalManager
      :showLogin="showLogin"
      :showSignUp="showSignUp"
      @closeLogin="showLogin = false"
      @closeSignUp="showSignUp = false"
      @openLogin="() => { showLogin = true; showSignUp = false }"
      @openSignUp="() => { showSignUp = true; showLogin = false }"
    />

    <div
      v-if="showHeader && !isHomePage"
      class="backdrop"
      @click="isHoveringHeader = false"
    />
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  position: relative;
}

.sidebar-trigger {
  position: fixed;
  top: 0;
  left: 0;
  width: 20px;
  height: 100vh;
  z-index: 1001;
  background: transparent;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 270px;
  height: 100vh;
  background: linear-gradient(180deg, var(--dark-grey) 0%, #1a1a1c 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow:
    4px 0 20px rgba(0, 0, 0, 0.3),
    inset -1px 0 0 rgba(255, 255, 255, 0.05);
  transform: translateX(0);
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  z-index: 1000;
  backdrop-filter: blur(10px);
  display: flex;
  flex-direction: column;
}

.sidebar-hidden {
  transform: translateX(-100%);
}

.logo-section {
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.1) 0%, rgba(255, 203, 59, 0.05) 100%);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 32px;
  height: 32px;
  color: var(--yellow);
}

.logo-text {
  text-decoration: none;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--white);
  outline: none;
}

.logo-text:focus {
  outline: none;
}

.nav-section {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.section-title {
  font-size: 12px;
  font-weight: 700;
  color: var(--light-grey);
  margin-bottom: 24px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  text-decoration: none;
  color: var(--white);
  font-size: 14px;
  font-weight: 600;
  position: relative;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-item:hover::before {
  opacity: 1;
}

.nav-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.nav-item-active {
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.2) 0%, rgba(255, 203, 59, 0.1) 100%);
  border: 1px solid rgba(255, 203, 59, 0.3);
  color: var(--yellow);
}

.nav-item-active::before {
  opacity: 1;
}

.nav-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.nav-item:hover .nav-icon {
  transform: scale(1.1);
}

.nav-icon svg {
  width: 20px;
  height: 20px;
  color: var(--white);
  transition: color 0.3s ease;
}

.nav-item-active .nav-icon svg {
  color: var(--yellow);
}

.nav-label {
  flex: 1;
  letter-spacing: 0.5px;
}

.nav-indicator {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--yellow);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-item-active .nav-indicator {
  opacity: 1;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
}

.report-bug-sidebar {
  margin-bottom: 16px;
}

.report-bug-sidebar h4 {
  font-size: 12px;
  font-weight: 700;
  color: var(--white);
  margin-bottom: 6px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.report-bug-sidebar p {
  font-size: 11px;
  color: var(--light-grey);
  line-height: 1.4;
  margin: 0;
}

.report-bug-sidebar .link {
  color: var(--yellow);
  text-decoration: none;
  font-weight: 700;
  cursor: pointer;
}

.report-bug-sidebar .link:hover {
  text-decoration: underline;
}

.version-info {
  font-size: 12px;
  color: var(--light-grey);
  text-align: center;
  opacity: 0.7;
}

.main-content {
  flex: 1;
  margin-left: 0;
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.content-expanded {
  margin-left: 0;
}

.content-with-sidebar {
  margin-left: 200px; /* Adjust based on sidebar width */
}

.content-wrapper {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  opacity: 0;
  animation: fadeIn 0.3s ease forwards;
}

@keyframes fadeIn {
  to {
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .sidebar {
    width: 100vw;
    transform: translateX(-100%);
  }

  .sidebar-trigger {
    width: 30px;
  }

  .main-content {
    margin-left: 0;
  }

  .content-wrapper {
    padding: 20px;
  }
}

.nav-section::-webkit-scrollbar {
  width: 4px;
}

.nav-section::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.nav-section::-webkit-scrollbar-thumb {
  background: rgba(255, 203, 59, 0.5);
  border-radius: 2px;
}

.nav-section::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 203, 59, 0.7);
}

.nav-item {
  animation: slideInLeft 0.5s ease forwards;
  opacity: 0;
}

.nav-item:nth-child(1) { animation-delay: 0.1s; }
.nav-item:nth-child(2) { animation-delay: 0.2s; }
.nav-item:nth-child(3) { animation-delay: 0.3s; }
.nav-item:nth-child(4) { animation-delay: 0.4s; }

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.sidebar:hover {
  box-shadow:
    4px 0 25px rgba(0, 0, 0, 0.4),
    inset -1px 0 0 rgba(255, 255, 255, 0.1);
}

.nav-item:focus {
  outline: 2px solid var(--yellow);
  outline-offset: 2px;
}

.logo-text:focus {
  outline: none;
}

.link {
  color: #00d8ff;
  cursor: pointer;
  text-decoration: underline;
}
.link:hover {
  text-decoration: none;
}
</style>
