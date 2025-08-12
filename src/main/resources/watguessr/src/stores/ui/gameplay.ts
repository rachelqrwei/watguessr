import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export type GameState = 'idle' | 'loading' | 'playing' | 'paused' | 'guessing' | 'results' | 'gameOver'

export interface GameplayState {
  currentState: GameState
  currentRound: number
  totalRounds: number
  timeRemaining: number
  score: number
  streak: number
  isGuessing: boolean
  showMap: boolean
  showResults: boolean
}

export const useGameplayStore = defineStore('gameplay', () => {
  // State
  const gameplay = ref<GameplayState>({
    currentState: 'idle',
    currentRound: 0,
    totalRounds: 5,
    timeRemaining: 0,
    score: 0,
    streak: 0,
    isGuessing: false,
    showMap: false,
    showResults: false
  })

  const timer = ref<number | null>(null)

  // Getters
  const isPlaying = computed(() => gameplay.value.currentState === 'playing')
  const isPaused = computed(() => gameplay.value.currentState === 'paused')
  const isGameOver = computed(() => gameplay.value.currentState === 'gameOver')
  const isGuessing = computed(() => gameplay.value.isGuessing)
  const canGuess = computed(() => isPlaying.value && !isGuessing.value && gameplay.value.timeRemaining > 0)
  const progress = computed(() => (gameplay.value.currentRound / gameplay.value.totalRounds) * 100)
  const timeFormatted = computed(() => {
    const minutes = Math.floor(gameplay.value.timeRemaining / 60)
    const seconds = gameplay.value.timeRemaining % 60
    return `${minutes}:${seconds.toString().padStart(2, '0')}`
  })

  // Actions
  const startGame = (totalRounds: number = 5) => {
    gameplay.value = {
      currentState: 'loading',
      currentRound: 0,
      totalRounds,
      timeRemaining: 0,
      score: 0,
      streak: 0,
      isGuessing: false,
      showMap: false,
      showResults: false
    }
  }

  const startRound = (timeLimit: number = 60) => {
    gameplay.value.currentState = 'playing'
    gameplay.value.currentRound++
    gameplay.value.timeRemaining = timeLimit
    gameplay.value.isGuessing = false
    gameplay.value.showMap = false
    gameplay.value.showResults = false
    startTimer()
  }

  const pauseGame = () => {
    gameplay.value.currentState = 'paused'
    stopTimer()
  }

  const resumeGame = () => {
    gameplay.value.currentState = 'playing'
    startTimer()
  }

  const endRound = () => {
    gameplay.value.currentState = 'results'
    gameplay.value.showResults = true
    stopTimer()
  }

  const nextRound = () => {
    if (gameplay.value.currentRound >= gameplay.value.totalRounds) {
      endGame()
    } else {
      gameplay.value.currentState = 'loading'
      gameplay.value.showResults = false
    }
  }

  const endGame = () => {
    gameplay.value.currentState = 'gameOver'
    stopTimer()
  }

  const resetGame = () => {
    gameplay.value = {
      currentState: 'idle',
      currentRound: 0,
      totalRounds: 5,
      timeRemaining: 0,
      score: 0,
      streak: 0,
      isGuessing: false,
      showMap: false,
      showResults: false
    }
    stopTimer()
  }

  const addScore = (points: number) => {
    gameplay.value.score += points
    if (points > 0) {
      gameplay.value.streak++
    } else {
      gameplay.value.streak = 0
    }
  }

  const setGuessing = (guessing: boolean) => {
    gameplay.value.isGuessing = guessing
  }

  const toggleMap = () => {
    gameplay.value.showMap = !gameplay.value.showMap
  }

  const showMap = () => {
    gameplay.value.showMap = true
  }

  const hideMap = () => {
    gameplay.value.showMap = false
  }

  const startTimer = () => {
    if (timer.value) clearInterval(timer.value)
    
    timer.value = setInterval(() => {
      if (gameplay.value.timeRemaining > 0) {
        gameplay.value.timeRemaining--
      } else {
        endRound()
      }
    }, 1000)
  }

  const stopTimer = () => {
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
  }

  const setTimeRemaining = (time: number) => {
    gameplay.value.timeRemaining = time
  }

  const setCurrentRound = (round: number) => {
    gameplay.value.currentRound = round
  }

  const setTotalRounds = (total: number) => {
    gameplay.value.totalRounds = total
  }

  return {
    // State
    gameplay,
    // Getters
    isPlaying,
    isPaused,
    isGameOver,
    isGuessing,
    canGuess,
    progress,
    timeFormatted,
    // Actions
    startGame,
    startRound,
    pauseGame,
    resumeGame,
    endRound,
    nextRound,
    endGame,
    resetGame,
    addScore,
    setGuessing,
    toggleMap,
    showMap,
    hideMap,
    setTimeRemaining,
    setCurrentRound,
    setTotalRounds
  }
}) 