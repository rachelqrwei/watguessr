<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router';
import { useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <header>
    <div>
      <div class="flex-container">
        <img src="../../src/assets/image/location_on.png" alt="Logo"/>
        <RouterLink to="/" class="website-title">WATGUESSR.IO</RouterLink>
      </div>

      <h4>MAIN</h4>

      <div :class="['nav-option flex-container', route.path === '/play' && 'selected']">
        <img src="../../src/assets/image/play-icon.png" alt="Play"/>
        <RouterLink to="/play">PLAY WATGUESSR</RouterLink>
      </div>

      <div :class="['nav-option flex-container', route.path === '/leaderboard' && 'selected']">
        <img src="../../src/assets/image/leaderboard-icon.png" alt="Leaderboard"/>
        <RouterLink to="/leaderboard">LEADERBOARD</RouterLink>
      </div>

      <div :class="['nav-option flex-container', route.path === '/profile' && 'selected']">
        <img src="../../src/assets/image/profile-icon.png" alt="Profile"/>
        <RouterLink to="/profile">PROFILE</RouterLink>
      </div>
    
      <div :class="['nav-option flex-container', route.path === '/settings' && 'selected']">
        <img src="../../src/assets/image/settings-icon.png" alt="Settings"/>
        <RouterLink to="/settings">SETTINGS</RouterLink>
      </div>
    </div>
  </header>

  <RouterView />

</template>

<style>
h4 {
  font-weight: bold;
  color: var(--color-zinc-400);
  font-size: 16px;
  margin-top: 50px;
}

.nav-option {
  margin: 10px;
  height: 36px;
  align-items: center;
  gap: 26px;
  padding: 30px;
  border-radius: 15px;
}

.nav-option > img {
  height: 36px;
}

.nav-option > RouterLink {
  font-size: 16px;
  font-weight: bold;
}

.nav-option:hover, .nav-option.selected {
  box-shadow: inset 0 0 0 2px white;
  background-color: rgba(139, 164, 177, 0.27);
}
</style>
