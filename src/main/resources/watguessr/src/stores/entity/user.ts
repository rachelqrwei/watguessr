import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface User {
  id: string
  username: string
  emailAddress: string
  elo: number
  streak: number
  createdAt: string
  lastLoginAt: string
}

export const useUserStore = defineStore('user', () => {
  // State
  const users = ref<User[]>([])
  const currentUser = ref<User | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getUserById = computed(() => {
    return (id: string) => users.value.find(user => user.id === id)
  })

  const isAuthenticated = computed(() => currentUser.value !== null)

  const topUsers = computed(() => {
    return [...users.value].sort((a, b) => b.elo - a.elo).slice(0, 10)
  })

  const userName = computed(() =>
    currentUser.value?.username || 'Guest'
  );

  // Actions
  const fetchUsers = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/user`)
      if (!response.ok) throw new Error('Failed to fetch users')
      users.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchUserById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/user/${id}`)
      if (!response.ok) throw new Error('Failed to fetch user')
      const user = await response.json()
      const existingIndex = users.value.findIndex(u => u.id === id)
      if (existingIndex >= 0) {
        users.value[existingIndex] = user
      } else {
        users.value.push(user)
      }
      return user
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const signUpUser = async (email: string, username: string, password: string) => {
    loading.value = true
    error.value = null
    try {
      console.log(import.meta.env.VITE_BASE_API_URL);
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, username, password })
      })
      // if (!response.ok) throw new Error('Login failed')
      if (!response.ok) {
        const errorData = await response.json();
        const message = errorData.message || errorData.error || 'Signup failed'
        // throw new Error(errorData.error || 'Signup failed');
        throw new Error(message)
      }

      return response.text()
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Signup failed'
      error.value = message
      throw new Error(message)
    } finally {
      loading.value = false
    }
  }

  const login = async (username: string, password: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/login`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      })

      if (!response.ok) {
        const errorData = await response.json();
        const message = errorData.message || errorData.error || 'Signup failed'
        throw new Error(message)
      }
      const user = await response.json()
      currentUser.value = user // logging in user.
      return user
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Login failed'
      throw new Error(error.value)
    } finally {
      loading.value = false
    }
  }


  const logout = () => {
    currentUser.value = null
  }

  const updateUser = async (id: string, updates: Partial<User>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/user/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update user')
      const updatedUser = await response.json()
      const index = users.value.findIndex(u => u.id === id)
      if (index >= 0) {
        users.value[index] = updatedUser
      }
      if (currentUser.value?.id === id) {
        currentUser.value = updatedUser
      }
      return updatedUser
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const sendOtp = async (to: string) => {
    loading.value = true
    error.value = null
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/user/send-otp?to=${encodeURIComponent(to)}`
      console.log(url);

      const response = await fetch(url, {
        method: 'POST'
      })
      if (!response.ok) throw new Error('Failed to send otp to user')
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const verifyOtp = async (email: string, submittedOtp: string) => {
    loading.value = true
    error.value = null
    try {
      const url = `${import.meta.env.VITE_API_BASE_URL}/api/user/verify-otp?email=${encodeURIComponent(email)}&submittedOtp=${encodeURIComponent(submittedOtp)}`

      const response = await fetch(url, {
        method: 'POST'
      })
      if (!response.ok) throw new Error('Failed to verify user')
      return "verified";
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    users,
    currentUser,
    loading,
    error,
    // Getters
    getUserById,
    isAuthenticated,
    topUsers,
    userName,
    // Actions
    fetchUsers,
    fetchUserById,
    signUpUser,
    login,
    logout,
    updateUser,
    sendOtp,
    verifyOtp
  }
})
