<template>
  <Transition name="modal-fade" appear>
    <div v-if="visible" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Report A Bug</h2>
          <button class="close-button" @click="closeModal">&times;</button>
        </div>

        <div class="modal-body">

          <form @submit.prevent="submitReport">
            <div class="form-group">
              <label for="title">Bug Title</label>
              <input
                id="title"
                v-model="bugReport.title"
                type="text"
                required
                placeholder="Enter bug title"
                maxlength="100"
              />
            </div>

            <div class="form-group">
              <label for="category">Category</label>
              <select id="category" v-model="bugReport.category" required>
                <option value="">Select category</option>
                <option value="gameplay">Gameplay Issue</option>
                <option value="ui">UI/UX Problem</option>
                <option value="performance">Performance Issue</option>
                <option value="authentication">Login/Account Issue</option>
                <option value="other">Other</option>
              </select>
            </div>

            <div class="form-group">
              <label for="description">Description</label>
              <textarea
                id="description"
                v-model="bugReport.description"
                placeholder="Describe the bug in detail"
                required
                rows="4"
                maxlength="500"
              ></textarea>
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="bugReport.includeUserInfo"
                />
                Include my username for follow-up questions
              </label>
            </div>

            <div class="character-count">
              <span>{{ bugReport.description.length }}/500</span>
            </div>

            <p v-if="error" class="error-message">{{ error }}</p>
            <p v-if="success" class="success-message">{{ success }}</p>

            <div class="form-actions">
              <button type="button" class="cancel-button" @click="closeModal">
                Cancel
              </button>
              <button type="submit" class="create-button" :disabled="loading">
                {{ loading ? 'Sending...' : 'Submit Report' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Transition>
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
    includeUserInfo: false
  }
  store.dispatch('bugReport/resetState')
}

const closeModal = () => {
  emit('close')
}

const submitReport = async () => {
  if (loading.value) return

  try {
    const result = await store.dispatch('bugReport/submitBugReport', bugReport.value)

    if (result.success) {
      setTimeout(() => {
        closeModal()
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
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 6000;
}

.modal-content {
  background: rgba(42, 42, 44, 0.7);
  border-radius: 16px;
  padding: 0;
  max-width: 500px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  border: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(42, 42, 44, 0.65);
  backdrop-filter: blur(8px);
}

.modal-header h2 {
  color: var(--white);
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.close-button {
  background: none;
  border: none;
  color: #888;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.close-button:hover {
  background: #333;
  color: white;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  background: rgba(42, 42, 44, 0.8);
  color: white;
  font-size: 14px;
  transition: all 0.2s;
  font-family: "Red Hat Text", sans-serif;
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  font-family: "Red Hat Text", sans-serif;
  font-size: 0.75rem;
  letter-spacing: 0.8px;
  color: rgba(255, 255, 255, 0.4);
  text-transform: uppercase;
}

.form-group select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%23ffffff' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 16px center;
  background-size: 16px;
  padding-right: 48px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-group select:hover {
  border-color: rgba(255, 255, 255, 0.2);
  background-color: rgba(42, 42, 44, 0.9);
}

.form-group select option {
  background: rgba(42, 42, 44, 0.98);
  color: white;
  font-family: "Red Hat Text", sans-serif;
  font-size: 0.75rem;
  font-weight: 400;
  letter-spacing: 0.6px;
  padding: 16px 12px;
  border: none;
  outline: none;
}

.form-group select option:hover {
  background: rgba(127, 185, 255, 0.2);
}

.form-group select option:checked {
  background: rgba(127, 185, 255, 0.3);
  color: #7FB9FF;
}

.form-group select:not([size]) {
  font-family: "Red Hat Text", sans-serif;
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: var(--white);
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #7FB9FF;
  box-shadow: 0 0 0 2px rgba(127, 185, 255, 0.25);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
  font-family: "Red Hat Text", sans-serif;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 0.75rem;
  letter-spacing: 0.6px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.checkbox-label input[type="checkbox"] {
  width: auto;
  margin: 0;
}

.character-count {
  text-align: right;
  font-size: 12px;
  color: var(--light-grey);
  margin-top: -8px;
  font-family: "Red Hat Text", sans-serif;
}

.error-message {
  color: #ff4757;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: rgba(255, 71, 87, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(255, 71, 87, 0.2);
  font-family: "Red Hat Text", sans-serif;
}

.success-message {
  color: #4caf50;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: rgba(76, 175, 80, 0.1);
  border-radius: 8px;
  border: 1px solid rgba(76, 175, 80, 0.2);
  font-family: "Red Hat Text", sans-serif;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.cancel-button,
.create-button {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 6px;
  font-size: 0.81rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  text-transform: uppercase;
  letter-spacing: 1.2px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
  font-family: "Red Hat Text", sans-serif;
}

.cancel-button {
  background: rgba(255, 255, 255, 0.06);
  color: var(--white);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.create-button {
  background: rgba(255, 235, 59, 0.15);
  color: var(--yellow);
  border: 0.5px solid var(--yellow);
  box-shadow: 0 4px 15px rgba(255, 235, 59, 0.1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(8px);
}

.create-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.create-button:hover:not(:disabled) {
  background: rgba(255, 235, 59, 0.2);
  border-color: rgba(255, 235, 59, 0.8);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 235, 59, 0.2);
}

.create-button:hover:not(:disabled)::before {
  left: 100%;
}

.create-button:active {
  transform: translateY(-1px);
}

.create-button:disabled {
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
  border-color: rgba(255, 255, 255, 0.06);
  transform: none;
}

.modal-fade-enter-from .modal-content {
  transform: translateY(-50px) scale(0.8);
  opacity: 0;
}

.modal-fade-leave-to .modal-content {
  transform: translateY(50px) scale(0.8);
  opacity: 0;
}
</style>
