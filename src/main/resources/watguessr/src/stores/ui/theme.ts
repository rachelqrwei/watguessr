import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export type ThemeMode = 'light' | 'dark' | 'auto'

export interface ThemeState {
  mode: ThemeMode
  primaryColor: string
  secondaryColor: string
  accentColor: string
  fontSize: 'small' | 'medium' | 'large'
  reducedMotion: boolean
}

export const useThemeStore = defineStore('theme', () => {
  // State
  const theme = ref<ThemeState>({
    mode: 'dark',
    primaryColor: '#1a1a1a',
    secondaryColor: '#2d2d2d',
    accentColor: '#4f46e5',
    fontSize: 'medium',
    reducedMotion: false
  })

  // Getters
  const isDarkMode = computed(() => {
    if (theme.value.mode === 'auto') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return theme.value.mode === 'dark'
  })

  const isLightMode = computed(() => {
    if (theme.value.mode === 'auto') {
      return window.matchMedia('(prefers-color-scheme: light)').matches
    }
    return theme.value.mode === 'light'
  })

  const cssVariables = computed(() => {
    const base = isDarkMode.value ? {
      '--bg-primary': '#1a1a1a',
      '--bg-secondary': '#2d2d2d',
      '--text-primary': '#ffffff',
      '--text-secondary': '#a0a0a0',
      '--border-color': '#404040'
    } : {
      '--bg-primary': '#ffffff',
      '--bg-secondary': '#f5f5f5',
      '--text-primary': '#1a1a1a',
      '--text-secondary': '#666666',
      '--border-color': '#e0e0e0'
    }

    return {
      ...base,
      '--primary-color': theme.value.primaryColor,
      '--secondary-color': theme.value.secondaryColor,
      '--accent-color': theme.value.accentColor,
      '--font-size': getFontSizeValue(theme.value.fontSize),
      '--reduced-motion': theme.value.reducedMotion ? 'reduce' : 'no-preference'
    }
  })

  // Actions
  const setThemeMode = (mode: ThemeMode) => {
    theme.value.mode = mode
    applyTheme()
  }

  const setPrimaryColor = (color: string) => {
    theme.value.primaryColor = color
    applyTheme()
  }

  const setSecondaryColor = (color: string) => {
    theme.value.secondaryColor = color
    applyTheme()
  }

  const setAccentColor = (color: string) => {
    theme.value.accentColor = color
    applyTheme()
  }

  const setFontSize = (size: 'small' | 'medium' | 'large') => {
    theme.value.fontSize = size
    applyTheme()
  }

  const setReducedMotion = (reduced: boolean) => {
    theme.value.reducedMotion = reduced
    applyTheme()
  }

  const toggleTheme = () => {
    theme.value.mode = isDarkMode.value ? 'light' : 'dark'
    applyTheme()
  }

  const resetTheme = () => {
    theme.value = {
      mode: 'dark',
      primaryColor: '#1a1a1a',
      secondaryColor: '#2d2d2d',
      accentColor: '#4f46e5',
      fontSize: 'medium',
      reducedMotion: false
    }
    applyTheme()
  }

  const applyTheme = () => {
    const root = document.documentElement
    const variables = cssVariables.value
    
    Object.entries(variables).forEach(([key, value]) => {
      root.style.setProperty(key, value)
    })

    // Update document class for theme
    root.classList.remove('light', 'dark')
    root.classList.add(isDarkMode.value ? 'dark' : 'light')

    // Save to localStorage
    localStorage.setItem('watguessr-theme', JSON.stringify(theme.value))
  }

  const loadTheme = () => {
    const saved = localStorage.getItem('watguessr-theme')
    if (saved) {
      try {
        const savedTheme = JSON.parse(saved)
        theme.value = { ...theme.value, ...savedTheme }
      } catch (error) {
        console.warn('Failed to load saved theme:', error)
      }
    }
    applyTheme()
  }

  // Helper function
  const getFontSizeValue = (size: 'small' | 'medium' | 'large'): string => {
    switch (size) {
      case 'small': return '14px'
      case 'large': return '18px'
      default: return '16px'
    }
  }

  return {
    // State
    theme,
    // Getters
    isDarkMode,
    isLightMode,
    cssVariables,
    // Actions
    setThemeMode,
    setPrimaryColor,
    setSecondaryColor,
    setAccentColor,
    setFontSize,
    setReducedMotion,
    toggleTheme,
    resetTheme,
    applyTheme,
    loadTheme
  }
}) 