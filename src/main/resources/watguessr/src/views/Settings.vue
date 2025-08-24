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
      <div
        v-if="isAuthenticated && !isLoading && !errorMessage"
        key="settings-card"
        class="profile"
      >
        <div class="profile-content single-column">
          <div class="hero">
            <div class="hero-info">
              <h1 class="name">
                <font-awesome-icon icon="fa-solid fa-cog" class="cog-icon" /> SETTINGS
              </h1>
            </div>
          </div>

          <!-- Profile Overview Card -->
          <div class="card profile-overview-card">
            <div class="profile-header">
              <div class="avatar-wrap">
                <div
                  class="avatar"
                  :style="{ background: avatarColors.bg, color: avatarColors.fg }"
                  aria-hidden="true"
                >
                  {{ (settings?.username || 'G')[0].toUpperCase() }}
                </div>
              </div>
              <div class="profile-info">
                <h2 class="username">{{ settings?.username }}</h2>
                <p class="email">{{ settings?.emailAddress }}</p>
                <p class="created-date">Member since {{ formatDate(settings?.createdAt) }}</p>
              </div>
            </div>
          </div>

          <!-- Account Security Card -->
          <div class="card account-security-card">
            <div class="card-header">
              <h3 class="card-title">
                <font-awesome-icon icon="fa-solid fa-shield-alt" class="card-icon" />
                Account Security
              </h3>
            </div>

            <!-- Username Section -->
            <div class="settings-section">
              <div class="section-header">
                <div class="section-info">
                  <h4 class="section-title">Username</h4>
                  <p class="section-description">Your display name visible to other players</p>
                </div>
                <button
                  v-if="!isEditingUsername && !isEditingPassword"
                  class="action-btn primary"
                  @click="startUsernameEditing"
                >
                  <font-awesome-icon icon="fa-solid fa-edit" />
                  Change Username
                </button>
              </div>

              <transition name="slide-down">
                <div v-if="isEditingUsername" class="edit-section">
                  <div class="edit-form">
                    <div class="input-group">
                      <div class="floating-label">
                        <input
                          type="text"
                          id="newUsername"
                          v-model="newUsername"
                          placeholder=" "
                          required
                          class="form-input"
                        />
                        <label for="newUsername">New Username</label>
                      </div>
                    </div>

                    <div class="validation-list">
                      <div class="validation-item" :class="{ valid: usernameChecks.validLengths }">
                        <font-awesome-icon
                          :icon="
                            usernameChecks.validLengths ? 'fa-solid fa-check' : 'fa-solid fa-times'
                          "
                        />
                        Between 3 to 24 characters
                      </div>
                      <div
                        class="validation-item"
                        :class="{ valid: usernameChecks.validCombination }"
                      >
                        <font-awesome-icon
                          :icon="
                            usernameChecks.validCombination
                              ? 'fa-solid fa-check'
                              : 'fa-solid fa-times'
                          "
                        />
                        No spaces allowed
                      </div>
                    </div>

                    <div class="username-warning">
                      <font-awesome-icon icon="fa-solid fa-exclamation-triangle" />
                      Username can be changed only once in 7 days.
                      {{
                        settings?.usernameChangedAt
                          ? ' Last changed: ' + formatDate(settings.usernameChangedAt)
                          : ' This is your first time changing it'
                      }}
                    </div>

                    <div class="edit-actions">
                      <button
                        class="action-btn success"
                        @click="saveUsername"
                        :disabled="isLoading || !usernameChecks.allValid"
                      >
                        <font-awesome-icon icon="fa-solid fa-check" />
                        Save Changes
                      </button>
                      <button class="action-btn secondary" @click="cancelUsernameEditing">
                        <font-awesome-icon icon="fa-solid fa-times" />
                        Cancel
                      </button>
                    </div>
                  </div>
                </div>
              </transition>
            </div>

            <!-- Password Section -->
            <div class="settings-section">
              <div class="section-header">
                <div class="section-info">
                  <h4 class="section-title">Password</h4>
                  <p class="section-description">Keep your account secure with a strong password</p>
                </div>
                <button
                  v-if="!isEditingPassword && !isEditingUsername"
                  class="action-btn primary"
                  @click="startPasswordEditing"
                >
                  <font-awesome-icon icon="fa-solid fa-key" />
                  Change Password
                </button>
              </div>

              <transition name="slide-down">
                <div v-if="isEditingPassword" class="edit-section">
                  <div class="edit-form">
                    <div class="input-group">
                      <div class="floating-label password-field">
                        <input
                          :type="showPassword ? 'text' : 'password'"
                          id="newPassword"
                          v-model="newPassword"
                          placeholder=" "
                          required
                          class="form-input"
                        />
                        <label for="newPassword">New Password</label>
                        <button
                          type="button"
                          class="password-toggle"
                          @click="showPassword = !showPassword"
                        >
                          <font-awesome-icon
                            :icon="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'"
                          />
                        </button>
                      </div>
                    </div>

                    <div class="validation-list">
                      <div class="validation-item" :class="{ valid: passwordChecks.lengthValid }">
                        <font-awesome-icon
                          :icon="
                            passwordChecks.lengthValid ? 'fa-solid fa-check' : 'fa-solid fa-times'
                          "
                        />
                        At least 8 characters
                      </div>
                      <div class="validation-item" :class="{ valid: passwordChecks.casingValid }">
                        <font-awesome-icon
                          :icon="
                            passwordChecks.casingValid ? 'fa-solid fa-check' : 'fa-solid fa-times'
                          "
                        />
                        Includes uppercase and lowercase
                      </div>
                      <div
                        class="validation-item"
                        :class="{ valid: passwordChecks.specialCharValid }"
                      >
                        <font-awesome-icon
                          :icon="
                            passwordChecks.specialCharValid
                              ? 'fa-solid fa-check'
                              : 'fa-solid fa-times'
                          "
                        />
                        Includes special character (!@#$%^&*)
                      </div>
                    </div>

                    <div class="edit-actions">
                      <button
                        class="action-btn success"
                        @click="onChangePassword"
                        :disabled="isLoading || !passwordChecks.allValid"
                      >
                        <font-awesome-icon icon="fa-solid fa-check" />
                        Save Changes
                      </button>
                      <button class="action-btn secondary" @click="cancelPasswordEditing">
                        <font-awesome-icon icon="fa-solid fa-times" />
                        Cancel
                      </button>
                    </div>
                  </div>
                </div>
              </transition>
            </div>
          </div>

          <!-- Keybinds Section -->
          <div class="card keybinds-card">
            <div class="card-header">
              <h3 class="card-title">
                <font-awesome-icon icon="fa-solid fa-keyboard" class="card-icon" />
                Keybinds
              </h3>
            </div>

            <div class="keybinds-content">
              <div class="keybind-row">
                <span class="keybind-label">Switch between Image view and Map view:</span>
                <div class="keybind-keys">
                  <div class="key-square">A</div>
                  <span class="keybind-separator">and</span>
                  <div class="key-square">D</div>
                </div>
              </div>

              <div class="keybind-row">
                <span class="keybind-label">Switch floors:</span>
                <div class="keybind-keys">
                  <div class="key-square">W</div>
                  <span class="keybind-separator">and</span>
                  <div class="key-square">S</div>
                </div>
              </div>

              <div class="keybind-row">
                <span class="keybind-label">Submit guess:</span>
                <div class="keybind-keys">
                  <div class="key-space">SPACE</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Danger Zone Card -->
          <div class="card danger-zone-card">
            <div class="card-header">
              <h3 class="card-title danger">
                <font-awesome-icon icon="fa-solid fa-exclamation-triangle" class="card-icon" />
                Danger Zone
              </h3>
            </div>
            <div class="danger-section">
              <div class="danger-info">
                <h4>Delete Account</h4>
                <p>
                  Permanently delete your account and all associated data. This action cannot be
                  undone.
                </p>
              </div>
              <button class="action-btn danger" @click="onDeleteUser">
                <font-awesome-icon icon="fa-solid fa-trash" />
                Delete Account
              </button>
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
      showPassword: false,
    }
  },
  computed: {
    ...mapGetters('user', ['getCurrentUser', 'isAuthenticated']),
    avatarColors() {
      const name = this.settings?.username || 'Guest'
      return colorPairFromName(name, {
        bgSaturation: 90,
        bgLightness: 80,
        fgSaturation: 100,
        fgLightness: 30,
        fgHueShift: -12,
      })
    },
    showErrorMessage() {
      return this.errorMessage && this.errorMessage.trim() !== ''
    },
    usernameChecks() {
      const validLengths = this.newUsername.length >= 3 && this.newUsername.length <= 24
      const validCombination = !this.newUsername.includes(' ')
      return {
        validLengths,
        validCombination,
        allValid: validLengths && validCombination,
      }
    },
    passwordChecks() {
      const lengthValid = this.newPassword.length >= 8
      const casingValid = /(?=.*[a-z])(?=.*[A-Z])/.test(this.newPassword)
      const specialCharValid = /(?=.*[!@#$%^&*])/.test(this.newPassword)
      return {
        lengthValid,
        casingValid,
        specialCharValid,
        allValid: lengthValid && casingValid && specialCharValid,
      }
    },
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

    startUsernameEditing() {
      // Prevent simultaneous editing
      if (this.isEditingPassword) {
        this.cancelPasswordEditing()
      }
      this.isEditingUsername = true
      this.errorMessage = null
      this.newUsername = this.settings?.username || ''
    },

    toggleUsernameEditing() {
      if (this.isEditingUsername) {
        this.saveUsername()
      } else {
        this.startUsernameEditing()
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
          newUsername: this.newUsername.trim(),
        })

        // Update local settings
        if (res) {
          this.settings = { ...this.settings, username: this.newUsername.trim() }
          this.isEditingUsername = false

          // Show success message
          this.$toast?.success?.('Username updated successfully') ||
            console.log('Username updated successfully')

          // Reload the page to reflect changes
          setTimeout(() => {
            window.location.reload()
          }, 1000) // Wait 1 second for user to see success message
        }
      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to change username'
        this.newUsername = this.settings.username
      } finally {
        this.isLoading = false
      }
    },

    startPasswordEditing() {
      // Prevent simultaneous editing
      if (this.isEditingUsername) {
        this.cancelUsernameEditing()
      }
      this.isEditingPassword = true
      this.newPassword = ''
      this.errorMessage = null
    },

    togglePasswordEditing() {
      if (this.isEditingPassword) {
        this.onChangePassword()
      } else {
        this.startPasswordEditing()
      }
    },

    cancelPasswordEditing() {
      this.isEditingPassword = false
      this.newPassword = ''
    },

    cancelUsernameEditing() {
      this.isEditingUsername = false
      this.newUsername = ''
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
          newPassword: this.newPassword,
        })

        this.isEditingPassword = false
        this.newPassword = ''

        // Show success message
        this.$toast?.success?.('Password changed successfully') ||
          console.log('Password changed successfully')
      } catch (err) {
        this.errorMessage = err instanceof Error ? err.message : 'Failed to change password'
        this.newPassword = ''
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
          emailAddress: this.settings.emailAddress,
        })
        this.$router.push('/')
        window.location.reload()
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
    },
  },
  mounted() {
    if (this.isAuthenticated && this.getCurrentUser) {
      this.loadSettings()
    } else if (!this.isAuthenticated) {
      this.isLoading = false
    }
  },
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
  background: #ffe37f;
  border-radius: 4px;
  opacity: 0.8;
}

