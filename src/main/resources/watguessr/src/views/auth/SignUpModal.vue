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

        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" v-model="password" placeholder="Guack123"/>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input type="password" id="confirmPassword" v-model="confirmPassword" placeholder="Guack123"/>
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
    async submitSignUp() {
      if (this.password !== this.confirmPassword) {
        alert("Passwords do not match");
        return;
      }
      console.log(`Email: ${this.email}, Username: ${this.username}, Password: ${this.password}`);
      this.$emit('close');
      const payload = {
        email: this.email,
        username: this.username,
        password: thid.password,
        confirmPassword: this.confirmPassword
      };
      await this.$emit('submit', payload);
    },
  },
};
</script>

<style scoped>
</style>
