import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export type NotificationType = 'success' | 'error' | 'warning' | 'info'

export interface Notification {
  id: string
  type: NotificationType
  title: string
  message: string
  duration?: number
  persistent?: boolean
  timestamp: number
}

export const useNotificationStore = defineStore('notification', () => {
  // State
  const notifications = ref<Notification[]>([])
  const maxNotifications = ref(5)

  // Getters
  const activeNotifications = computed(() => notifications.value)
  const hasNotifications = computed(() => notifications.value.length > 0)
  const successCount = computed(() => notifications.value.filter(n => n.type === 'success').length)
  const errorCount = computed(() => notifications.value.filter(n => n.type === 'error').length)

  // Actions
  const addNotification = (notification: Omit<Notification, 'id' | 'timestamp'>) => {
    const id = Date.now().toString() + Math.random().toString(36).substr(2, 9)
    const newNotification: Notification = {
      ...notification,
      id,
      timestamp: Date.now(),
      duration: notification.duration ?? 5000,
      persistent: notification.persistent ?? false
    }

    notifications.value.unshift(newNotification)

    // Limit the number of notifications
    if (notifications.value.length > maxNotifications.value) {
      notifications.value = notifications.value.slice(0, maxNotifications.value)
    }

    // Auto-remove non-persistent notifications
    if (!newNotification.persistent && newNotification.duration) {
      setTimeout(() => {
        removeNotification(id)
      }, newNotification.duration)
    }

    return id
  }

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  const clearNotifications = () => {
    notifications.value = []
  }

  const clearNotificationsByType = (type: NotificationType) => {
    notifications.value = notifications.value.filter(n => n.type !== type)
  }

  // Convenience methods
  const success = (title: string, message: string, duration?: number) => {
    return addNotification({ type: 'success', title, message, duration })
  }

  const error = (title: string, message: string, duration?: number) => {
    return addNotification({ type: 'error', title, message, duration })
  }

  const warning = (title: string, message: string, duration?: number) => {
    return addNotification({ type: 'warning', title, message, duration })
  }

  const info = (title: string, message: string, duration?: number) => {
    return addNotification({ type: 'info', title, message, duration })
  }

  const setMaxNotifications = (max: number) => {
    maxNotifications.value = max
    if (notifications.value.length > max) {
      notifications.value = notifications.value.slice(0, max)
    }
  }

  return {
    // State
    notifications,
    maxNotifications,
    // Getters
    activeNotifications,
    hasNotifications,
    successCount,
    errorCount,
    // Actions
    addNotification,
    removeNotification,
    clearNotifications,
    clearNotificationsByType,
    success,
    error,
    warning,
    info,
    setMaxNotifications
  }
}) 