::-webkit-scrollbar-thumb:hover {
  background: #ffe37f;
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
  border-top: 3px solid #ffe37f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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
  width: 200px;
}

.username-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 420px;
  width: 100%;
}

.form-row.compact {
  margin: 0;
}

.actions {
  display: flex;
  gap: 10px;
}
.username-actions .actions {
  justify-content: flex-end;
}

.btn {
  background: var(--yellow);
  color: #101010;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.username-field {
  margin-bottom: 0;
}

.input-with-icon {
  position: relative;
}

.input-with-icon input {
  width: 100%;
  padding-right: 88px;
  height: 40px;
  min-width: 250px; /* Add minimum width */
  font-size: 14px; /* Increase font size */
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
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: var(--white);
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    background 180ms ease,
    border-color 180ms ease;
}

.icon-btn-text {
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.4px;
}

.username-line {
  display: flex;
  width: 300px;
  gap: 10px;
  align-items: center;
}

.username-display {
  white-space: nowrap;
  height: 40px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 203, 59, 0.12);
  border: 1px solid var(--grey);
  color: var(--grey);
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 12px;
  text-transform: uppercase;
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    background 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
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
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    background 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
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
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    background 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
}

.save-password-btn {
  background: rgba(76, 175, 80, 0.12);
  border: 1px solid #4caf50;
  color: #4caf50;
}

