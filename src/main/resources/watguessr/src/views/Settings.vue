<template>

  <div class="profile-background" aria-hidden="true"></div>
  <div class="profile-view">

    <div v-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading settings...</p>
    </div>

    <div v-else-if="errorMessage" class="error">{{ errorMessage }}</div>

    <transition name="fade-slide" mode="out-in">
      <div v-if="!isLoading && !errorMessage" key="settings-card" class="profile">
        <div class="profile-content single-column">
          <div class="hero">
            <div class="hero-info">
              <h1 class="name"><font-awesome-icon icon="fa-solid fa-cog" class="cog-icon" /> SETTINGS</h1>
            </div>
          </div>

          <div class="card">
          <!-- <div class="card-label">Account</div> -->

          <div class="profile-username-row">
            <div class="avatar-wrap">
              <div class="avatar" :style="{ background: avatarColors.bg, color: avatarColors.fg }" aria-hidden="true">
                {{ (settings?.username || 'G')[0].toUpperCase() }}
              </div>
            </div>
            <div class="username-actions">
              <div class="form-row compact username-field">
                <label>Username</label>
                <div class="username-line">
                  <div class="input-with-icon">
                    <input type="text" :value="settings?.username" disabled />
                    <button class="edit-inline-btn" title="Edit username">
                      <font-awesome-icon icon="fa-solid fa-pen" />
                      <span class="icon-btn-text">EDIT</span>
                    </button>
                  </div>
                  <button class="change-password-btn" @click="onChangePassword">Change Password</button>
                </div>
              </div>
            </div>
          </div>

          <div class="form-row">
            <label>Email</label>
            <input type="email" :value="settings?.emailAddress" disabled />
          </div>
          <div class="form-row">
            <label>Created</label>
            <input type="text" :value="formatDate(settings?.createdAt)" disabled />
          </div>

          <div class="settings-actions-bottom">
            <button class="delete-user-btn" @click="onDeleteUser">Delete User</button>
          </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import { colorPairFromName } from '@/utils/color'

export default {
  name: 'SettingsView',
  data() {
    return {
      settings: null,
      isLoading: true,
      errorMessage: null
    }
  },
  computed: {
    ...mapGetters('user', ['getCurrentUser']),
    avatarColors() {
      const name = this.settings?.username || 'Guest'
      return colorPairFromName(name, { bgSaturation: 90, bgLightness: 80, fgSaturation: 100, fgLightness: 30, fgHueShift: -12 })
    }
  },
  methods: {
    ...mapActions('user', ['fetchUserSettings']),
    async loadSettings() {
      try {
        this.isLoading = true
        this.errorMessage = null
        const res = await this.fetchUserSettings(this.getCurrentUser.id)
        this.settings = res
      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to load settings'
      } finally {
        this.isLoading = false
      }
    },
    onChangePassword() {
      // TODO: Hook up to change password flow when available
      alert('Change password is not implemented yet.');
    },
    onDeleteUser() {
      // TODO: Wire to backend delete endpoint with confirmation
      if (confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
        alert('Delete user is not implemented yet.');
      }
    },
    formatDate(iso) {
      if (!iso) return ''
      try {
        const d = new Date(iso)
        return d.toLocaleString()
      } catch (_) {
        return String(iso)
      }
    }
  },
  mounted() {
    if (this.getCurrentUser) {
      this.loadSettings()
    }
  }
}
</script>

<style>
/* Global custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 227, 127, 0.05);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #FFE37F;
  border-radius: 4px;
  opacity: 0.8;
}

::-webkit-scrollbar-thumb:hover {
  background: #FFE37F;
  opacity: 1;
}
</style>

<style scoped>
.profile-background {
  position: fixed;
  inset: 0;
  background: var(--dark-grey);
  z-index: -1;
}

.profile-background::after {
  content: '';
  position: absolute;
  inset: 0;
  background: url('/ProfilePage.png') center top / cover no-repeat;
  opacity: 0.8;
  pointer-events: none;
}

.profile-view {
  max-width: 880px;
  margin: 0 auto;
  padding: 80px 20px 30px;
  color: var(--white);
  position: relative;
  min-height: 100vh;
}

.settings-header {
  text-align: center;
  margin-bottom: 16px;
}

.subtitle {
  color: var(--light-grey);
  margin-top: 4px;
}

.loading,
.error,
.empty {
  text-align: center;
  color: var(--light-grey);
}

.profile {
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  overflow: hidden;
  backdrop-filter: blur(8px);
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: start;
  padding: 22px 22px 22px 22px;
}

.profile-content.single-column {
  grid-template-columns: 1fr;
  gap: 10px;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0;
  background: none;
}

.avatar-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--yellow);
  color: var(--dark-grey);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1.6rem;
  box-shadow: none;
}

.hero-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-left: 14px;
}

.name {
  margin: 0 0 6px 0;
  font-size: 1.8rem;
  font-weight: 800;
  letter-spacing: 0.2px;
}

.cog-icon {
  margin-right: 10px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--light-grey);
}

.card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  padding: 20px 16px;
}

.profile-username-row {
  display: grid;
  grid-template-columns: 84px auto;
  gap: 16px;
  align-items: center;
  margin-bottom: 8px;
}

.username-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 420px;
  width: 100%;
}

.form-row.compact { margin: 0; }

.actions { display: flex; gap: 10px; }
.username-actions .actions { justify-content: flex-end; }

.btn {
  background: var(--yellow);
  color: #101010;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.username-field { margin-bottom: 0; }

.input-with-icon {
  position: relative;
}

.input-with-icon input {
  width: 100%;
  padding-right: 88px;
  height: 40px;
}

.edit-inline-btn {
  position: absolute;
  top: 50%;
  right: 6px;
  transform: translateY(-50%);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.2);
  color: var(--white);
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease;
}

.icon-btn-text { font-weight: 700; font-size: 12px; letter-spacing: 0.4px; }

.username-line {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  align-items: center;
}

.change-password-btn {
  white-space: nowrap;
  height: 40px;
  padding: 0 12px;
  margin-left: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 203, 59, 0.12);
  border: 1px solid var(--yellow);
  color: var(--yellow);
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 12px;
  text-transform: uppercase;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease, color 180ms ease;
}

.change-password-btn:hover {
  background: rgba(255, 203, 59, 0.2);
  box-shadow: 0 0 0 3px rgba(255, 203, 59, 0.12);
  transform: translateY(-1px);
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 8px 0;
}

label {
  color: var(--light-grey);
  font-size: 14px;
}

input[type="text"],
input[type="email"] {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  color: var(--white);
  padding: 10px 12px;
  outline: none;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  padding: 12px;
  text-align: center;
}

.stat-label {
  color: var(--light-grey);
  font-size: 12px;
}

.stat-value {
  font-weight: 600;
  font-size: 18px;
}

/* Fade + slide up transition to match Profile */
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(12px);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 300ms ease, transform 300ms ease;
}

.settings-actions-bottom {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.delete-user-btn {
  white-space: nowrap;
  height: 40px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 127, 127, 0.12);
  border: 1px solid #FF7F7F;
  color: #FF7F7F;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 12px;
  text-transform: uppercase;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease, color 180ms ease;
}

.delete-user-btn:hover {
  background: rgba(255, 127, 127, 0.2);
  box-shadow: 0 0 0 3px rgba(255, 127, 127, 0.12);
  transform: translateY(-1px);
}

.edit-inline-btn:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.35);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 18px 18px 18px 18px;
  }

  .hero { grid-template-columns: 1fr; gap: 0; }

}
</style>


