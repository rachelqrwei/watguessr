import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface Scene {
  id: string
  name: string
  description?: string
  imageUrl?: string
  buildingId?: string
}

export const useSceneStore = defineStore('scene', () => {
  // State
  const scenes = ref<Scene[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getSceneById = computed(() => {
    return (id: string) => scenes.value.find(scene => scene.id === id)
  })

  const getScenesByBuilding = computed(() => {
    return (buildingId: string) => scenes.value.filter(scene => scene.buildingId === buildingId)
  })

  // Actions
  const fetchScenes = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/scenes')
      if (!response.ok) throw new Error('Failed to fetch scenes')
      scenes.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchSceneById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/scenes/${id}`)
      if (!response.ok) throw new Error('Failed to fetch scene')
      const scene = await response.json()
      const existingIndex = scenes.value.findIndex(s => s.id === id)
      if (existingIndex >= 0) {
        scenes.value[existingIndex] = scene
      } else {
        scenes.value.push(scene)
      }
      return scene
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const createScene = async (sceneData: Omit<Scene, 'id'>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/scenes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(sceneData)
      })
      if (!response.ok) throw new Error('Failed to create scene')
      const newScene = await response.json()
      scenes.value.push(newScene)
      return newScene
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const updateScene = async (id: string, updates: Partial<Scene>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/scenes/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update scene')
      const updatedScene = await response.json()
      const index = scenes.value.findIndex(s => s.id === id)
      if (index >= 0) {
        scenes.value[index] = updatedScene
      }
      return updatedScene
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const deleteScene = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/scenes/${id}`, {
        method: 'DELETE'
      })
      if (!response.ok) throw new Error('Failed to delete scene')
      scenes.value = scenes.value.filter(s => s.id !== id)
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
    scenes,
    loading,
    error,
    // Getters
    getSceneById,
    getScenesByBuilding,
    // Actions
    fetchScenes,
    fetchSceneById,
    createScene,
    updateScene,
    deleteScene
  }
}) 