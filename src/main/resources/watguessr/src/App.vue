<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <header>
    <div class="website-title-container flex-container">
      <img src="../../src/assets/images/App/location_on.png" alt="Logo" :style="{height: '50px'}"/>
      <RouterLink to="/" class="website-title">WATGUESSR.IO</RouterLink>
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

  <div class="main-content">
    <RouterView />
  </div>
  

</template>

<style scoped>
header {
  position: fixed;
  width: 30vw;
  height: 100vh;
  box-shadow: 1px 4px 20px rgba(0, 0, 0, 0.25);
}

.website-title-container {
  align-items: center;
}

.website-title {
  font-size: 32px;
  font-weight: bold;
}

h4 {
  font-weight: bold;
  color: var(--color-zinc-400);
  font-size: 16px;
  margin-top: 50px;
}

.nav-option {
  margin: 10px;
  height: 30px;
  align-items: center;
  gap: 26px;
  padding: 30px;
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
  position: absolute;
  width: 70vw;
  left: 30vw;
  padding: 50px;
}
</style>
