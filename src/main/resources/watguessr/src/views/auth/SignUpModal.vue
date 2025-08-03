<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>
      <form @submit.prevent="submitSignUp" class="login-form">
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" id="email" v-model="email" placeholder="gguack123@uwaterloo.ca"/>
        </div>

        <div class="form-group">
          <label for="username">Username</label>
          <input type="text" id="username" v-model="username" placeholder="Geese"/>
        </div>
        <p v-if="username.length < 8" class="input-error">Username has to be more than 8 characters</p>

        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" placeholder="Guack123"/>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input type="password" id="confirmPassword" v-model="confirmPassword" placeholder="Guack123"/>
        </div>

        <ul class="error-message" v-if="error.includes('Not a valid password')">
          <li>Password must be at least 8 characters long</li>
          <li>At least one uppercase letter</li>
          <li>At least one lowercase letter</li>
          <li>At least one special character (!@#$%^&*)</li>
        </ul>
        <p v-else class="error-message">{{ error }}</p>

        <p v-if="showSuccess" class="success-message">{{ successMessage }}</p>

        <button :disabled="loading" class="login-btn">
          <span v-if="loading">Signing up...</span>
          <span v-else>Sign Up</span>
        </button>

        <div class="sign-up">
          <label>Already a Watguessr?
            <span class="link" @click="$emit('openLogin')">Login</span>
          </label>
        </div>

        <StatusModal
          v-if="showStatus"
          :message="statusMessage"
          :type="statusType"
          @close="showStatus = false"
        />

      </form>
    </div>
  </div>
</template>

<script>
import { useUserStore } from '@/stores/entity/user.ts';
import StatusModal from "@/views/auth/StatusModal.vue";

export default {
  props: ['visible'],
  data() {
    return {
      email: '',
      username: '',
      password: '',
      confirmPassword: '',
      error: '',
      loading: false,
      successMessage: '',
      showSuccess: false,
      statusMessage: '',
      showStatus: false,
      statusType: 'success', // or 'error'
      userStore: useUserStore()
    };
  },
  methods: {
    async submitSignUp() {
      this.error = '';
      if (this.password !== this.confirmPassword) {
        this.error = "Passwords do not match";
        return;
      }
      const { email, username, password } = this;
      this.showSuccess = false;

      try {
        const result = await this.userStore.signUpUser(email, username, password);

        this.statusMessage = "Login successful!";
        this.statusType = "success";
        this.showStatus = true;

        this.successMessage = result;
        this.showSuccess = true;

        this.$emit('openLogin');
        this.$emit('close');

      } catch (err) {
        this.error = err instanceof Error ? err.message : 'Signup failed';
      }
    },
  },
  watch: {
    visible(val) {
      if (val) {
        this.error = '';
      }
    }
  },
};
</script>

<style scoped>
.error-message {
  color: #ff6b6b;
  font-size: 0.85rem;
  margin-top: -0.5rem;
  margin-bottom: 1rem;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background-color: #2b2b2b;
  padding: 1.5rem;
  border-radius: 10px;
  width: 400px;
  color: #fff;
  font-family: 'Segoe UI', sans-serif;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.4);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 0.4rem;
  right: 0.7rem;
  font-size: 1.2rem;
  border: none;
  background: transparent;
  color: #ccc;
  cursor: pointer;
}

.login-form .form-group {
  display: flex;
  flex-direction: column;
  margin-top: 1rem;
  margin-bottom: 0;
}

.login-form label {
  font-size: 0.85rem;
  margin-bottom: 0.3rem;
  color: #ccc;
}

.login-form input {
  padding: 0.6rem;
  font-size: 1rem;
  background-color: #3a3a3a;
  border: 1px solid #555;
  color: #eee;
  border-radius: 6px;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.login-form input:focus {
  outline: none;
  border-color: #00d8ff;
  background-color: #444;
}

.login-btn {
  padding: 0.6rem 1.2rem;
  background-color: #00d8ff;
  color: black;
  font-weight: bold;
  font-size: 0.95rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.login-btn:hover {
  background-color: #00c4e4;
}

.sign-up .link {
  color: var(--yellow);
  text-decoration: none;
  font-weight: 700;
  cursor: pointer;
}

.sign-up .link:hover {
  text-decoration: underline;
}

.error-message {
  color: #ff6b6b;
  font-size: 0.85rem;
  margin-top: 0.25rem;
  margin-bottom: 1rem;
  line-height: 1.4;
  white-space: pre-line;
}

.success-message {
  background-color: #ffe066; /* soft yellow */
  color: #333;
  font-size: 0.85rem;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  text-align: center;
  margin-top: 0.5rem;
  animation: fadeOut 1s ease-in 1s forwards;
}

@keyframes fadeOut {
  to {
    opacity: 0;
  }
}
.input-error {
  color: #e57373;
  font-size: 0.875rem; /* slightly smaller text */
  margin-top: 0;
  display: block;
}
input {
  margin-bottom: 0.1rem; /* or remove this if it's adding too much space */
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transition: opacity 0.3s ease;
}
</style>
