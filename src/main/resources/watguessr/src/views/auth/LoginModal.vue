<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>
      <form @submit.prevent="submitLogin" class="login-form">
        <div class="form-group">
          <label for="username">Username</label>
          <input type="text" id="username" v-model="username" placeholder="Geese" />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" placeholder="Guack123"/>
        </div>

        <button type="submit" class="login-btn">Login</button>

        <div class="checkbox-wrapper">
          <label>
            <input type="checkbox" v-model="rememberMe" />
            Remember me
          </label>
        </div>

        <div class="sign-up">
          <label>Don't have an account?
<!--            <span class="link" @click="showSignIn = true">Sign Up</span>-->
            <span class="link" @click="$emit('openSignUp')">Sign Up</span>
          </label>
        </div>

      </form>
    </div>
  </div>
</template>

<script>

export default {
  props: ['visible'],
  data() {
    return {
      username: '',
      password: '',
      rememberMe: false,
    };
  },
  methods: {
    submitLogin() {
      console.log(`Username: ${this.username}, Password: ${this.password}, Remember: ${this.rememberMe}`);
      this.$emit('close');
    },
    openSignUp() {
      this.showSignIn = true;
      this.$emit('close'); // close current LoginModal
    }
  },
};
</script>

<style scoped>
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
</style>