.save-password-btn:hover {
  background: rgba(76, 175, 80, 0.2);
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.12);
  transform: translateY(-1px);
}

.cancel-password-btn {
  background: rgba(158, 158, 158, 0.12);
  border: 1px solid #9e9e9e;
  color: #9e9e9e;
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

input[type='text'],
input[type='email'] {
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
  transition:
    opacity 300ms ease,
    transform 300ms ease;
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
  border: 1px solid #ff7f7f;
  color: #ff7f7f;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 12px;
  text-transform: uppercase;
  transition:
    transform 180ms ease,
    box-shadow 180ms ease,
    background 180ms ease,
    border-color 180ms ease,
    color 180ms ease;
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
  border: 1px solid #ff7f7f;
  color: #ff7f7f;
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

.username-caution-message {
  white-space: nowrap;
  height: 20px;
  padding: 0 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 203, 59, 0.12);
  border: 1px solid var(--yellow);
  color: var(--yellow);
  border-radius: 6px;
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 9px;
  text-transform: uppercase;
  margin-top: 8px;
  margin-left: 0; /* Align with the username input field */
}

.username-caution-message:hover {
  background: rgba(255, 203, 59, 0.2);
  box-shadow: 0 0 0 3px rgba(255, 203, 59, 0.12);
}

/* Keybinds Section Styles */
.keybinds-card {
  margin-bottom: 24px;
}

.keybinds-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.keybind-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 0;
}

