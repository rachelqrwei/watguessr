<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header>
    <img alt="Vue logo" class="logo" src="@/assets/logo.svg" width="125" height="125" />

    <div class="wrapper">
      <!-- <HelloWorld msg="You did it!" /> -->

      <nav>
        <RouterLink to="/">Home</RouterLink>
        <RouterLink to="/about">About</RouterLink>
      </nav>
    </div>
  </header>

  <RouterView />

</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Istok+Web:ital,wght@0,400;0,700;1,400;1,700&display=swap');

:root {
  --dark-grey: #2A2A2C;
  --yellow: #FFCB3B;
  --light-grey: #9C9C9C;
  --white: #FEFAF8;
  --player-1-gradient: linear-gradient(#7FB9FF, #AA7FFF);
  --player-2-gradient: linear-gradient(#FFE37F, #FF7F7F);
}

body {
  font-family: "Istok Web", sans-serif;
  font-weight: 400;
  font-style: normal;
}
</style>
