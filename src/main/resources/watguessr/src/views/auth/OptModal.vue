<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>
      <h2>Enter Verification Code</h2>
      <input
        type="text"
        v-model="otp"
        maxlength="6"
        placeholder="Enter 6-digit code"
      />
      <button class="verify-btn" @click="submitOtp">Verify</button>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useUserStore } from '@/stores/entity/user.ts';

const props = defineProps({
  visible: Boolean,
  email: String
})

const emit = defineEmits(['close', 'verified'])

const otp = ref('')
const error = ref(null)

const submitOtp = async () => {
  error.value = null
  try {
    const res = await fetch('/api/email/verify-otp', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ to: props.email, code: otp.value })
    })
    const text = await res.text()
    if (text.includes('verified')) {
      emit('verified')
      emit('close')
    } else {
      error.value = text
    }
  } catch (err) {
    error.value = 'Verification failed.'
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  text-align: center;
}

.verify-btn {
  margin-top: 1rem;
  padding: 0.5rem 1rem;
}

.error {
  color: red;
  margin-top: 0.5rem;
}
</style>
