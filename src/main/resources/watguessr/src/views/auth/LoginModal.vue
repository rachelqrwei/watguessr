<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>
      <form @submit.prevent="submitLogin" class="login-form">
        <div class="form-group floating-label">
          <input type="text" id="username" v-model="username" placeholder="" required />
          <label for="username">USERNAME</label>
        </div>

        <div class="form-group floating-label">
          <input type="password" id="password" v-model="password" placeholder="" required />
          <label for="password">PASSWORD</label>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>

        <button type="submit" class="login-btn">LOGIN</button>

        <div class="checkbox-wrapper">
          <label>
            <input type="checkbox" v-model="rememberMe" />
            Remember me
          </label>
        </div>

        <div class="sign-up">
          <label>
            Don't have an account?
            <span class="link" @click="$emit('openSignUp')">SIGN UP</span>
          </label>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex';

export default {
  props: ['visible'],

  data() {
    return {
      username: '',
      password: '',
      error: '',
      rememberMe: false,
    };
  },

  methods: {
    ...mapActions('user', ['login']),

    async submitLogin() {
      this.error = '';
      try {
        await this.login({ username: this.username, password: this.password });
        this.$emit('close');
      } catch (err) {
        this.error = err instanceof Error ? err.message : 'Login failed';
      }
    },
  },

  watch: {
    visible(val) {
      if (val) {
        this.error = '';
        this.username = '';
        this.password = '';
        this.rememberMe = false;
      }
    },
  },
};
</script>

<style scoped>
.error-message {
  color: #FF7F7F;
  font-size: 0.85rem;
  margin-top: 0.25rem;
  margin-bottom: 1rem;
  line-height: 1.4;
  white-space: pre-line;
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
  background: rgba(42, 42, 44, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  padding: 3rem 2rem;
  border-radius: 10px;
  width: 300px;
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
  margin-bottom: 1rem;
}

.login-form label {
  font-size: 0.85rem;
  margin-bottom: 0.3rem;
  color: #ccc;
}

.login-form input[type="text"],
.login-form input[type="password"] {
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
  background-color: var(--yellow);
  color: black;
  font-weight: bold !important;
  font-size: 0.95rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  display: block;
  margin: 1rem auto 0;
  margin-top: 1.25rem;
  transition: transform 0.06s ease, filter 0.2s ease, background-color 0.2s ease;
}

.login-btn:hover {
  background-color: #ffd24d;
  transform: translateY(-2px);
}

.login-btn:active {
  transform: translateY(-1px);
}

.checkbox-wrapper {
  margin-top: 1rem;
  color: #ccc;
  font-size: 0.85rem;
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

.floating-label {
  position: relative;
  margin-top: 1.5rem;
}

.floating-label input {
  padding: 1.1rem 0.6rem 0.4rem;
  height: 3rem; /* Ensure consistent height */
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

.floating-label input:focus + label,
.floating-label input:not(:placeholder-shown) + label,
.floating-label input:valid + label {
  top: -0.2rem;
  left: 0.2rem;
  font-size: 0.6rem;
  color: #aaa;
  padding: 0.3rem 0.4rem;
  z-index: 2;
}

.floating-label input:focus {
  border-color: #aaa;
  box-shadow: 0 0 0 1px #aaa;
}

.floating-label input:-webkit-autofill {
  background-color: #3a3a3a !important;
  color: #aaa !important;
}
</style>
