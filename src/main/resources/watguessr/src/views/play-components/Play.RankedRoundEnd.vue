<template>
  <div class="round-end-container">
    <div class="round-end-panel">
      <div class="round-header">
        <span class="round-label">ROUND</span>
        <span class="round-number">#{{ rankedGame_getCurrentRound }}</span>
      </div>

      <div class="points-section">
        <span class="points-label">POINTS</span>
        <span class="points-value">{{ displayPoints }}</span>
      </div>

      <div class="stats-section">
        <div class="stat-item">
          <span class="stat-label">TIME TAKEN</span>
          <span class="stat-value">{{ displayTimeTaken }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">DISTANCE</span>
          <span class="stat-value">{{ displayDistance }}</span>
        </div>
      </div>

      <div class="map-section">
        <div id="answer-map">
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
      refreshInterval: null
    };
  },
  computed: {
    ...mapGetters("rankedGame", [
      "rankedGame_getCurrentRound",
      "rankedGame_getGameId",
      "rankedGame_getPlayers"
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
    displayTimeTaken() {
      const ms = Math.floor((this.getGuessTime % 1000) / 10);
      const totalSeconds = Math.floor(this.getGuessTime / 1000);
      const s = Math.floor(totalSeconds % 60);
      const m = Math.floor(totalSeconds / 60);
      const pad = (n, z = 2) => String(n).padStart(z, '0');
      return `${pad(m)}:${pad(s)}.${pad(ms)}`;
    },
    displayDistance() {
      return this.distance;
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
  },
  methods: {
    async fetchAllGuesses() {
      try {
        const gameId = this.rankedGame_getGameId;
        if (!gameId) {
          console.error('No game ID available');
          return;
        }

        const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/round/by-game-with-guesses?gameId=${gameId}`);
        if (!response.ok) {
          throw new Error(`Failed to fetch guesses: ${response.statusText}`);
        }

        const roundsData = await response.json();

        // Find the current round
        const currentRound = roundsData.find(round =>
          round.roundId === this.getRoundResult?.roundId ||
          roundsData.indexOf(round) === this.rankedGame_getCurrentRound - 1
        );

        if (currentRound && currentRound.guesses) {
          this.allGuesses = currentRound.guesses;
        }
      } catch (error) {
        console.error('Error fetching all guesses:', error);
      }
    },

    // Subscribe to live updates for new guesses
    subscribeToLiveUpdates() {
      const gameId = this.rankedGame_getGameId;
      if (!gameId) return;

      // Import the WebSocket service dynamically to avoid circular dependencies
      import('@/services/rankedGameWebSocket').then(({ connectToRankedGame }) => {
        // Check if we're already connected
        if (window.stompClient && window.stompClient.connected) {
          this.subscribeToGuessUpdates(gameId);
        } else {
          // If not connected, connect and then subscribe
          connectToRankedGame(gameId);
          // Wait a bit for connection to establish
          setTimeout(() => {
            this.subscribeToGuessUpdates(gameId);
          }, 1000);
        }
      });
    },

    // Subscribe to guess updates for the current round
    subscribeToGuessUpdates(gameId) {
      if (window.stompClient && window.stompClient.connected) {
        // Subscribe to round updates for the current round
        const roundId = this.getRoundResult?.roundId;
        if (roundId) {
          this.guessUpdateSubscription = window.stompClient.subscribe(
            `/topic/round/${roundId}/guesses`,
            (message) => {
              console.log('🎯 Received live guess update:', message.body);
              this.handleLiveGuessUpdate(JSON.parse(message.body));
            }
          );
          console.log('📡 Subscribed to live guess updates for round:', roundId);
        }

        // Also subscribe to game state updates to catch new guesses
        this.gameStateSubscription = window.stompClient.subscribe(
          `/topic/ranked-game/${gameId}/state`,
          (message) => {
            console.log('📊 Received game state update in round end:', message.body);
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

      console.log('✅ Added live guess update:', newGuess);
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
      await this.fetchAllGuesses();
      this.updateMapMarkers();
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

      // Add correct answer marker (red)
      if (this.correctAnswer) {
        const answerCoordinates = [
          this.correctAnswer.locationX,
          this.correctAnswer.locationY
        ];

        const answerMarker = new mapboxgl.Marker({
          color: '#ff4444',
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
          markerColor = '#44ff44'; // Green for current user
          markerLabel = 'You';
        } else {
          // Generate different colors for other players
          const colors = ['#ff8844', '#8844ff', '#44ffff', '#ff4488', '#88ff44'];
          markerColor = colors[index % colors.length];
          markerLabel = this.rankedGame_getPlayers[guess.userId].username;
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
      if (this.markers.length === 0) return;

      const bounds = new mapboxgl.LngLatBounds();

      // Add all marker coordinates to bounds
      this.markers.forEach(marker => {
        bounds.extend(marker.getLngLat());
      });

      // Fit map to show all markers
      this.map.fitBounds(bounds, {
        padding: 80,
        duration: 1000,
        easing: (t) => t,
      });
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
  gap: 12px;
  padding: 24px 24px 20px;
  border-radius: 16px;
  background: var(--dark-grey);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.round-header {
  text-align: center;
}

.round-label {
  display: block;
  font-size: 16px;
  color: var(--yellow);
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 0px;
}

.round-number {
  display: block;
  font-size: 42px;
  font-weight: 900;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.points-section {
  text-align: center;
  padding: 5px 28px;
}

.points-label {
  display: block;
  font-size: 13px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 0px;
}

.points-value {
  display: block;
  font-size: 28px;
  font-weight: 900;
  color: #fff;
}

.stats-section {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 5px 10px;
  min-width: 80px;
}

.stat-label {
  display: block;
  font-family: "Red Hat Text", sans-serif;
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
  font-size: 16px;
  font-weight: 900;
  color: var(--yellow);
}

#answer-map {
  width: 100%;
  height: 300px;
}

.map-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 15px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.aggregated-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
  padding: 8px 16px;
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
  margin-top: 10px;
  padding: 8px 16px;
  background: rgba(0, 255, 0, 0.1);
  border: 1px solid rgba(0, 255, 0, 0.3);
  border-radius: 6px;
  color: #00ff00;
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
  background: rgba(0, 255, 0, 0.9);
  border: 1px solid rgba(0, 255, 0, 0.5);
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
  background-color: #ff4444;
}

.your-guess-marker {
  background-color: #44ff44;
}

.other-guess-marker {
  background-color: #4444ff;
}
</style>
