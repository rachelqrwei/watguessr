<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">×</button>

      <div class="modal-header">
        <h2>REPORT A BUG</h2>
        <p>Help us improve WatGuessr by reporting any issues you encounter</p>
      </div>

      <form @submit.prevent="submitReport" class="report-form">
        <div class="form-group floating-label">
          <input
            type="text"
            id="title"
            v-model="bugReport.title"
            placeholder=""
            required
            maxlength="100"
          />
          <label for="title">BUG TITLE</label>
        </div>

        <div class="form-group floating-label">
          <select id="category" v-model="bugReport.category" required>
            <option value="">Select category</option>
            <option value="gameplay">Gameplay Issue</option>
            <option value="ui">UI/UX Problem</option>
            <option value="performance">Performance Issue</option>
            <option value="authentication">Login/Account Issue</option>
            <option value="other">Other</option>
          </select>
          <label for="category">CATEGORY</label>
        </div>

        <div class="form-group floating-label">
          <textarea
            id="description"
            v-model="bugReport.description"
            placeholder=""
            required
            rows="4"
            maxlength="500"
          ></textarea>
          <label for="description">DESCRIPTION</label>
        </div>

        <div class="form-group floating-label">
          <input
            type="text"
            id="steps"
            v-model="bugReport.steps"
            placeholder=""
            required
            maxlength="200"
          />
          <label for="steps">STEPS TO REPRODUCE</label>
        </div>

        <div class="form-group floating-label">
          <input
            type="text"
            id="browser"
            v-model="bugReport.browser"
            placeholder=""
            maxlength="100"
          />
          <label for="browser">BROWSER (Optional)</label>
        </div>

        <div class="form-group floating-label">
          <input
            type="text"
            id="device"
            v-model="bugReport.device"
            placeholder=""
            maxlength="100"
          />
          <label for="device">DEVICE (Optional)</label>
        </div>

        <div class="form-group">
          <label class="checkbox-label">
            <input
              type="checkbox"
              v-model="bugReport.includeUserInfo"
            />
            <span class="checkmark"></span>
            Include my username for follow-up questions
          </label>
        </div>

        <div class="character-count">
          <span>{{ bugReport.description.length }}/500</span>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>
        <p v-if="success" class="success-message">{{ success }}</p>

        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="$emit('close')">
            CANCEL
          </button>
          <button type="submit" class="submit-btn" :disabled="loading">
            <span v-if="loading">SENDING...</span>
            <span v-else>SUBMIT REPORT</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { useStore } from 'vuex'
import type { BugReport } from '@/stores/modules/bugReport/state'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const store = useStore()

const bugReport = ref<BugReport>({
  title: '',
  category: '',
  description: '',
  steps: '',
  browser: '',
  device: '',
  includeUserInfo: false
})

// Vuex state
const loading = computed(() => store.getters['bugReport/getLoading'])
const error = computed(() => store.getters['bugReport/getError'])
const success = computed(() => store.getters['bugReport/getSuccess'])

const resetForm = () => {
  bugReport.value = {
    title: '',
    category: '',
    description: '',
    steps: '',
    browser: '',
    device: '',
    includeUserInfo: false
  }
  store.dispatch('bugReport/resetState')
}

const submitReport = async () => {
  if (loading.value) return

  try {
    const result = await store.dispatch('bugReport/submitBugReport', bugReport.value)

    if (result.success) {
      setTimeout(() => {
        emit('close')
      }, 2000)
    }

  } catch (err) {
    console.error('Failed to submit bug report:', err)
  }
}

// Reset form when modal opens/closes
watch(() => props.visible, (newVal) => {
  if (newVal) {
    resetForm()
  }
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: rgba(42, 42, 44, 0.95);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 32px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: none;
  border: none;
  color: var(--light-grey);
  font-size: 24px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--white);
}

.modal-header {
  text-align: center;
  margin-bottom: 32px;
}

.modal-header h2 {
  font-size: 28px;
  font-weight: 900;
  color: var(--white);
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.modal-header p {
  color: var(--light-grey);
  font-size: 14px;
  line-height: 1.5;
}

.report-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  position: relative;
}

.floating-label {
  position: relative;
}

.floating-label input,
.floating-label select,
.floating-label textarea {
  width: 100%;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: var(--white);
  font-size: 14px;
  font-family: inherit;
  transition: all 0.3s ease;
}

.floating-label input:focus,
.floating-label select:focus,
.floating-label textarea:focus {
  outline: none;
  border-color: var(--yellow);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 3px rgba(255, 203, 59, 0.1);
}

.floating-label label {
  position: absolute;
  left: 20px;
  top: 16px;
  color: var(--light-grey);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  transition: all 0.3s ease;
  pointer-events: none;
  background: rgba(42, 42, 44, 0.95);
  padding: 0 8px;
}

.floating-label input:focus + label,
.floating-label select:focus + label,
.floating-label textarea:focus + label,
.floating-label input:not(:placeholder-shown) + label,
.floating-label select:not([value=""]) + label,
.floating-label textarea:not(:placeholder-shown) + label {
  top: -8px;
  left: 16px;
  font-size: 10px;
  color: var(--yellow);
}

.floating-label textarea {
  resize: vertical;
  min-height: 100px;
}

.floating-label select {
  cursor: pointer;
}

.floating-label select option {
  background: var(--dark-grey);
  color: var(--white);
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  color: var(--light-grey);
  font-size: 14px;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: var(--yellow);
}

.character-count {
  text-align: right;
  font-size: 12px;
  color: var(--light-grey);
  margin-top: -8px;
}

.error-message {
  color: #ff4757;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: rgba(255, 71, 87, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(255, 71, 87, 0.2);
}

.success-message {
  color: #4caf50;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.form-actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.cancel-btn,
.submit-btn {
  flex: 1;
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.3s ease;
  letter-spacing: 0.5px;
}

.cancel-btn {
  background: rgba(255, 255, 255, 0.1);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.submit-btn {
  background: var(--yellow);
  color: var(--dark-grey);
  border: 1px solid var(--yellow);
}

.submit-btn:hover:not(:disabled) {
  background: #e6b800;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(255, 203, 59, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .modal-content {
    padding: 24px;
    margin: 16px;
    width: calc(100% - 32px);
  }

  .form-actions {
    flex-direction: column;
  }

  .modal-header h2 {
    font-size: 24px;
  }
}
</style>
