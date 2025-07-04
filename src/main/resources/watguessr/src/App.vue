<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import { useRoute } from 'vue-router'
import { ref, computed } from 'vue';
import Header from './components/Header.vue';


const isHeaderVisible = ref(false);
const route = useRoute();

const isHomePage = computed(() => route.path === '/');
</script>
<template>
  <Header />
  <div class="main-layout">
    <div
      class="header-wrapper"
      @mouseenter="isHeaderVisible = true"
      @mouseleave="isHeaderVisible = false"
    >
      <div :class="[(isHomePage || isHeaderVisible) ? 'none' : 'header-menu website-title-container flex-container']">
        <img src="../../src/assets/images/App/location_on.png" alt="Logo" :style="{height: '40px'}"/>
        <RouterLink to="/" class="website-title"> WATGUESSR.IO</RouterLink>
      </div>

      <header :class="[(!isHomePage && !isHeaderVisible) && 'hidden-header']">
        <div class="website-title-container flex-container">
          <img src="../../src/assets/images/App/location_on.png" alt="Logo" :style="{height: '40px'}"/>
          <RouterLink to="/" class="website-title"> WATGUESSR.IO</RouterLink>
        </div>

        <h4>MAIN</h4>

        <div :class="['nav-option flex-container', route.path === '/play' && 'selected']">
          <img src="../../src/assets/images/App/play-icon.png" alt="Play"/>
          <RouterLink to="/play" id="router-link">PLAY WATGUESSR</RouterLink>
        </div>

        <div :class="['nav-option flex-container', route.path === '/leaderboard' && 'selected']">
          <img src="../../src/assets/images/App/leaderboard-icon.png" alt="Leaderboard"/>
          <RouterLink to="/leaderboard" id="router-link">LEADERBOARD</RouterLink>
        </div>

        <div :class="['nav-option flex-container', route.path === '/profile' && 'selected']">
          <img src="../../src/assets/images/App/profile-icon.png" alt="Profile"/>
          <RouterLink to="/profile" id="router-link">PROFILE</RouterLink>
        </div>

        <div :class="['nav-option flex-container', route.path === '/settings' && 'selected']">
          <img src="../../src/assets/images/App/settings-icon.png" alt="Settings"/>
          <RouterLink to="/settings" id="router-link">SETTINGS</RouterLink>
        </div>
      </header>
    </div>

  </div>
  <div :class="['main-content', (isHomePage) ? 'narrow' : 'full']">
    <RouterView />
  </div>
</template>

<style scoped>
header {
  position: fixed;
  width: 30vw;
  height: 100vh;
  box-shadow: 1px 4px 20px rgba(0, 0, 0, 0.25);
  background-color: var(--dark-grey);
}

.main-layout {
  display: flex;
}

.website-title-container {
  align-items: baseline;
}

.website-title {
  font-size: 24px;
  font-weight: bold;
}

h4 {
  font-weight: bold;
  color: var(--color-zinc-400);
  font-size: 16px;
  margin-top: 50px;
}

.header-menu {
  padding: 20px;
  background: var(--dark-grey);
  border-radius: 0 0 30px 0;
  align-items: baseline;
}

.nav-option {
  margin: 10px;
  height: 30px;
  align-items: center;
  gap: 26px;
  padding: 20px;
  border-radius: 5px;
}

.nav-option > img {
  height: 30px;
}

.nav-option:hover, .nav-option.selected {
  box-shadow: inset 0 0 0 2px white;
  background-color: rgba(139, 164, 177, 0.27);
}

.main-content {
  width: 100vw;
  transition: width 0.3s ease;
  padding: 50px;
}

.main-content.full {
  width: 100vw;
  transition: width 0.3s ease;
  padding: 100px;
}

.main-content.narrow {
  width: 70vw;
  margin-left: 30vw;
  transition: width 0.3s ease;
}

.header-wrapper {
  z-index: 999;
}

.hidden-header {
  height: 100%;
  transform: translateX(-100%);
  transition: transform 0.3s ease-in-out;
  box-shadow: 1px 4px 20px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  z-index: 1000;
  position: absolute;
  left: 0;
  top: 0;
}

/* When hovering over header wrapper, shrink main content */
.header-wrapper:hover + .main-content {
  width: 70vw;
  margin-left: 30vw;
}
</style>
