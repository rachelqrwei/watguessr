<template>
  <div class="round-end-container">
    <div class="round-end-panel">
      <div class="round-header">
        <span class="round-label">ROUND</span>
        <span class="round-number">#{{ multiplayerGame_getCurrentRound }}</span>
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
      isAggregatedGuesses: false
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
    ]),
    ...mapGetters('user', [
      'getCurrentUser'
    ]),
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
  },
  async mounted() {
    this.currentUser = this.getCurrentUser;
    await this.fetchAllGuesses();
    this.renderMap();
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
        console.log(roundsData);

        // Find the current round
        const currentRound = roundsData.find(round =>
          round.roundId === this.getRoundResult?.roundId ||
          roundsData.indexOf(round) === this.multiplayerGame_getCurrentRound - 1
        );

        if (currentRound && currentRound.guesses) {
          this.allGuesses = currentRound.guesses;
          console.log('Fetched all guesses:', this.allGuesses);
        }

        if (uniqueUserIds.length > 1) {
          // We have guesses from multiple users across different rounds
          // Use all guesses but mark them as aggregated
          this.allGuesses = allGuessesFromAllRounds;
          this.isAggregatedGuesses = true;
          console.log('Aggregated guesses from multiple rounds:', this.allGuesses);
          console.log('Number of aggregated guesses:', this.allGuesses.length);
          
          // Log each guess for debugging
          this.allGuesses.forEach((guess, index) => {
            console.log(`Aggregated guess ${index + 1}:`, {
              userId: guess.userId,
              username: guess.username,
              guessX: guess.guessX,
              guessY: guess.guessY,
              isCurrentUser: guess.userId === this.currentUser?.id,
              roundId: guess.roundId
            });
          });
          return; // Exit early since we're using aggregated guesses
        }
      } catch (error) {
        console.error('Error fetching all guesses:', error);
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
      if (this.getRoundResult?.correctAnswer) {
        const answerCoordinates = [
          this.getRoundResult.correctAnswer.locationX,
          this.getRoundResult.correctAnswer.locationY
        ];

        const answerMarker = new mapboxgl.Marker({
          color: '#ff4444',
          scale: 1.2
        })
          .setLngLat(answerCoordinates)
          .setPopup(new mapboxgl.Popup({ offset: 25 })
            .setHTML('<span style="color: black; font-weight: bold;">🏆 Correct Answer</span>'))
          .addTo(this.map);

        this.markers.push(answerMarker);
      }

      // Add all players' guess markers
      this.allGuesses.forEach((guess, index) => {
        const isCurrentUser = guess.userId === this.currentUser?.id;
        const coordinates = [guess.guessX, guess.guessY];

        // Different colors for different players
        let markerColor = '#4444ff'; // Default blue for other players
        let markerLabel = 'Other Player';

        if (isCurrentUser) {
          markerColor = '#44ff44'; // Green for current user
          markerLabel = 'Your Guess';
        } else {
          // Generate different colors for other players
          const colors = ['#ff8844', '#8844ff', '#44ffff', '#ff4488', '#88ff44'];
          markerColor = colors[index % colors.length];
          markerLabel = `Player ${index + 1}`;
        }

        const marker = new mapboxgl.Marker({
          color: markerColor,
          scale: isCurrentUser ? 1.1 : 1.0
        })
          .setLngLat(coordinates)
          .setPopup(new mapboxgl.Popup({ offset: 25 })
            .setHTML(`
              <div style="color: black; font-weight: bold;">
                ${markerLabel}<br>
                <small>${guess.username || 'Unknown Player'}</small>
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
        duration: 2000,
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
