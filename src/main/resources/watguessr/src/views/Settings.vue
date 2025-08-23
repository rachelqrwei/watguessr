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
                  <div class="form-row">
                    <input type="text" :value="settings?.username" disabled />
                  </div>
                  <button class="change-password-btn" @click="toggleUsernameEditing">Change Username</button>
                  <button class="change-password-btn" @click="togglePasswordEditing">Change Password</button>
                </div>
              </div>
            </div>
          </div>

            <!-- Username change section -->
            <div v-if="isEditingUsername" class="form-row">
              <div class="password-input-group">
                <div class="form-group floating-label password-wrapper">
                  <input
                    type="text"
                    id="newUsername"
                    v-model="newUsername"
                    placeholder=" "
                    required
                  />
                  <label for="newUsername">NEW USERNAME</label>

                  <button class="edit-inline-btn" @click="saveUsername" :disabled="isLoading || !usernameChecks.allValid">
                    <font-awesome-icon :icon="'fa-solid fa-check'" />
                    <span class="icon-btn-text">SAVE</span>
                  </button>
                </div>

                <button class="cancel-password-btn" @click="cancelUsernameEditing">Cancel</button>

                <ul class="password-checklist">
                  <li :class="{ valid: usernameChecks.validLengths }">
                    {{ usernameChecks.validLengths ? "✓" : "✗" }} Between 3 to 24 in lengths
                  </li>
                  <li :class="{ valid: usernameChecks.validCombination }">
                    {{ usernameChecks.validCombination ? "✓" : "✗" }} Does not include space
                  </li>
                </ul>
              </div>
              <div class="username-caution-bar">
                Username can be changed only once in 7 days.
                {{ this.settings?.usernameChangedAt ? " Last changed: " + formatDate(this.settings.usernameChangedAt) : " This is your first time changing it" }}
              </div>
            </div>

            <!-- Password change section -->
            <div v-if="isEditingPassword" class="form-row">
              <div class="password-input-group">
                <div class="form-group floating-label password-wrapper">
                  <input
                    :type="showPassword ? 'text' : 'password'"
                    id="newPassword"
                    v-model="newPassword"
                    placeholder=" "
                    required
                  />
                  <label for="newPassword">NEW PASSWORD</label>

                  <span class="toggle-eye" @click="showPassword = !showPassword">
                    <font-awesome-icon :icon="showPassword ? 'fa-solid fa-eye-slash' : 'fa-solid fa-eye'" />
                  </span>

                  <button class="edit-inline-btn" @click="onChangePassword" :disabled="isLoading || !passwordChecks.allValid">
                    <font-awesome-icon :icon="'fa-solid fa-check'" />
                    <span class="icon-btn-text">{{ 'SAVE' }}</span>
                  </button>

                </div>


                <button class="cancel-password-btn" @click="cancelPasswordEditing">Cancel</button>

                <ul class="password-checklist">
                  <li :class="{ valid: passwordChecks.lengthValid }">
                    {{ passwordChecks.lengthValid ? "✓" : "✗" }} At least 8 characters
                  </li>
                  <li :class="{ valid: passwordChecks.casingValid }">
                    {{ passwordChecks.casingValid ? "✓" : "✗" }} Includes uppercase and lowercase
                  </li>
                  <li :class="{ valid: passwordChecks.specialCharValid }">
                    {{ passwordChecks.specialCharValid ? "✓" : "✗" }} Includes special character (!@#$%^&*)
                  </li>
                </ul>
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
      showPassword: false,
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
    },
    usernameChecks() {
      const validLengths = this.newUsername.length >= 3 && this.newUsername.length <= 24;
      const validCombination = !(this.newUsername.includes(" "));
      return {
        validLengths,
        validCombination,
        allValid: validLengths && validCombination
      };
    },
    passwordChecks() {
      const lengthValid = this.newPassword.length >= 8;
      const casingValid = /(?=.*[a-z])(?=.*[A-Z])/.test(this.newPassword);
      const specialCharValid = /(?=.*[!@#$%^&*])/.test(this.newPassword);
      return {
        lengthValid,
        casingValid,
        specialCharValid,
        allValid: lengthValid && casingValid && specialCharValid,
      };
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

          // Show success message
          this.$toast?.success?.('Username updated successfully') || console.log('Username updated successfully')

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

    togglePasswordEditing() {
      this.isEditingPassword = true
      this.newPassword = ''
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
  width: 200px
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
  min-width: 250px;  /* Add minimum width */
  font-size: 14px;   /* Increase font size */
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
  transition: transform 180ms ease, box-shadow 180ms ease, background 180ms ease, border-color 180ms ease, color 180ms ease;
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

@media (max-width: 768px) {
  .profile-content {
    grid-template-columns: 1fr;
    gap: 10px;
    padding: 18px 18px 18px 18px;
  }

  .hero { grid-template-columns: 1fr; gap: 0; }

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
  color: #B6FF7F;
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
  transition: border-color 0.2s ease, background-color 0.2s ease;
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

</style>


