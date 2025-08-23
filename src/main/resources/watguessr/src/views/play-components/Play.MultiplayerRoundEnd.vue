<template>
  <div class="round-end-container">
    <div class="round-end-panel">
      <div class="round-header">
        <span class="round-label">ROUND #{{ multiplayerGame_getCurrentRound }}</span>
      </div>

      <div class="stats-section">
        <div class="stat-item">
          <span class="stat-label">TIME TAKEN</span>
          <span class="stat-value">{{ displayTimeTaken }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">POINTS</span>
          <span class="stat-value">{{ displayPoints }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">DISTANCE</span>
          <span class="stat-value">{{ displayDistance }}</span>
        </div>
      </div>

      <div class="cards-container">
        <!-- Correct Answer Section -->
        <div class="answer-section" v-if="correctAnswer">
          <div class="answer-header">
            <span class="answer-label">CORRECT ANSWER</span>
          </div>
          <div class="answer-details">
            <div class="answer-item">
              <span class="answer-detail-label">Building:</span>
              <span class="answer-detail-value">{{ correctAnswer.buildingName }}</span>
            </div>
            <div class="answer-item">
              <span class="answer-detail-label">Floor:</span>
              <span class="answer-detail-value">{{ correctAnswer.floor }}</span>
            </div>
          </div>
        </div>

        <!-- Player's Guess Section -->
        <div class="guess-section" v-if="playerGuess">
          <div class="guess-header">
            <span class="guess-label">YOUR GUESS</span>
          </div>
          <div class="guess-details">
            <div class="guess-item">
              <span class="guess-detail-label">Building:</span>
              <span class="guess-detail-value">{{ playerGuess.building || 'Not selected' }}</span>
            </div>
            <div class="guess-item">
              <span class="guess-detail-label">Floor:</span>
              <span class="guess-detail-value">{{ playerGuess.floor || 'Not selected' }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="map-section">
        <div id="answer-map">
        </div>
      </div>

      <!-- Show indicator when displaying aggregated guesses -->
      <div v-if="isAggregatedGuesses" class="aggregated-notice">
        <span class="notice-icon">📊</span>
        <span>Showing guesses from multiple rounds</span>
      </div>

      <!-- Show live updates indicator -->
      <div v-if="isLiveUpdatesActive" class="live-updates-notice">
        <span class="notice-icon">🔄</span>
        <span>Live updates active - watching for new guesses</span>
      </div>

      <div class="map-legend">
        <div class="legend-item">
          <div class="legend-marker answer-marker"></div>
          <span>Correct Answer</span>
        </div>
        <div class="legend-item">
          <div class="legend-marker your-guess-marker"></div>
          <span>Your Guess</span>
        </div>
        <div class="legend-item">
          <div class="legend-marker other-guess-marker"></div>
          <span>Other Players</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {mapGetters} from "vuex";

export default {
  props: {
    points: {
      type: Number,
      required: true
    },
    distance: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      allGuesses: [],
      map: null,
      markers: [],
      currentUser: null,
      isAggregatedGuesses: false,
      guessUpdateSubscription: null,
      gameStateSubscription: null,
      refreshInterval: null,
      markerMap: new Map(), // Track markers by unique key to prevent duplicates
      lastCorrectAnswer: null, // Track last correct answer to detect changes
      completedRoundId: null // Store the round ID for the completed round
    };
  },
  computed: {
    ...mapGetters("multiplayerGame", [
      "multiplayerGame_getCurrentRound",
      "multiplayerGame_getGameId",
      "multiplayerGame_getPlayers"
    ]),
    ...mapGetters('guess', [
      'getGuessTime',
    ]),
    ...mapGetters('round', [
      'getRoundResult',
      'getCorrectAnswer'
    ]),
    ...mapGetters('user', [
      'getCurrentUser'
    ]),
    correctAnswer() {
      return this.getCorrectAnswer;
    },
    playerGuess() {
      return {
        building: this.$store.getters['guess/getGuessBuilding'],
        floor: this.$store.getters['guess/getGuessFloor']
      };
    },
    displayTimeTaken() {
      const ms = Math.floor((this.getGuessTime % 1000) / 10);
      const totalSeconds = Math.floor(this.getGuessTime / 1000);
      const s = Math.floor(totalSeconds % 60);
      const m = Math.floor(totalSeconds / 60);
      const pad = (n, z = 2) => String(n).padStart(z, '0');
      return `${pad(m)}:${pad(s)}.${pad(ms)}`;
    },
    displayDistance() {
      return `${this.distance.toFixed(2)}m`;
    },
    displayPoints() {
      return this.points;
    },
    isLiveUpdatesActive() {
      return this.guessUpdateSubscription !== null || this.gameStateSubscription !== null;
    },
  },
  async mounted() {
    this.currentUser = this.getCurrentUser;
    
    // Capture the round ID for the completed round BEFORE it gets changed
    this.completedRoundId = this.$store.getters['round/getRoundId'];
    
    // Clear any existing data from previous rounds
    this.allGuesses = [];
    this.markers.forEach(marker => marker.remove());
    this.markers = [];
    this.markerMap.clear();
    
    await this.fetchAllGuesses();
    this.renderMap();

    // Subscribe to live updates for new guesses
    this.subscribeToLiveUpdates();

    // Set up periodic refresh as backup
    this.setupPeriodicRefresh();
  },

  beforeUnmount() {
    // Clean up subscriptions and intervals
    this.cleanupLiveUpdates();
    
    // Clean up markers
    this.markers.forEach(marker => marker.remove());
    this.markers = [];
    this.markerMap.clear();
  },
  methods: {
    async fetchAllGuesses() {
      try {
        const gameId = this.multiplayerGame_getGameId;
        if (!gameId) {
          console.error('No game ID available');
          return;
        }

        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/round/by-game-with-guesses?gameId=${gameId}`);
        if (!response.ok) {
          throw new Error(`Failed to fetch guesses: ${response.statusText}`);
        }

        const roundsData = await response.json();

        // Use the completed round ID instead of current round ID
        if (!this.completedRoundId) {
          console.warn('No completed round ID available, clearing guesses');
          this.allGuesses = [];
          return;
        }

        // Find the completed round by exact round ID match
        const completedRound = roundsData.find(round => round.roundId === this.completedRoundId);

        if (completedRound && completedRound.guesses) {
          // Only use guesses from the completed round
          this.allGuesses = completedRound.guesses;
          console.log(`Loaded ${this.allGuesses.length} guesses for completed round ${this.completedRoundId}`);
        } else {
          // No guesses for completed round yet
          this.allGuesses = [];
          console.log(`No guesses found for completed round ${this.completedRoundId}`);
        }
      } catch (error) {
        console.error('Error fetching all guesses:', error);
        // Clear guesses on error to prevent showing stale data
        this.allGuesses = [];
      }
    },

    // Subscribe to live updates for new guesses
    subscribeToLiveUpdates() {
      const gameId = this.multiplayerGame_getGameId;
      if (!gameId) return;

      // Import the WebSocket service dynamically to avoid circular dependencies
      import('@/services/multiplayerGameWebSocket').then(({ connectToMultiplayerGame }) => {
        // Check if we're already connected
        if (window.stompClient && window.stompClient.connected) {
          this.subscribeToGuessUpdates(gameId);
        } else {
          // If not connected, connect and then subscribe
          connectToMultiplayerGame(gameId);
          // Wait a bit for connection to establish
          setTimeout(() => {
            this.subscribeToGuessUpdates(gameId);
          }, 1000);
        }
      });
    },

    // Subscribe to guess updates for the completed round
    subscribeToGuessUpdates(gameId) {
      if (window.stompClient && window.stompClient.connected) {
        // Subscribe to round updates for the completed round
        if (this.completedRoundId) {
          this.guessUpdateSubscription = window.stompClient.subscribe(
            `/topic/round/${this.completedRoundId}/guesses`,
            (message) => {
              this.handleLiveGuessUpdate(JSON.parse(message.body));
            }
          );
        }

        // Also subscribe to game state updates to catch new guesses
        this.gameStateSubscription = window.stompClient.subscribe(
          `/topic/multiplayer-game/${gameId}/state`,
          (message) => {
            // Refresh guesses when game state updates
            this.refreshGuesses();
          }
        );
      }
    },

    // Handle live guess updates
    handleLiveGuessUpdate(newGuess) {
      // Add the new guess to our list
      this.allGuesses.push(newGuess);
      // Add the new marker to the map
      this.addGuessMarker(newGuess, this.allGuesses.length - 1);

      // Refit map to show all markers
      this.fitMapToMarkers();

      // Show notification
      this.showNewGuessNotification(newGuess);
    },

    // Show notification for new guess
    showNewGuessNotification(guess) {
      // Create a temporary notification element
      const notification = document.createElement('div');
      notification.className = 'new-guess-notification';
      notification.innerHTML = `
        <span class="notice-icon">🎯</span>
        <span>${guess.username || 'Player'} just submitted a guess!</span>
      `;

      // Add to the map container
      const mapContainer = document.getElementById('answer-map');
      if (mapContainer) {
        mapContainer.appendChild(notification);

        // Remove after 3 seconds
        setTimeout(() => {
          if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
          }
        }, 3000);
      }
    },
    // Set up periodic refresh as backup
    setupPeriodicRefresh() {
      this.refreshInterval = setInterval(() => {
        this.refreshGuesses();
      }, 2000); // Refresh every 2 seconds
    },

    // Refresh guesses from the backend
    async refreshGuesses() {
      const previousGuessCount = this.allGuesses.length;
      const previousCorrectAnswer = this.lastCorrectAnswer;
      
      await this.fetchAllGuesses();
      
      // Only update markers if there are new guesses or correct answer changed
      const hasNewGuesses = this.allGuesses.length > previousGuessCount;
      const correctAnswerChanged = JSON.stringify(this.correctAnswer) !== JSON.stringify(previousCorrectAnswer);
      
      if (hasNewGuesses || correctAnswerChanged) {
        this.updateMapMarkers();
        this.lastCorrectAnswer = this.correctAnswer ? { ...this.correctAnswer } : null;
      }
    },

    // Update map markers with current guesses
    updateMapMarkers() {
      if (this.map) {
        this.addAllMarkers();
        this.fitMapToMarkers();
      }
    },

    // Clean up live updates
    cleanupLiveUpdates() {
      if (this.guessUpdateSubscription) {
        this.guessUpdateSubscription.unsubscribe();
        this.guessUpdateSubscription = null;
      }

      if (this.gameStateSubscription) {
        this.gameStateSubscription.unsubscribe();
        this.gameStateSubscription = null;
      }

      if (this.refreshInterval) {
        clearInterval(this.refreshInterval);
        this.refreshInterval = null;
      }
    },
    renderMap() {
      mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

      const defaultCenter = [-80.54478250141877, 43.47247223467783];

      this.map = new mapboxgl.Map({
        container: 'answer-map',
        style: 'mapbox://styles/mapbox/streets-v12',
        center: defaultCenter,
        zoom: 17,
      });

      this.map.on('load', () => {
        this.map.resize();
        this.addAllMarkers();
        this.fitMapToMarkers();
      });
    },

    addAllMarkers() {
      // Clear existing markers
      this.markers.forEach(marker => marker.remove());
      this.markers = [];

      // Add correct answer marker (yellow-green with similar brightness/saturation to #FF7F7F)
      if (this.correctAnswer) {
        const answerCoordinates = [
          this.correctAnswer.locationX,
          this.correctAnswer.locationY
        ];

        const answerMarker = new mapboxgl.Marker({
          color: '#B6FF7F', // Light green with same brightness/saturation as #FF7F7F
          scale: 1.2
        })
          .setLngLat(answerCoordinates)
          .setPopup(new mapboxgl.Popup({ offset: 25 })
            .setHTML('<span style="color: black; font-weight: bold;">Correct Answer</span>'))
          .addTo(this.map);

        this.markers.push(answerMarker);
      }

      // Add all players' guess markers
      this.allGuesses.forEach((guess, index) => {
        const isCurrentUser = guess.userId === this.currentUser?.id;
        const coordinates = [guess.guessX, guess.guessY];

        let markerColor, markerLabel;
        if (isCurrentUser) {
          markerColor = '#FF7F7F'; // Red with same brightness/saturation as reference
          markerLabel = 'You';
        } else {
          // Grey with same brightness/saturation as #FF7F7F
          markerColor = '#7F7F7F';
          markerLabel = this.multiplayerGame_getPlayers[guess.userId].username;
        }

        const marker = new mapboxgl.Marker({
          color: markerColor,
          scale: isCurrentUser ? 1.1 : 1.0
        })
          .setLngLat(coordinates)
          .setPopup(new mapboxgl.Popup({ offset: 25 })
            .setHTML(`
              <div style="color: black; font-weight: bold;">
                ${markerLabel}
              </div>
            `))
          .addTo(this.map);

        this.markers.push(marker);
      });
    },

    fitMapToMarkers() {
      if (this.markers.length === 0) {
        // If no markers yet, center on a default location (UW campus)
        this.map.flyTo({
          center: [-80.54478250141877, 43.47247223467783],
          zoom: 17,
          duration: 1000
        });
        return;
      }

      const bounds = new mapboxgl.LngLatBounds();

      // Add all marker coordinates to bounds
      this.markers.forEach(marker => {
        const lngLat = marker.getLngLat();
        // Only extend bounds if coordinates are valid
        if (lngLat.lng && lngLat.lat) {
          bounds.extend(lngLat);
        }
      });

      // Only fit bounds if we have valid bounds
      if (!bounds.isEmpty()) {
        // Fit map to show all markers
        this.map.fitBounds(bounds, {
          padding: 80,
          duration: 1000,
          easing: (t) => t,
        });
      } else {
        // Fallback to default center if bounds are empty
        this.map.flyTo({
          center: [-80.54478250141877, 43.47247223467783],
          zoom: 17,
          duration: 1000
        });
      }
    }
  }
};
</script>

<style scoped>
.round-end-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(8px) saturate(120%);
  -webkit-backdrop-filter: blur(8px) saturate(120%);
  z-index: 1;
}

.round-end-panel {
  width: 95%;
  max-width: 740px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 12px 12px 10px;
  border-radius: 16px;
  background: var(--dark-grey);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.round-header {
  text-align: center;
  padding-top: 16px;
}

.round-label {
  display: block;
  font-size: 1.6rem;
  color: var(--white);
  font-weight: 900;
  letter-spacing: 1px;
  text-transform: uppercase;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  margin-bottom: 0px;
}



.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  gap: 8px;
  width: 100%;
  margin-bottom: 8px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 6px 4px;
  text-align: center;
}

.stat-label {
  display: block;
  font-family: "Istok Web", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  text-transform: uppercase;
  margin-bottom: 0px;
}

.stat-value {
  display: block;
  font-size: 14px;
  font-weight: 900;
  color: var(--yellow);
}

#answer-map {
  width: 100%;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
}

.cards-container {
  display: flex;
  flex-direction: row;
  gap: 8px;
  width: 100%;
  align-items: center;
  justify-content: center;
}

.answer-section {
  width: 45%;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 12px;
  margin-top: 8px;
}

.answer-header {
  text-align: center;
  margin-bottom: 10px;
}

.answer-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.answer-label {
  display: block;
  font-family: "Istok Web", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  text-transform: uppercase;
  margin-bottom: 0px;
}

.answer-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.answer-detail-label {
  display: inline;
  font-size: 10px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.answer-detail-value {
  display: inline;
  font-size: 12px;
  font-weight: 900;
  color: #fff;
  text-align: left;
}

.guess-section {
  width: 45%;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 12px;
  margin-top: 0px;
}

.guess-header {
  text-align: center;
  margin-bottom: 10px;
}

.guess-label {
  display: block;
  font-family: "Istok Web", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 12px;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  text-transform: uppercase;
  margin-bottom: 0px;
}

.guess-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.guess-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  gap: 8px;
}

.guess-detail-label {
  display: inline;
  font-size: 10px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.guess-detail-value {
  display: inline;
  font-size: 12px;
  font-weight: 900;
  color: #fff;
  text-align: left;
}

.map-section {
  width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 12px;
}

.map-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.aggregated-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 6px;
  padding: 6px 12px;
  background: rgba(255, 215, 0, 0.1);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 6px;
  color: var(--yellow);
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.live-updates-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 6px;
  padding: 6px 12px;
  background: rgba(182, 255, 127, 0.1);
  border: 1px solid rgba(182, 255, 127, 0.3);
  border-radius: 6px;
  color: #B6FF7F;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}

.new-guess-notification {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(182, 255, 127, 0.9);
  border: 1px solid rgba(182, 255, 127, 0.5);
  border-radius: 8px;
  color: white;
  font-size: 14px;
  font-weight: 600;
  z-index: 1000;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.notice-icon {
  font-size: 14px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--white);
  font-weight: 500;
}

.legend-marker {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.answer-marker {
  background-color: #B6FF7F;
}

.your-guess-marker {
  background-color: #FF7F7F;
}

.other-guess-marker {
  background-color: #7F7F7F;
}
</style>

