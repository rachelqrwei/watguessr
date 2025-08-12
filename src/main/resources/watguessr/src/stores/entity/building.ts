import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Types
export interface Building {
  id: string
  name: string
  floors: number
  longitude: number
  latitude: number
}

export const useBuildingStore = defineStore('building', () => {
  // State
  const buildings = ref<Building[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const getBuildingById = computed(() => {
    return (id: string) => buildings.value.find(building => building.id === id)
  })

  const getBuildingByName = computed(() => {
    return (name: string) => buildings.value.find(building => building.name === name)
  })

  const buildingsByLocation = computed(() => {
    return (lat: number, lng: number, radius: number = 0.01) => {
      return buildings.value.filter(building => {
        const distance = Math.sqrt(
          Math.pow(building.latitude - lat, 2) + 
          Math.pow(building.longitude - lng, 2)
        )
        return distance <= radius
      })
    }
  })

  // Actions
  const fetchBuildings = async () => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/buildings')
      if (!response.ok) throw new Error('Failed to fetch buildings')
      buildings.value = await response.json()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  const fetchBuildingById = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/buildings/${id}`)
      if (!response.ok) throw new Error('Failed to fetch building')
      const building = await response.json()
      const existingIndex = buildings.value.findIndex(b => b.id === id)
      if (existingIndex >= 0) {
        buildings.value[existingIndex] = building
      } else {
        buildings.value.push(building)
      }
      return building
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const createBuilding = async (buildingData: Omit<Building, 'id'>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch('/api/buildings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(buildingData)
      })
      if (!response.ok) throw new Error('Failed to create building')
      const newBuilding = await response.json()
      buildings.value.push(newBuilding)
      return newBuilding
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const updateBuilding = async (id: string, updates: Partial<Building>) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/buildings/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
      if (!response.ok) throw new Error('Failed to update building')
      const updatedBuilding = await response.json()
      const index = buildings.value.findIndex(b => b.id === id)
      if (index >= 0) {
        buildings.value[index] = updatedBuilding
      }
      return updatedBuilding
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
      return null
    } finally {
      loading.value = false
    }
  }

  const deleteBuilding = async (id: string) => {
    loading.value = true
    error.value = null
    try {
      const response = await fetch(`/api/buildings/${id}`, {
        method: 'DELETE'
      })
      if (!response.ok) throw new Error('Failed to delete building')
      buildings.value = buildings.value.filter(b => b.id !== id)
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
    buildings,
    loading,
    error,
    // Getters
    getBuildingById,
    getBuildingByName,
    buildingsByLocation,
    // Actions
    fetchBuildings,
    fetchBuildingById,
    createBuilding,
    updateBuilding,
    deleteBuilding
  }
}) 