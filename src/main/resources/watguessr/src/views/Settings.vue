<template>

  <div class="profile-background" aria-hidden="true"></div>
  <div class="profile-view">

    <div v-if="!isAuthenticated" class="auth-placeholder">
      <div class="auth-placeholder-content">
        <p>You need to be signed in to access your account settings.</p>
      </div>
    </div>

    <div v-else-if="isLoading" class="loading">
      <div class="loading-spinner"></div>
      <p>Loading settings...</p>
    </div>

    <div v-else-if="errorMessage" class="error">{{ errorMessage }}</div>

    <transition name="fade-slide" mode="out-in">
      <div v-if="isAuthenticated && !isLoading && !errorMessage" key="settings-card" class="profile">
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
                    <input type="text"
                           v-model="newUsername"
                           :placeholder="settings?.username || 'Enter new username'"
                           :disabled="!isEditingUsername"
                    />
                    <button class="edit-inline-btn" @click="toggleUsernameEditing" title="Edit username">
                      <font-awesome-icon :icon="isEditingUsername ? 'fa-solid fa-check' : 'fa-solid fa-pen'" />
                      <span class="icon-btn-text">{{ isEditingUsername ? 'SAVE' : 'EDIT' }}</span>
                    </button>
                  </div>
                  <button class="change-password-btn" @click="togglePasswordEditing">Change Password</button>
                  <!-- Username error message -->
                  <div v-if="showErrorMessage" class="username-error-message">
                    {{ errorMessage }}
                  </div>

                </div>

              </div>
            </div>
          </div>

          <!-- Password change section -->
          <div v-if="isEditingPassword" class="form-row">
            <label>New Password</label>
            <div class="password-input-group">
              <input type="password" v-model="newPassword" placeholder="Enter new password" />
              <button class="save-password-btn" @click="onChangePassword">Save Password</button>
              <button class="cancel-password-btn" @click="cancelPasswordEditing">Cancel</button>
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
      errorMessage: null,
      newUsername: '',
      newPassword: '',
      isEditingUsername: false,
      isEditingPassword: false,
    }
  },
  computed: {
    ...mapGetters('user', ['getCurrentUser', 'isAuthenticated']),
    avatarColors() {
      const name = this.settings?.username || 'Guest'
      return colorPairFromName(name, { bgSaturation: 90, bgLightness: 80, fgSaturation: 100, fgLightness: 30, fgHueShift: -12 })
    },
    showErrorMessage() {
      return this.errorMessage && this.errorMessage.trim() !== ''
    }
  },
  methods: {
    ...mapActions('user', ['fetchUserSettings', 'changeUsername', 'deleteUser', 'changePassword']),

    async loadSettings() {
      try {
        this.isLoading = true
        this.errorMessage = null
        const res = await this.fetchUserSettings(this.getCurrentUser.id)
        this.settings = res
        this.newUsername = res.username || ''
      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to load settings'
      } finally {
        this.isLoading = false
      }
    },

    toggleUsernameEditing() {
      if (this.isEditingUsername) {
        this.saveUsername()
      } else {
        this.isEditingUsername = true
        this.errorMessage = null
        this.newUsername = this.settings?.username || ''
      }
    },

    async saveUsername() {
      if (!this.newUsername || this.newUsername.trim() === '') {
        this.errorMessage = 'Username cannot be empty'
        return
      }

      try {
        this.isLoading = true
        this.errorMessage = null


        const res = await this.changeUsername({
          emailAddress: this.settings.emailAddress,
          newUsername: this.newUsername.trim()
        })

        // Update local settings
        if (res) {
          this.settings = {...this.settings, username: this.newUsername.trim()}
          this.isEditingUsername = false
          this.$toast?.success?.('Username updated successfully') || console.log('Username updated successfully')
        }
      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to change username'
        this.newUsername = this.settings.username
      } finally {
        this.isLoading = false
      }
    },

    togglePasswordEditing() {
      this.isEditingPassword = true
      this.newPassword = ''
    },

    cancelPasswordEditing() {
      this.isEditingPassword = false
      this.newPassword = ''
    },

    async onChangePassword() {
      if (!this.newPassword || this.newPassword.trim() === '') {
        this.errorMessage = 'Password cannot be empty'
        return
      }

      try {
        this.isLoading = true
        this.errorMessage = null

        await this.changePassword({
          emailAddress: this.settings.emailAddress,
          newPassword: this.newPassword
        })

        this.isEditingPassword = false
        this.newPassword = ''

        // Show success message
        this.$toast?.success?.('Password changed successfully') || console.log('Password changed successfully')

      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to change password'
        this.newPassword =  ''
      } finally {
        this.isLoading = false
      }
    },

    async onDeleteUser() {
      if (!confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
        return
      }

      try {
        this.isLoading = true
        this.errorMessage = null

        await this.deleteUser({
          emailAddress: this.settings.emailAddress
        })

        // Redirect to home page after successful deletion
        this.$router.push('/')

      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to delete user'
      } finally {
        this.isLoading = false
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
    if (this.isAuthenticated && this.getCurrentUser) {
      this.loadSettings()
    } else if (!this.isAuthenticated) {
      this.isLoading = false
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

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 227, 127, 0.3);
  border-top: 3px solid #FFE37F;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.auth-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
}

.auth-placeholder-content {
  text-align: center;
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 18px;
  padding: 40px 30px;
  backdrop-filter: blur(8px);
  max-width: 400px;
}

.placeholder-icon {
  font-size: 3rem;
  color: var(--white);
  margin-bottom: 20px;
}

.auth-placeholder-content h2 {
  color: var(--white);
  margin: 0 0 12px 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.auth-placeholder-content p {
  color: var(--light-grey);
  margin: 0;
  line-height: 1.5;
  font-style: italic;
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

.password-input-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.password-input-group input {
  flex: 1;
}

.save-password-btn,
.cancel-password-btn {
  white-space: nowrap;
  height: 40px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 12px;
  text-transform: uppercase;
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease, color 180ms ease;
}

.save-password-btn {
  background: rgba(76, 175, 80, 0.12);
  border: 1px solid #4CAF50;
  color: #4CAF50;
}

.save-password-btn:hover {
  background: rgba(76, 175, 80, 0.2);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.12);
  transform: translateY(-1px);
}

.cancel-password-btn {
  background: rgba(158, 158, 158, 0.12);
  border: 1px solid #9E9E9E;
  color: #9E9E9E;
}

.cancel-password-btn:hover {
  background: rgba(158, 158, 158, 0.2);
  box-shadow: 0 0 0 3px rgba(158, 158, 158, 0.12);
  transform: translateY(-1px);
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

.username-error-message {
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
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 12px;
  text-transform: uppercase;
  margin-top: 8px;
  margin-left: 84px; /* Align with the username input field */
}

.username-error-message:hover {
  background: rgba(255, 127, 127, 0.2);
  box-shadow: 0 0 0 3px rgba(255, 127, 127, 0.12);
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


