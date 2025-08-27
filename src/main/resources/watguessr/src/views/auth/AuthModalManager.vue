<template>
  <LoginModal
    :visible="showLogin"
    @close="$emit('closeLogin')"
    @openSignUp="$emit('closeLogin'); $emit('openSignUp')" />

  <SignUpModal
    :visible="showSignUp"
    @close="$emit('closeSignUp')"
    @openLogin="$emit('closeSignUp'); $emit('openLogin')"
    @openWelcome="$emit('closeSignUp'); $emit('openWelcome')" />
</template>

<script>
import LoginModal from './LoginModal.vue'
import SignUpModal from './SignUpModal.vue'

export default {
  components: { LoginModal, SignUpModal },
  props: ["showLogin", "showSignUp"],
  emits: ['closeLogin', 'closeSignUp', 'openLogin', 'openSignUp', 'openWelcome']
};
</script>

<style scoped>

</style>
