<template>
  <div class="header-container">


    <div class="streak-container flex-container">
      <div class="streak-glow">
        <img src="../assets/images/Header/streak-icon.png" alt="Streak" />
      </div>
      <p>{{ getStreak }}</p>
    </div>

    <div class="profile-container flex-container" @click="dropdownOpen = !dropdownOpen">
      <font-awesome-icon icon="user" class="profile-icon" :style="{ background: profileColors.bg, color: profileColors.fg }" />
      <p>{{ getUserName }}</p>

      <font-awesome-icon icon="chevron-down" class="dropdown-icon" />
    </div>

     <div v-if="dropdownOpen" class="dropdown-menu">
        <ul>
          <template v-if="loggedIn">
            <li @click="handleProfile">Profile</li>
            <li @click="handleSettings">Settings</li>
            <li @click="handleLogout">Log Out</li>
          </template>
          <template v-else>
            <li @click="handleLogin">Log in</li>
            <li @click="handleSignUp">Sign up</li>
          </template>
        </ul>
      </div>

    <!-- Side Menu for Mobile/Tablet -->
    <div v-if="sideMenuOpen" class="side-menu-overlay" @click="closeSideMenu"></div>
    <div class="side-menu" :class="{ 'side-menu-open': sideMenuOpen }">
      <div class="side-menu-header">
        <div class="logo-container">
          <font-awesome-icon icon="map-marker-alt" class="logo-icon" />
          <span class="logo-text">WATGUESSR.IO</span>
        </div>
        <button class="close-menu-btn" @click="closeSideMenu">
          <font-awesome-icon icon="times" />
        </button>
      </div>
      <nav class="side-menu-nav">
        <ul>
          <li @click="navigateTo('home')">Home</li>
          <li @click="navigateTo('leaderboard')">Leaderboard</li>
          <li @click="navigateTo('profile')">Profile</li>
          <template v-if="loggedIn">
            <li @click="handleLogout">Log Out</li>
          </template>
          <template v-else>
            <li @click="handleLogin">Log in</li>
            <li @click="handleSignUp">Sign up</li>
          </template>
        </ul>
      </nav>
    </div>

    <AuthModalManager
      :showLogin="showLogin"
      :showSignUp="showSignUp"
      @closeLogin="showLogin = false"
      @closeSignUp="showSignUp = false"
      @openLogin="() => { showLogin = true; showSignUp = false }"
      @openSignUp="() => { showSignUp = true; showLogin = false }"
    />

  </div>
</template>
<script>
import { mapGetters, mapActions } from 'vuex';
import AuthModalManager from '@/views/auth/AuthModalManager.vue';
import { colorPairFromName } from '@/utils/color';

export default {
  components: { AuthModalManager },

  data() {
    return {
      dropdownOpen: false,
      showLogin: false,
      showSignUp: false,
      showProfile: false,
      sideMenuOpen: false,
    };
  },

  computed: {
    ...mapGetters('user', [
      'getCurrentUser'
    ]),

    getUserName() {
      return this.getCurrentUser?.username || 'Guest';
    },

    getStreak() {
      return this.getCurrentUser?.streak || 0;
    },

    loggedIn() {
      return !!this.getCurrentUser;
    },

    profileColors() {
      const name = this.getCurrentUser?.username || 'Guest';
      return colorPairFromName(name, { bgSaturation: 90, bgLightness: 80, fgSaturation: 100, fgLightness: 30, fgHueShift: -12 });
    }
  },

  methods: {
    ...mapActions('user', ['fetchUserById', 'logout']),

    handleSettings() {
      this.dropdownOpen = false;
      if (this.loggedIn) {
        this.$router.push({ name: 'settings' });
      } else {
        this.showLogin = true;
      }
    },

    handleProfile() {
      this.dropdownOpen = false;
      const userId = this.getCurrentUser?.id;
      if (userId) {
        this.$router.push({ name: 'profile', params: { userId } });
      } else {
        this.$router.push({ name: 'profile' });
      }
    },

    handleLogout() {
      this.logout();
      this.dropdownOpen = false;
    },

    handleLogin() {
      this.showLogin = true;
      this.dropdownOpen = false;
    },

    handleSignUp() {
      this.showSignUp = true;
      this.dropdownOpen = false;
    },



    closeSideMenu() {
      this.sideMenuOpen = false;
    },

    navigateTo(routeName) {
      this.closeSideMenu();
      if (routeName === 'profile') {
        const userId = this.getCurrentUser?.id;
        if (userId) {
          this.$router.push({ name: 'profile', params: { userId } });
        } else {
          this.$router.push({ name: 'profile' });
        }
      } else {
        this.$router.push({ name: routeName });
      }
    },

    openSideMenuFromLogo() {
      this.sideMenuOpen = true;
    },


    onClickOutside(event) {
      const dropdown = this.$el.querySelector('.dropdown-menu');
      const profile = this.$el.querySelector('.profile-container');
      if (
        dropdown &&
        !dropdown.contains(event.target) &&
        profile &&
        !profile.contains(event.target)
      ) {
        this.dropdownOpen = false;
      }
    },
  },
  mounted() {
    this.fetchUserById();
    document.addEventListener('click', this.onClickOutside);

    // Listen for logo click events from App.vue
    window.addEventListener('openSideMenu', this.openSideMenuFromLogo);
  },

  beforeUnmount() {
    document.removeEventListener('click', this.onClickOutside);
    window.removeEventListener('openSideMenu', this.openSideMenuFromLogo);
  }
};
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
  padding: 5px 16px;
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
  right: 16px;
  background: rgba(42, 42, 44, 0.65);
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
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  letter-spacing: 0.9px;
  color: var(--white);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.dropdown-menu li:hover {
  background: rgba(255, 255, 255, 0.1);
}

