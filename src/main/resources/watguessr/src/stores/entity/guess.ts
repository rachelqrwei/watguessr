import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface Guess {
  id: string
  userId: string
  time: number
  guessX: number
  guessY: number
  buildingId?: string
  floor?: number
  roundId?: string
  points?: number
}

export const useGuessStore = defineStore('guess', () => {
  // State
  const guesses = ref<Guess[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getGuessById = computed(() => {
    return (id: string) => guesses.value.find(guess => guess.id === id)
  })

  const getGuessesByUser = computed(() => {
    return (userId: string) => guesses.value.filter(guess => guess.userId === userId)
  })

  const getGuessesByRound = computed(() => {
    return (roundId: string) => guesses.value.filter(guess => guess.roundId === roundId)
  })

  const getGuessesByBuilding = computed(() => {
    return (buildingId: string) => guesses.value.filter(guess => guess.buildingId === buildingId)
  })

  const userTotalPoints = computed(() => {
    return (userId: string) => {
      return guesses.value
        .filter(guess => guess.userId === userId)
        .reduce((total, guess) => total + (guess.points || 0), 0)
    }
  })

  // Actions
  const fetchGuesses = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/guesses')
      if (!response.ok) throw new Error('Failed to fetch guesses')
      guesses.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchGuessById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/guesses/${id}`)
      if (!response.ok) throw new Error('Failed to fetch guess')
      const guess = await response.json()
      const existingIndex = guesses.value.findIndex(g => g.id === id)
      if (existingIndex >= 0) {
        guesses.value[existingIndex] = guess
      } else {
        guesses.value.push(guess)
      }
      return guess
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const fetchGuessesByUser = async (userId: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/users/${userId}/guesses`)
      if (!response.ok) throw new Error('Failed to fetch user guesses')
      const userGuesses = await response.json()
      // Update existing guesses or add new ones
      userGuesses.forEach((guess: Guess) => {
        const existingIndex = guesses.value.findIndex(g => g.id === guess.id)
        if (existingIndex >= 0) {
          guesses.value[existingIndex] = guess
        } else {
          guesses.value.push(guess)
        }
      })
      return userGuesses
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return []
    } finally {
      loading.value = false
    }
  }

  const fetchGuessesByRound = async (roundId: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/rounds/${roundId}/guesses`)
      if (!response.ok) throw new Error('Failed to fetch round guesses')
      const roundGuesses = await response.json()
      // Update existing guesses or add new ones
      roundGuesses.forEach((guess: Guess) => {
        const existingIndex = guesses.value.findIndex(g => g.id === guess.id)
        if (existingIndex >= 0) {
          guesses.value[existingIndex] = guess
        } else {
          guesses.value.push(guess)
        }
      })
      return roundGuesses
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return []
    } finally {
      loading.value = false
    }
  }

  const createGuess = async (guessData: Omit<Guess, 'id'>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/guesses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(guessData)
      })
      if (!response.ok) throw new Error('Failed to create guess')
      const newGuess = await response.json()
      guesses.value.push(newGuess)
      return newGuess
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const updateGuess = async (id: string, updates: Partial<Guess>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/guesses/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update guess')
      const updatedGuess = await response.json()
      const index = guesses.value.findIndex(g => g.id === id)
      if (index >= 0) {
        guesses.value[index] = updatedGuess
      }
      return updatedGuess
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const deleteGuess = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/guesses/${id}`, {
        method: 'DELETE'
      })
      if (!response.ok) throw new Error('Failed to delete guess')
      guesses.value = guesses.value.filter(g => g.id !== id)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    guesses,
    loading,
    error,
    // Getters
    getGuessById,
    getGuessesByUser,
    getGuessesByRound,
    getGuessesByBuilding,
    userTotalPoints,
    // Actions
    fetchGuesses,
    fetchGuessById,
    fetchGuessesByUser,
    fetchGuessesByRound,
    createGuess,
    updateGuess,
    deleteGuess
  }
}) 