<template>
  <transition name="fade">
    <div class="status-modal" v-if="visible" :class="typeClass">
      {{ message }}
    </div>
  </transition>
</template>

<script>
export default {
  name: 'StatusModal',
  props: {
    message: String,
    type: {
      type: String,
      default: 'success' // 'success' | 'error'
    }
  },
  data() {
    return {
      visible: false
    };
  },
  mounted() {
    this.visible = true;
    setTimeout(() => {
      this.visible = false;
      this.$emit('close');
    }, 2000); // ⏱️ auto-hide after 2 seconds
  },
  computed: {
    typeClass() {
      return this.type === 'error' ? 'status-error' : 'status-success';
    }
  }
};
</script>

<style scoped>
.status-modal {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  font-weight: bold;
  z-index: 1000;
  font-size: 0.9rem;
  text-align: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

/* Success */
.status-success {
  background-color: #4caf50;
}

/* Error */
.status-error {
  background-color: #f44336;
}

/* Fade animation */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
