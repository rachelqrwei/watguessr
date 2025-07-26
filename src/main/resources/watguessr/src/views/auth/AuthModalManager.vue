<template>
  <LoginModal
    :visible="showLogin"
    @close="$emit('closeLogin')"
    @openSignUp="$emit('closeLogin'); $emit('openSignUp')" />

  <SignUpModal
    :visible="showSignUp"
    @close="$emit('closeSignUp')"
    @openLogin="$emit('closeSignUp'); $emit('openLogin')" />
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
  props: ["showLogin", "showSignUp"],
  emits: ['closeLogin', 'closeSignUp', 'openLogin', 'openSignUp'],
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
