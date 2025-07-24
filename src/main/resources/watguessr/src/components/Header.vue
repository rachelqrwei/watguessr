<template>
  <div class="header-container">
    <div class="streak-container flex-container">
      <img src="../assets/images/Header/streak-icon.png" alt="Streak" />
      <p>1</p>
    </div>

    <div class="profile-container flex-container" @click="dropdownOpen = !dropdownOpen">
      <font-awesome-icon icon="user" class="profile-icon" />
      <p>RACHEL W</p>
      <font-awesome-icon icon="chevron-down" class="dropdown-icon" />
    </div>

        <div v-if="dropdownOpen" class="dropdown-menu" @click.outside="dropdownOpen = false">
          <ul>
            <li @click="handleSettings">Settings</li>
            <li @click="handleLogout" v-if="loggedIn">Log Out</li>
            <li @click="handleLogin" v-if="!loggedIn">Log in</li>
            <li @click="handleSignOut" v-if="signedIn">Sign Out</li>
            <li @click="handleSignIn" v-if="!signedIn">Sign in</li>
            <li @click="handleQuit">Quit Game</li>
          </ul>
        </div>

      <LoginModal :visible="showLoginModal" @close="showLoginModal = false" />
    </div>
</template>
<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";
import A from "@/views/auth/AuthModalManager.vue";

import {data} from "autoprefixer";

const dropdownOpen = ref(false);
const loggedIn = ref(false);
const signedIn = ref(false);
const showLoginModal = ref(false);

const handleSettings = () => {
  console.log('Navigating to settings...')
  dropdownOpen.value = false
}

const handleLogout = () => {
  console.log('Logging out...')
  dropdownOpen.value = false
}

function handleLogin() {
  console.log('Logging in...');
  dropdownOpen.value = false;
  showLoginModal.value = true;
}

const handleSignOut = () => {
  console.log('Signing out...')
  dropdownOpen.value = false
}

function handleSignIn() {
  console.log('Signing in...');
  dropdownOpen.value = false;
  showSignInModal.value = true;
}

function handleQuit() {
  console.log('Quitting game...');
  dropdownOpen.value = false;
}

const onClickOutside = (event) => {
  const dropdown = document.querySelector('.dropdown-container')
  if (dropdown && !dropdown.contains(event.target)) {
    dropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', onClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside);
});


</script>
<style scoped>
.header-container {
  display: flex;
  justify-content: flex-end;
  gap: 40px;
  position: absolute;
  top: 0;
  right: 0;
  padding: 24px;
  z-index: 1001;
}

.streak-container {
  align-items: center;
  gap: 8px;
}

.streak-container img {
  height: 28px;
  width: 24px;
}

.streak-container p {
  font-weight: 700;
  font-size: 14px;
  background: var(--player-1-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.profile-container {
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.2s ease;
}

.profile-container:hover {
  background: rgba(255, 255, 255, 0.05);
}

.profile-icon {
  height: 24px;
  width: 24px;
  color: var(--white);
  background: rgba(255, 255, 255, 0.1);
  padding: 8px;
  border-radius: 50%;
}

.dropdown-icon {
  height: 16px;
  width: 16px;
  color: var(--white);
  opacity: 0.7;
  transition: transform 0.2s ease;
}

.profile-container:hover .dropdown-icon {
  opacity: 1;
  transform: translateY(1px);
}

.profile-container p {
  font-weight: 600;
  font-size: 14px;
  color: var(--white);
  letter-spacing: 0.5px;
}

.dropdown-menu {
  position: absolute;
  top: 70px;
  right: 0;
  background: var(--dark-grey);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  border-radius: 12px;
  padding: 8px;
  z-index: 1002;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  min-width: 160px;
}

.dropdown-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.dropdown-menu li {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--white);
}

.dropdown-menu li:hover {
  background: rgba(255, 255, 255, 0.1);
}

.dropdown-menu li:active {
  background: rgba(255, 255, 255, 0.15);
}
</style>
