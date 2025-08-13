import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export type ModalType = 'login' | 'signup' | 'settings' | 'gameOver' | 'leaderboard' | 'help'

export interface ModalState {
  type: ModalType
  visible: boolean
  data?: any
}

export const useModalStore = defineStore('modal', () => {
  // State
  const modals = ref<Record<ModalType, ModalState>>({
    login: { type: 'login', visible: false },
    signup: { type: 'signup', visible: false },
    settings: { type: 'settings', visible: false },
    gameOver: { type: 'gameOver', visible: false },
    leaderboard: { type: 'leaderboard', visible: false },
    help: { type: 'help', visible: false }
  })

  const activeModal = ref<ModalType | null>(null)

  // Getters
  const isModalOpen = computed(() => activeModal.value !== null)

  const getModalState = computed(() => {
    return (type: ModalType) => modals.value[type]
  })

  const getActiveModalData = computed(() => {
    if (!activeModal.value) return null
    return modals.value[activeModal.value]
  })

  // Actions
  const openModal = (type: ModalType, data?: any) => {
    modals.value[type] = {
      type,
      visible: true,
      data
    }
    activeModal.value = type
  }

  const closeModal = (type: ModalType) => {
    modals.value[type] = {
      type,
      visible: false,
      data: undefined
    }
    if (activeModal.value === type) {
      activeModal.value = null
    }
  }

  const closeAllModals = () => {
    Object.keys(modals.value).forEach((key) => {
      const modalType = key as ModalType
      modals.value[modalType] = {
        type: modalType,
        visible: false,
        data: undefined
      }
    })
    activeModal.value = null
  }

  const updateModalData = (type: ModalType, data: any) => {
    if (modals.value[type]) {
      modals.value[type].data = data
    }
  }

  return {
    // State
    modals,
    activeModal,
    // Getters
    isModalOpen,
    getModalState,
    getActiveModalData,
    // Actions
    openModal,
    closeModal,
    closeAllModals,
    updateModalData
  }
}) 