.keybind-label {
  color: var(--white);
  font-size: 14px;
  font-weight: 500;
  flex: 1;
}

.keybind-keys {
  display: flex;
  align-items: center;
  gap: 8px;
}

.keybind-separator {
  color: var(--light-grey);
  font-size: 12px;
  font-weight: 500;
}

.key-square {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--white);
  font-weight: 700;
  font-size: 14px;
  text-transform: uppercase;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
}

.key-square:hover {
  background: rgba(255, 227, 127, 0.15);
  border-color: var(--yellow);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

.key-space {
  min-width: 80px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--white);
  font-weight: 700;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
  padding: 0 12px;
}

.key-space:hover {
  background: rgba(255, 227, 127, 0.15);
  border-color: var(--yellow);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 18px 18px 18px 18px;
  }

  .hero {
    grid-template-columns: 1fr;
    gap: 0;
  }

  .keybind-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .keybind-keys {
    align-self: flex-end;
  }
}

.password-checklist {
  margin-top: 1.2rem;
  margin-bottom: 1rem;
  list-style: none;
  padding-left: 0;
  font-size: 0.85rem;
  color: #ccc;
  width: 500px;
}

.password-checklist li {
  margin-bottom: 0.25rem;
}

.password-checklist li.valid {
  color: #b6ff7f;
}

