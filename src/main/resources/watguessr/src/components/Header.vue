<template>
    <div class="header-container">
        <div class="streak-container flex-container">
            <img src="../assets/images/Header/streak-icon.png" alt="Streak" />
            <p>1</p>
        </div>

        <div class="profile-container flex-container" @click="dropdownOpen = !dropdownOpen">
            <img src="../assets/images/Header/generic-avatar.png" alt="Profile Icon" />
            <p>RACHEL W</p>
            <img src="../assets/images/Header/drop-down.png" alt="Drop down" />
        </div>

        <div v-if="dropdownOpen" class="dropdown-menu" @click.outside="dropdownOpen = false">
          <ul>
            <li @click="handleSettings">Settings</li>
            <li @click="handleLogout">Log Out</li>
            <li @click="handleQuit">Quit Game</li>
          </ul>
        </div>
    </div>
</template>
<script setup>
import {onBeforeUnmount, onMounted, ref} from "vue";

const dropdownOpen = ref(false);

function handleSettings() {
  console.log('Navigating to settings...');
  dropdownOpen.value = false;
}

function handleLogout() {
  console.log('Logging out...');
  dropdownOpen.value = false;
}

function handleQuit() {
  console.log('Quitting game...');
  dropdownOpen.value = false;
}

function onClickOutside(event) {
  const dropdown = document.querySelector('.dropdown-container');
  if (dropdown && !dropdown.contains(event.target))
  {
    dropdownOpen.value = false;
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
    gap: 50px;
    position: absolute;
    top: 0;
    right: 0;
    padding: 30px;
}

.streak-container {
    align-items: center;
    gap: 10px;
}

.streak-container p {
    font-weight: bold;
    font-size: 16px;
    background: var(--player-1-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.profile-container {
    align-items: center;
    gap: 10px;
  cursor: pointer;
}

.profile-container p {
    font-weight: bold;
    font-size: 16px;
}

.profile-container img {
    height: 36px;
}

button {
  cursor: pointer
}

.dropdown-container {
  position: relative;
  display: inline-block;
}

.dropdown-toggle {
  padding: 10px;
  font-size: 18px;
  border: none;
  background: var(--dark-grey);
  color: white;
  cursor: pointer;
  border-radius: 4px;
}

.dropdown-menu {
  position: absolute;
  top: 80px;
  right: 30px;
  background: var(--dark-grey);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  border-radius: 6px;
  padding: 10px;
  z-index: 999;
}

.dropdown-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.dropdown-menu li {
  padding: 10px 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.dropdown-menu li:hover {
  background: #f0f0f0;
  color: black;
}
</style>
