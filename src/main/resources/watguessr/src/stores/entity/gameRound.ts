import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface GameRound {
  id: string
  gameId: string
  roundId: string
  roundNumber: number
  status: 'PENDING' | 'ACTIVE' | 'COMPLETED'
  startTime?: string
  endTime?: string
}

export const useGameRoundStore = defineStore('gameRound', () => {
  // State
  const gameRounds = ref<GameRound[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getGameRoundById = computed(() => {
    return (id: string) => gameRounds.value.find(gameRound => gameRound.id === id)
  })

  const getGameRoundsByGame = computed(() => {
    return (gameId: string) => gameRounds.value.filter(gameRound => gameRound.gameId === gameId)
  })

  const getGameRoundsByRound = computed(() => {
    return (roundId: string) => gameRounds.value.filter(gameRound => gameRound.roundId === roundId)
  })

  const getActiveGameRounds = computed(() => {
    return gameRounds.value.filter(gameRound => gameRound.status === 'ACTIVE')
  })

  const getGameRoundsByStatus = computed(() => {
    return (status: GameRound['status']) => gameRounds.value.filter(gameRound => gameRound.status === status)
  })

  const getNextRoundNumber = computed(() => {
    return (gameId: string) => {
      const gameRoundsForGame = gameRounds.value.filter(gr => gr.gameId === gameId)
      if (gameRoundsForGame.length === 0) return 1
      return Math.max(...gameRoundsForGame.map(gr => gr.roundNumber)) + 1
    }
  })

  // Actions
  const fetchGameRounds = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/game-rounds')
      if (!response.ok) throw new Error('Failed to fetch game rounds')
      gameRounds.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchGameRoundById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/game-rounds/${id}`)
      if (!response.ok) throw new Error('Failed to fetch game round')
      const gameRound = await response.json()
      const existingIndex = gameRounds.value.findIndex(gr => gr.id === id)
      if (existingIndex >= 0) {
        gameRounds.value[existingIndex] = gameRound
      } else {
        gameRounds.value.push(gameRound)
      }
      return gameRound
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const fetchGameRoundsByGame = async (gameId: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/games/${gameId}/game-rounds`)
      if (!response.ok) throw new Error('Failed to fetch game rounds')
      const gameRoundsForGame = await response.json()
      // Update existing game rounds or add new ones
      gameRoundsForGame.forEach((gameRound: GameRound) => {
        const existingIndex = gameRounds.value.findIndex(gr => gr.id === gameRound.id)
        if (existingIndex >= 0) {
          gameRounds.value[existingIndex] = gameRound
        } else {
          gameRounds.value.push(gameRound)
        }
      })
      return gameRoundsForGame
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return []
    } finally {
      loading.value = false
    }
  }

  const createGameRound = async (gameRoundData: Omit<GameRound, 'id'>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/game-rounds', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(gameRoundData)
      })
      if (!response.ok) throw new Error('Failed to create game round')
      const newGameRound = await response.json()
      gameRounds.value.push(newGameRound)
      return newGameRound
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const updateGameRound = async (id: string, updates: Partial<GameRound>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/game-rounds/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update game round')
      const updatedGameRound = await response.json()
      const index = gameRounds.value.findIndex(gr => gr.id === id)
      if (index >= 0) {
        gameRounds.value[index] = updatedGameRound
      }
      return updatedGameRound
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const deleteGameRound = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/game-rounds/${id}`, {
        method: 'DELETE'
      })
      if (!response.ok) throw new Error('Failed to delete game round')
      gameRounds.value = gameRounds.value.filter(gr => gr.id !== id)
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return false
    } finally {
      loading.value = false
    }
  }

  const startGameRound = async (id: string) => {
    return await updateGameRound(id, { 
      status: 'ACTIVE', 
      startTime: new Date().toISOString() 
    })
  }

  const completeGameRound = async (id: string) => {
    return await updateGameRound(id, { 
      status: 'COMPLETED', 
      endTime: new Date().toISOString() 
    })
  }

  return {
    // State
    gameRounds,
    loading,
    error,
    // Getters
    getGameRoundById,
    getGameRoundsByGame,
    getGameRoundsByRound,
    getActiveGameRounds,
    getGameRoundsByStatus,
    getNextRoundNumber,
    // Actions
    fetchGameRounds,
    fetchGameRoundById,
    fetchGameRoundsByGame,
    createGameRound,
    updateGameRound,
    deleteGameRound,
    startGameRound,
    completeGameRound
  }
}) 