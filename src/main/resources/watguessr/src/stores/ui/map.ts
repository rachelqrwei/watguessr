import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface MapState {
  center: [number, number]
  zoom: number
  bearing: number
  pitch: number
  style: string
  showBuildings: boolean
  showLabels: boolean
  show3D: boolean
  isFullscreen: boolean
}

export interface MapInteraction {
  isDragging: boolean
  isZooming: boolean
  isRotating: boolean
  lastClick: [number, number] | null
}

export const useMapStore = defineStore('map', () => {
  // State
  const mapState = ref<MapState>({
    center: [0, 0],
    zoom: 10,
    bearing: 0,
    pitch: 0,
    style: 'mapbox://styles/mapbox/streets-v12',
    showBuildings: true,
    showLabels: true,
    show3D: false,
    isFullscreen: false
  })

  const interaction = ref<MapInteraction>({
    isDragging: false,
    isZooming: false,
    isRotating: false,
    lastClick: null
  })

  const mapInstance = ref<any>(null)

  // Getters
  const currentCenter = computed(() => mapState.value.center)
  const currentZoom = computed(() => mapState.value.zoom)
  const isInteracting = computed(() => 
    interaction.value.isDragging || 
    interaction.value.isZooming || 
    interaction.value.isRotating
  )

  const mapStyle = computed(() => mapState.value.style)
  const mapConfig = computed(() => ({
    center: mapState.value.center,
    zoom: mapState.value.zoom,
    bearing: mapState.value.bearing,
    pitch: mapState.value.pitch,
    style: mapState.value.style
  }))

  // Actions
  const setMapInstance = (map: any) => {
    mapInstance.value = map
  }

  const setCenter = (center: [number, number]) => {
    mapState.value.center = center
    if (mapInstance.value) {
      mapInstance.value.setCenter(center)
    }
  }

  const setZoom = (zoom: number) => {
    mapState.value.zoom = Math.max(0, Math.min(22, zoom))
    if (mapInstance.value) {
      mapInstance.value.setZoom(mapState.value.zoom)
    }
  }

  const setBearing = (bearing: number) => {
    mapState.value.bearing = bearing
    if (mapInstance.value) {
      mapInstance.value.setBearing(bearing)
    }
  }

  const setPitch = (pitch: number) => {
    mapState.value.pitch = Math.max(0, Math.min(85, pitch))
    if (mapInstance.value) {
      mapInstance.value.setPitch(mapState.value.pitch)
    }
  }

  const setMapStyle = (style: string) => {
    mapState.value.style = style
    if (mapInstance.value) {
      mapInstance.value.setStyle(style)
    }
  }

  const flyTo = (options: {
    center?: [number, number]
    zoom?: number
    bearing?: number
    pitch?: number
    duration?: number
  }) => {
    if (mapInstance.value) {
      mapInstance.value.flyTo({
        center: options.center || mapState.value.center,
        zoom: options.zoom || mapState.value.zoom,
        bearing: options.bearing || mapState.value.bearing,
        pitch: options.pitch || mapState.value.pitch,
        duration: options.duration || 1000
      })
    }
  }

  const toggleBuildings = () => {
    mapState.value.showBuildings = !mapState.value.showBuildings
    if (mapInstance.value) {
      const layers = mapInstance.value.getStyle().layers
      layers.forEach((layer: any) => {
        if (layer.id.includes('building')) {
          mapInstance.value.setLayoutProperty(
            layer.id,
            'visibility',
            mapState.value.showBuildings ? 'visible' : 'none'
          )
        }
      })
    }
  }

  const toggleLabels = () => {
    mapState.value.showLabels = !mapState.value.showLabels
    if (mapInstance.value) {
      const layers = mapInstance.value.getStyle().layers
      layers.forEach((layer: any) => {
        if (layer.id.includes('label') || layer.id.includes('text')) {
          mapInstance.value.setLayoutProperty(
            layer.id,
            'visibility',
            mapState.value.showLabels ? 'visible' : 'none'
          )
        }
      })
    }
  }

  const toggle3D = () => {
    mapState.value.show3D = !mapState.value.show3D
    if (mapState.value.show3D) {
      setPitch(45)
    } else {
      setPitch(0)
    }
  }

  const toggleFullscreen = () => {
    mapState.value.isFullscreen = !mapState.value.isFullscreen
  }

  const resetMap = () => {
    mapState.value = {
      center: [0, 0],
      zoom: 10,
      bearing: 0,
      pitch: 0,
      style: 'mapbox://styles/mapbox/streets-v12',
      showBuildings: true,
      showLabels: true,
      show3D: false,
      isFullscreen: false
    }
    
    if (mapInstance.value) {
      mapInstance.value.setCenter(mapState.value.center)
      mapInstance.value.setZoom(mapState.value.zoom)
      mapInstance.value.setBearing(mapState.value.bearing)
      mapInstance.value.setPitch(mapState.value.pitch)
      mapInstance.value.setStyle(mapState.value.style)
    }
  }

  // Interaction handlers
  const setDragging = (isDragging: boolean) => {
    interaction.value.isDragging = isDragging
  }

  const setZooming = (isZooming: boolean) => {
    interaction.value.isZooming = isZooming
  }

  const setRotating = (isRotating: boolean) => {
    interaction.value.isRotating = isRotating
  }

  const setLastClick = (coordinates: [number, number]) => {
    interaction.value.lastClick = coordinates
  }

  const clearLastClick = () => {
    interaction.value.lastClick = null
  }

  return {
    // State
    mapState,
    interaction,
    mapInstance,
    // Getters
    currentCenter,
    currentZoom,
    isInteracting,
    mapStyle,
    mapConfig,
    // Actions
    setMapInstance,
    setCenter,
    setZoom,
    setBearing,
    setPitch,
    setMapStyle,
    flyTo,
    toggleBuildings,
    toggleLabels,
    toggle3D,
    toggleFullscreen,
    resetMap,
    setDragging,
    setZooming,
    setRotating,
    setLastClick,
    clearLastClick
  }
}) 