.toggle-eye {
  position: absolute;
  right: 10px;
  top: 17%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #ccc;
  font-size: 1rem;
  user-select: none;
  z-index: 3;
  margin-right: 60px;
}

/* Wrap the password input for eye toggle */
.password-wrapper {
  position: relative;
  width: 100%;
}

.password-wrapper input {
  width: 100%;
  padding-right: 2.5rem; /* reserve space for the eye */
  background: rgba(255, 255, 255, 0.08); /* Match username input background */
  border: 1px solid rgba(255, 255, 255, 0.12); /* Match username input border */
  color: var(--white); /* Match username input text color */
  border-radius: 8px; /* Match username input border radius */
  padding: 10px 12px; /* Match username input padding */
  font-size: 14px; /* Match username input font size */
  height: 40px; /* Match username input height */
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.password-wrapper input:focus {
  outline: none;
  border-color: var(--yellow); /* Use yellow focus color to match theme */
  background: rgba(255, 255, 255, 0.12); /* Slightly lighter on focus */
}

/* Eye toggle button */
.toggle-eye {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #ccc;
  font-size: 1rem;
  user-select: none;
  z-index: 3;
}

/* Floating label styling (optional, if you want labels inside input) */
.floating-label {
  position: relative;
}

.floating-label input {
  padding: 1.1rem 0.6rem 0.4rem;
  height: 3rem;
  background: #3a3a3a;
  border: 1px solid #555;
  border-radius: 6px;
  color: #aaa;
  font-size: 1rem;
}

.floating-label label {
  position: absolute;
  top: 0.9rem;
  left: 0.75rem;
  color: #cccccc;
  font-size: 1rem;
  pointer-events: none;
  background-color: transparent;
  transition: all 0.2s ease;
  padding: 0 0.25rem;
}

/* Label shrinks when input is filled or focused */
.floating-label input:focus + label,
.floating-label input:not(:placeholder-shown) + label,
.floating-label input:valid + label {
  top: -0.2rem;
  left: 0.2rem;
  font-size: 0.6rem;
  color: var(--light-grey, #ccc);
  padding: 0.3rem 0.4rem;
  z-index: 2;
}

.username-note {
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  padding: 8px 12px;
  background: rgba(255, 203, 59, 0.12);
  border: 1px solid var(--yellow);
  color: var(--yellow);
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  text-align: center;
  max-width: 500px;
}

.username-caution-bar {
  margin-top: 0.25rem;
  margin-bottom: 0.5rem;
  padding: 4px 8px;
  background: rgba(255, 203, 59, 0.08);
  border: 1px solid rgba(255, 203, 59, 0.3);
  color: rgba(255, 203, 59, 0.8);
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  text-align: center;
  max-width: 500px;
  opacity: 0.8;
}

/* New Design Styles */

/* Profile Overview Card */
.profile-overview-card {
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-info {
  flex: 1;
}

.profile-info .username {
  margin: 0 0 4px 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--white);
}

.profile-info .email {
  margin: 0 0 2px 0;
  font-size: 0.9rem;
  color: var(--light-grey);
  opacity: 0.8;
}

.profile-info .created-date {
  margin: 0;
  font-size: 0.8rem;
  color: var(--light-grey);
  opacity: 0.6;
}

/* Account Security Card */
.account-security-card {
  margin-bottom: 24px;
}

.card-header {
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.card-title {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--white);
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title.danger {
  color: #ff7f7f;
}

.card-icon {
  color: var(--yellow);
}

.card-title.danger .card-icon {
  color: #ff7f7f;
}

/* Settings Sections */
.settings-section {
  margin-bottom: 32px;
}

.settings-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-info {
  flex: 1;
}

.section-title {
  margin: 0 0 4px 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--white);
}

.section-description {
  margin: 0;
  font-size: 0.85rem;
  color: var(--light-grey);
  opacity: 0.8;
}

/* Action Buttons */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.action-btn.primary {
  background: rgba(255, 203, 59, 0.15);
  border: 1px solid var(--yellow);
  color: var(--yellow);
}

.action-btn.primary:hover {
  background: rgba(255, 203, 59, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 203, 59, 0.2);
}

.action-btn.success {
  background: rgba(76, 175, 80, 0.15);
  border: 1px solid #4caf50;
  color: #4caf50;
}

.action-btn.success:hover:not(:disabled) {
  background: rgba(76, 175, 80, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.2);
}

.action-btn.success:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.action-btn.secondary {
  background: rgba(158, 158, 158, 0.15);
  border: 1px solid #9e9e9e;
  color: #9e9e9e;
}

.action-btn.secondary:hover {
  background: rgba(158, 158, 158, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(158, 158, 158, 0.2);
}

.action-btn.danger {
  background: rgba(255, 127, 127, 0.15);
  border: 1px solid #ff7f7f;
  color: #ff7f7f;
}

.action-btn.danger:hover {
  background: rgba(255, 127, 127, 0.25);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 127, 127, 0.2);
}

/* Edit Section */
.edit-section {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 20px;
  margin-top: 16px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  width: 100%;
}

.floating-label {
  position: relative;
  width: 100%;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  color: var(--white);
  font-size: 0.9rem;
  outline: none;
  transition: all 0.2s ease;
}

.form-input:focus {
  border-color: var(--yellow);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 0 0 3px rgba(255, 203, 59, 0.1);
}

.floating-label label {
  position: absolute;
  top: 12px;
  left: 16px;
  color: var(--light-grey);
  font-size: 0.9rem;
  pointer-events: none;
  transition: all 0.2s ease;
  background: transparent;
}

.floating-label input:focus + label,
.floating-label input:not(:placeholder-shown) + label {
  top: -8px;
  left: 12px;
  font-size: 0.75rem;
  color: var(--yellow);
  background: var(--dark-grey);
  padding: 0 4px;
}

/* Password Field */
.password-field {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--light-grey);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: var(--yellow);
}

/* Validation List */
.validation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.validation-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.85rem;
  color: #ccc;
  transition: color 0.2s ease;
}

