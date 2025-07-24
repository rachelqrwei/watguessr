<template>
  <LoginModal :visible="showLogin" @close="showLogin = false" @openSignUp="openSignUp" />
  <SignUpModal :visible="showSignUp" @close="showSignUp = false" @openLogin="openLogin" />
</template>

<script>
import LoginModal from './LoginModal.vue'
import SignUpModal from './SignUpModal.vue'

export default {
  components: { LoginModal, SignUpModal },
  data() {
    return {
      showLogin: false,
      showSignUp: false,
    };
  },
  methods: {
    openLogin() {
      this.showSignUp = false;
      this.showLogin = true;
    },
    openSignUp() {
      this.showLogin = false;
      this.showSignUp = true;
    }
  }
};
</script>

<style scoped>

</style>
