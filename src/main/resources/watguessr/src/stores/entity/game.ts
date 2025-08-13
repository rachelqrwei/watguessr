import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface Game {
  id: string
  name: string
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED'
  createdAt: string
  completedAt?: string
}

export const useGameStore = defineStore('game', () => {
  // State
  const games = ref<Game[]>([])
  const currentGame = ref<Game | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getGameById = computed(() => {
    return (id: string) => games.value.find(game => game.id === id)
  })

  const getActiveGames = computed(() => {
    return games.value.filter(game => game.status === 'ACTIVE')
  })

  const getCompletedGames = computed(() => {
    return games.value.filter(game => game.status === 'COMPLETED')
  })

  const getGamesByStatus = computed(() => {
    return (status: Game['status']) => games.value.filter(game => game.status === status)
  })

  // Actions
  const fetchGames = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/games')
      if (!response.ok) throw new Error('Failed to fetch games')
      games.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchGameById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/games/${id}`)
      if (!response.ok) throw new Error('Failed to fetch game')
      const game = await response.json()
      const existingIndex = games.value.findIndex(g => g.id === id)
      if (existingIndex >= 0) {
        games.value[existingIndex] = game
      } else {
        games.value.push(game)
      }
      return game
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const createGame = async (gameData: Omit<Game, 'id' | 'createdAt'>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/games', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(gameData)
      })
      if (!response.ok) throw new Error('Failed to create game')
      const newGame = await response.json()
      games.value.push(newGame)
      currentGame.value = newGame
      return newGame
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const updateGame = async (id: string, updates: Partial<Game>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/games/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update game')
      const updatedGame = await response.json()
      const index = games.value.findIndex(g => g.id === id)
      if (index >= 0) {
        games.value[index] = updatedGame
      }
      if (currentGame.value?.id === id) {
        currentGame.value = updatedGame
      }
      return updatedGame
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const deleteGame = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/games/${id}`, {
        method: 'DELETE'
      })
      if (!response.ok) throw new Error('Failed to delete game')
      games.value = games.value.filter(g => g.id !== id)
      if (currentGame.value?.id === id) {
        currentGame.value = null
      }
      return true
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return false
    } finally {
      loading.value = false
    }
  }

  const setCurrentGame = (game: Game | null) => {
    currentGame.value = game
  }

  const completeGame = async (id: string) => {
    return await updateGame(id, { 
      status: 'COMPLETED', 
      completedAt: new Date().toISOString() 
    })
  }

  return {
    // State
    games,
    currentGame,
    loading,
    error,
    // Getters
    getGameById,
    getActiveGames,
    getCompletedGames,
    getGamesByStatus,
    // Actions
    fetchGames,
    fetchGameById,
    createGame,
    updateGame,
    deleteGame,
    setCurrentGame,
    completeGame
  }
}) 