.validation-item.valid {
  color: #4caf50;
}

.validation-item svg {
  width: 14px;
  height: 14px;
}

/* Username Warning */
.username-warning {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px;
  background: rgba(255, 203, 59, 0.08);
  border: 1px solid rgba(255, 203, 59, 0.3);
  border-radius: 8px;
  color: rgba(255, 203, 59, 0.9);
  font-size: 0.8rem;
  line-height: 1.4;
}

.username-warning svg {
  width: 14px;
  height: 14px;
  margin-top: 1px;
  flex-shrink: 0;
}

/* Edit Actions */
.edit-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* Danger Zone */
.danger-zone-card {
  border-color: rgba(255, 127, 127, 0.2);
  margin-bottom: 0; /* Last card, no bottom margin needed */
}

.danger-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.danger-info h4 {
  margin: 0 0 4px 0;
  font-size: 1rem;
  font-weight: 600;
  color: #ff7f7f;
}

.danger-info p {
  margin: 0;
  font-size: 0.85rem;
  color: var(--light-grey);
  opacity: 0.8;
  line-height: 1.4;
}

/* Slide Down Transition */
.slide-down-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
  overflow: hidden;
}

.slide-down-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.slide-down-enter-from {
  opacity: 0;
  max-height: 0;
  transform: translateY(-8px);
  margin-top: 0;
}

.slide-down-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-5px);
  margin-top: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.slide-down-enter-to {
  opacity: 1;
  max-height: 600px;
  transform: translateY(0);
  margin-top: 16px;
}

.slide-down-leave-from {
  opacity: 1;
  max-height: 600px;
  transform: translateY(0);
  margin-top: 16px;
}

/* Responsive Design */
@media (max-width: 768px) {
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .danger-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .edit-actions {
    flex-direction: column;
  }

  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
