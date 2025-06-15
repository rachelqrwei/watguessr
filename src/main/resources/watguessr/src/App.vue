<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header>
    <div>
      <div class="flex-container">
        <img src="../../src/assets/image/location_on.png" alt="Logo"/>
        <h1 class="website-title">WATGUESSR.IO</h1>
      </div>

      <h4>MAIN</h4>

      <div class="nav-option flex-container">
        <img src="../../src/assets/image/play-icon.png" alt="Play"/>
        <h5>PLAY WATGUESSR</h5>
      </div>

      <div>
        <div class="nav-option flex-container">
          <img src="../../src/assets/image/leaderboard-icon.png" alt="Leaderboard"/>
          <h5>LEADERBOARD</h5>
        </div>
      </div>

      <div>
        <div class="nav-option flex-container">
          <img src="../../src/assets/image/profile-icon.png" alt="Profile"/>
          <h5>PROFILE</h5>
        </div>
      </div>
      
      <div>
        <div class="nav-option flex-container">
          <img src="../../src/assets/image/settings-icon.png" alt="Settings"/>
          <h5>SETTINGS</h5>
        </div>
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

.nav-option > h5 {
  font-size: 16px;
  font-weight: bold;
}

.nav-option:hover, .nav-option.selected {
  box-shadow: inset 0 0 0 2px white;
  background-color: rgba(139, 164, 177, 0.27);
}
</style>
