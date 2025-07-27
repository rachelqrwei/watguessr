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
.checkbox-wrapper {
  margin-top: 1rem;
  color: #ccc;
  font-size: 0.85rem;
}
</style>
