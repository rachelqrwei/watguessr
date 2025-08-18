<template>
  <div class="round-end-container">
    <div class="round-end-panel">
      <div class="round-header">
        <span class="round-label">ROUND</span>
        <span class="round-number">#{{ singleplayerGame_getCurrentRound }}</span>
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

      <div class="flex-container justify-between columns-12">
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
    </div>
  </div>
</template>

<script>
import 'mapbox-gl/dist/mapbox-gl.css';
import {mapGetters} from "vuex";
import mapboxgl from "mapbox-gl";

export default {
  data() {
    return {
      answerMarker: null,
      guessMarker: null,
    }
  },
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
  mounted() {
    this.renderMap();
  },

  // beforeUnmount() {
  //   window.removeEventListener('keydown', this.onKeyDown);
  //
  //   // persist map position when leaving the map view
  //   if (this.map) {
  //     const c = this.map.getCenter();
  //     const z = this.map.getZoom();
  //     this.SET_MAP_CENTER([c.lng, c.lat]);
  //     this.SET_MAP_ZOOM(z);
  //   }
  // },
  computed: {
    ...mapGetters("singleplayerGame", [
      "singleplayerGame_getCurrentRound"
    ]),
    ...mapGetters('guess', [
      'getGuessTime',
      'getGuessX',
      'getGuessY'
    ]),
    ...mapGetters('round', [
      "getRoundId",
      'getRoundResult',
      'getCorrectAnswer'
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
      return this.distance;
    },
    displayPoints() {
      return this.points;
    },
  },
  methods: {
    renderMap() {
      mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

      const guessCoordinates = [this.getGuessX, this.getGuessY];
      const answerCoordinates = [this.getCorrectAnswer.locationX, this.getCorrectAnswer.locationY];

      const defaultCenter = [-80.54478250141877, 43.47247223467783];

      const map = new mapboxgl.Map({
        container: 'answer-map',
        style: 'mapbox://styles/mapbox/streets-v12',
        center: defaultCenter,
        zoom: 17,
      });

      this.guessMarker = new mapboxgl.Marker({ color: 'blue' })
        .setLngLat(guessCoordinates)
        .setPopup(new mapboxgl.Popup({ offset: 25, color: 'black' })
          .setHTML('<span style="color: black; font-weight: bold;">Guess</span>')
        ) // Label
        .addTo(map);

      this.answerMarker = new mapboxgl.Marker({ color: 'red' })
        .setLngLat(answerCoordinates)
        .setPopup(new mapboxgl.Popup({ offset: 25 })
          .setHTML('<span style="color: black; font-weight: bold;">Answer</span>')) // Label
        .addTo(map);

      map.on('load', () => {
        map.resize();

        // Compute bounds that include both markers
        const bounds = new mapboxgl.LngLatBounds();
        bounds.extend(this.guessMarker.getLngLat());
        bounds.extend(this.answerMarker.getLngLat());

        // Animate zooming/panning to fit both points
        map.fitBounds(bounds, {
          padding: 80,       // space around markers
          duration: 2000,    // animation length (ms)
          easing: (t) => t,  // linear easing (you can play with easing functions)
        });

        map.addSource('line', {
          type: 'geojson',
          data: {
            type: 'Feature',
            geometry: {
              type: 'LineString',
              coordinates: [guessCoordinates, answerCoordinates]
            }
          }
        });

        map.addLayer({
          id: 'line',
          type: 'line',
          source: 'line',
          layout: {},
          paint: {
            'line-color': 'black',
            'line-width': 3
          }
        });
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

.map-section {
  width: 100%;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 12px;
}

.answer-section {
  width: 50%;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px;
}

.answer-header {
  text-align: center;
  margin-bottom: 10px;
}

.answer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.answer-label {
  display: block;
  font-size: 13px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 0px;
}

.answer-details {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.answer-detail-label {
  display: block;
  font-size: 12px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-right: 10px;
}

.answer-detail-value {
  display: block;
  font-size: 14px;
  font-weight: 900;
  color: #fff;
}

.guess-section {
  width: 50%;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 12px;
  margin-top: 12px;
}

.guess-header {
  text-align: center;
  margin-bottom: 10px;
}

.guess-label {
  display: block;
  font-size: 13px;
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.1em;
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
  justify-content: space-between;
  align-items: center;
}

.guess-detail-label {
  display: block;
  font-size: 12px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-right: 10px;
}

.guess-detail-value {
  display: block;
  font-size: 14px;
  font-weight: 900;
  color: #fff;
}

#answer-map {
  width: 100%;
  height: 300px;
}
</style>