.dropdown-menu li:active {
  background: rgba(255, 255, 255, 0.15);
}

.streak-glow {
  position: relative;
  display: inline-block;
  border-radius: 50%;
  transition: box-shadow 0.2s;
}
.streak-glow::after {
  content: '';
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 70px;
  height: 70px;
  background: radial-gradient(circle, rgba(255,140,0,0.35) 0%, rgba(255,140,0,0) 70%);
  opacity: 0;
  pointer-events: none;
  z-index: 0;
  transition: opacity 0.2s;
}
.streak-glow:hover::after {
  opacity: 1;
}
.streak-glow img {
  display: block;
  border-radius: 50%;
  position: relative;
  z-index: 1;
}

.streak-container img:hover {
  box-shadow: none;
  background: none;
  border-radius: 0;
  transition: none;
}

/* Responsive Design */
@media (max-width: 768px) {
  .header-container {
    gap: 20px;
    padding: 16px;
  }

  .streak-container img {
    height: 24px;
    width: 20px;
  }

  .streak-container p {
    font-size: 12px;
  }

  .profile-container {
    gap: 8px;
    padding: 6px 12px;
  }

  .profile-icon {
    height: 20px;
    width: 20px;
    padding: 6px;
  }

  .profile-container p {
    font-size: 12px;
    letter-spacing: 0.3px;
  }

  .dropdown-icon {
    height: 14px;
    width: 14px;
  }

  .dropdown-menu {
    top: 60px;
    right: 12px;
    min-width: 140px;
  }

  .dropdown-menu li {
    padding: 10px 14px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .header-container {
    gap: 16px;
    padding: 12px;
  }

  .streak-container {
    gap: 6px;
  }

  .streak-container img {
    height: 20px;
    width: 18px;
  }

  .streak-container p {
    font-size: 11px;
  }

  .profile-container {
    gap: 6px;
    padding: 6px 8px;
  }

  .profile-icon {
    height: 18px;
    width: 18px;
    padding: 5px;
  }

  .profile-container p {
    font-size: 11px;
    letter-spacing: 0.2px;
  }

  .dropdown-icon {
    height: 12px;
    width: 12px;
  }

  .dropdown-menu {
    top: 55px;
    right: 8px;
    min-width: 120px;
    padding: 6px;
  }

  .dropdown-menu li {
    padding: 8px 12px;
    font-size: 12px;
  }
}

@media (max-width: 360px) {
  .header-container {
    gap: 12px;
    padding: 10px;
  }

  .streak-container img {
    height: 18px;
    width: 16px;
  }

  .streak-container p {
    font-size: 10px;
  }

  .profile-container {
    gap: 4px;
    padding: 10px 6px;
  }

  .profile-icon {
    height: 16px;
    width: 16px;
    padding: 4px;
  }

  .profile-container p {
    font-size: 10px;
  }

  .dropdown-menu {
    top: 50px;
    right: 6px;
    min-width: 100px;
  }

  .dropdown-menu li {
    padding: 6px 10px;
    font-size: 11px;
  }
}

/* Landscape orientation adjustments for mobile */
@media (max-height: 500px) and (orientation: landscape) {
  .header-container {
    padding: 12px 24px;
  }

  .dropdown-menu {
    top: 50px;
  }
}

/* High DPI displays */
@media (-webkit-min-device-pixel-ratio: 2), (min-resolution: 192dpi) {
  .streak-container img,
  .profile-icon,
  .dropdown-icon {
    image-rendering: -webkit-optimize-contrast;
    image-rendering: crisp-edges;
  }
}

/* Dark mode support for systems that prefer it */
@media (prefers-color-scheme: dark) {
  .dropdown-menu {
    background: rgba(20, 20, 22, 0.85);
    border: 1px solid rgba(255, 255, 255, 0.15);
  }

  .dropdown-menu li:hover {
    background: rgba(255, 255, 255, 0.15);
  }
}

/* Reduced motion for accessibility */
@media (prefers-reduced-motion: reduce) {
  .profile-container,
  .dropdown-icon,
  .dropdown-menu li,
  .streak-glow {
    transition: none;
  }

  .profile-container:hover .dropdown-icon {
    transform: none;
  }
}



/* Side Menu Styles */
.side-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  backdrop-filter: blur(4px);
}

