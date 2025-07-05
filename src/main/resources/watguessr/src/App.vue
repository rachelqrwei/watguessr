<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import Header from './components/Header.vue';

const route = useRoute();
const isHoveringHeader = ref(false);

const isHomePage = computed(() => route.path === '/');
const showHeader = computed(() => isHomePage.value || isHoveringHeader.value);

const navLinks = [
  { path: '/play', label: 'PLAY WATGUESSR', icon: 'play-icon.png' },
  { path: '/leaderboard', label: 'LEADERBOARD', icon: 'leaderboard-icon.png' },
  { path: '/profile', label: 'PROFILE', icon: 'profile-icon.png' },
  { path: '/settings', label: 'SETTINGS', icon: 'settings-icon.png' },
];
</script>

<template>
  <Header />

  <div class="layout">
    <div
      class="sidebar-container"
      @mouseenter="isHoveringHeader = true"
      @mouseleave="isHoveringHeader = false"
    >
      <div class="logo-container-container">
        <div class="logo-container">
          <img src="@/assets/images/App/location_on.png" alt="Logo" />
          <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
        </div>
      </div>

      <!-- Sidebar Section -->
      <aside
        class="sidebar"
        :class="{ hidden: !showHeader }"
      >
        <div class="logo-container">
          <img src="@/assets/images/App/location_on.png" alt="Logo" />
          <RouterLink to="/" class="logo-text">WATGUESSR.IO</RouterLink>
        </div>

        <h4 class="section-title">MAIN</h4>

        <nav>
          <div
            v-for="link in navLinks"
            :key="link.path"
            :class="['nav-item', { selected: route.path === link.path }]"
          >
            <img :src="`../../src/assets/images/App/${link.icon}`" :alt="link.label" />
            <RouterLink :to="link.path">{{ link.label }}</RouterLink>
          </div>
        </nav>
      </aside>
    </div>


    <!-- Content Section -->
    <main :class="isHomePage ? 'content-narrow' : 'content-full'">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 30vw;
  height: 100%;
  background-color: var(--dark-grey);
  position: fixed;
  top: 0;
  left: 0;
  padding: 20px;
  box-shadow: 1px 4px 20px rgba(0, 0, 0, 0.25);
  transition: transform 0.3s ease;
  z-index: 999;
}

.sidebar.hidden {
  transform: translateX(-100%);
}

.logo-container-container {
  margin: 20px;
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-text {
  font-size: 24px;
  font-weight: bold;
}

.section-title {
  margin-top: 40px;
  font-size: 16px;
  font-weight: bold;
  color: var(--color-zinc-400);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px;
  margin-top: 10px;
  border-radius: 8px;
  cursor: pointer;
}

.nav-item:hover,
.nav-item.selected {
  background-color: rgba(139, 164, 177, 0.27);
  box-shadow: inset 0 0 0 2px white;
}

.nav-item img {
  height: 24px;
}

.content-full {
  margin-left: 30vw;
  padding: 60px;
  width: calc(100vw - 30vw);
  transition: all 0.3s ease;
}

.content-narrow {
  width: 100vw;
  padding: 60px;
  transition: all 0.3s ease;
}
</style>
