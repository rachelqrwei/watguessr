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

  watch: {
    loggedIn(newVal) {
      console.log('User login status changed:', newVal);
    },

    getCurrentUser(newUser, oldUser) {
      if (newUser && !oldUser) {
        console.log('✅ User logged in:', newUser.username);
      } else if (!newUser && oldUser) {
        console.log('👋 User logged out');
      }
    }
  },

  mounted() {
    this.fetchUserById();
    document.addEventListener('click', this.onClickOutside);
  },

  beforeUnmount() {
    document.removeEventListener('click', this.onClickOutside);
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
</style>