.side-menu {
  position: fixed;
  top: 0;
  left: -300px;
  width: 300px;
  height: 100vh;
  background: rgba(42, 42, 44, 0.65);
  backdrop-filter: blur(8px);
  border-right: 1px solid rgba(255, 255, 255, 0.1);
  z-index: 1000;
  transition: left 0.3s ease;
  overflow-y: auto;
}

.side-menu.side-menu-open {
  left: 0;
}

.side-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, rgba(255, 203, 59, 0.1) 0%, rgba(255, 203, 59, 0.05) 100%);
}

.side-menu-header .logo-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.side-menu-header .logo-icon {
  width: 32px;
  height: 32px;
  color: var(--yellow);
}

.side-menu-header .logo-text {
  text-decoration: none;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.5px;
  color: var(--white);
  outline: none;
}

.close-menu-btn {
  background: none;
  border: none;
  color: var(--white);
  font-size: 1.2rem;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background 0.2s ease;
}

.close-menu-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.side-menu-nav ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.side-menu-nav li {
  padding: 16px 20px;
  color: var(--white);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-weight: 600;
  font-size: 14px;
  position: relative;
  overflow: hidden;
}

.side-menu-nav li::before {
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

.side-menu-nav li:hover::before {
  opacity: 1;
}

.side-menu-nav li:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.side-menu-nav li:active {
  background: rgba(255, 255, 255, 0.15);
}

/* Responsive Design */
@media (max-width: 1024px) {
  .side-menu {
    width: 280px;
    left: -280px;
  }

  .side-menu-header {
    padding: 16px;
  }

  .side-menu-header .logo-icon {
    width: 28px;
    height: 28px;
  }

  .side-menu-header .logo-text {
    font-size: 20px;
  }

  .side-menu-nav li {
    padding: 14px 16px;
    font-size: 0.95rem;
  }
}

@media (max-width: 480px) {
  .side-menu {
    width: 260px;
    left: -260px;
  }

  .side-menu-header {
    padding: 14px;
  }

  .side-menu-header .logo-icon {
    width: 24px;
    height: 24px;
  }

  .side-menu-header .logo-text {
    font-size: 18px;
  }

  .side-menu-nav li {
    padding: 12px 14px;
    font-size: 0.9rem;
  }
}

@media (max-width: 360px) {
  .side-menu {
    width: 240px;
    left: -240px;
  }

  .side-menu-header {
    padding: 12px;
  }

  .side-menu-header .logo-icon {
    width: 22px;
    height: 22px;
  }

  .side-menu-header .logo-text {
    font-size: 16px;
  }

  .side-menu-nav li {
    padding: 10px 12px;
    font-size: 0.85rem;
  }
}
</style>
