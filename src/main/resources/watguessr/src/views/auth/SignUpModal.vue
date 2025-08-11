<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>
      <form @submit.prevent="submitSignUp" class="login-form">
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" id="email" v-model="email" />
        </div>

        <div class="form-group">
          <label for="username">Username</label>
          <input type="text" id="username" v-model="username" />
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" />
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input type="password" id="confirmPassword" v-model="confirmPassword" />
        </div>

        <button type="submit" class="login-btn">Sign Up</button>

        <div class="sign-up">
          <label>Already a Watguessr?
            <span class="link" @click="$emit('openLogin')">Login</span>
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
      email: '',
      username: '',
      password: '',
      confirmPassword: '',
    };
  },
  methods: {
    submitSignUp() {
      if (this.password !== this.confirmPassword) {
        alert("Passwords do not match");
        return;
      }
      console.log(`Email: ${this.email}, Username: ${this.username}, Password: ${this.password}`);
      this.$emit('close');
    },
  },
};
</script>

<style scoped>
</style>
