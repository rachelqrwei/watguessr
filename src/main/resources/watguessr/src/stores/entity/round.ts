import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface Round {
  id: string
  sceneId: string
  gameId?: string
}

export const useRoundStore = defineStore('round', () => {
  // State
  const rounds = ref<Round[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getRoundById = computed(() => {
    return (id: string) => rounds.value.find(round => round.id === id)
  })

  const getRoundsByGame = computed(() => {
    return (gameId: string) => rounds.value.filter(round => round.gameId === gameId)
  })

  const getRoundsByScene = computed(() => {
    return (sceneId: string) => rounds.value.filter(round => round.sceneId === sceneId)
  })

  // Actions
  const fetchRounds = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/rounds`)
      if (!response.ok) throw new Error('Failed to fetch rounds')
      rounds.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchRoundById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/rounds/${id}`)
      if (!response.ok) throw new Error('Failed to fetch round')
      const round = await response.json()
      const existingIndex = rounds.value.findIndex(r => r.id === id)
      if (existingIndex >= 0) {
        rounds.value[existingIndex] = round
      } else {
        rounds.value.push(round)
      }
      return round
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const fetchRoundsByGame = async (gameId: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/games/${gameId}/rounds`)
      if (!response.ok) throw new Error('Failed to fetch game rounds')
      const gameRounds = await response.json()
      // Update existing rounds or add new ones
      gameRounds.forEach((round: Round) => {
        const existingIndex = rounds.value.findIndex(r => r.id === round.id)
        if (existingIndex >= 0) {
          rounds.value[existingIndex] = round
        } else {
          rounds.value.push(round)
        }
      })
      return gameRounds
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return []
    } finally {
      loading.value = false
    }
  }

  const createRound = async (roundData: Omit<Round, 'id'>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/rounds`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(roundData)
      })
      if (!response.ok) throw new Error('Failed to create round')
      const newRound = await response.json()
      rounds.value.push(newRound)
      return newRound
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const updateRound = async (id: string, updates: Partial<Round>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/rounds/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update round')
      const updatedRound = await response.json()
      const index = rounds.value.findIndex(r => r.id === id)
      if (index >= 0) {
        rounds.value[index] = updatedRound
      }
      return updatedRound
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const deleteRound = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`${import.meta.env.VITE_API_URL}/api/rounds/${id}`, {
        method: 'DELETE'
      })
      if (!response.ok) throw new Error('Failed to delete round')
      rounds.value = rounds.value.filter(r => r.id !== id)
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
    rounds,
    loading,
    error,
    // Getters
    getRoundById,
    getRoundsByGame,
    getRoundsByScene,
    // Actions
    fetchRounds,
    fetchRoundById,
    fetchRoundsByGame,
    createRound,
    updateRound,
    deleteRound
  }
})
