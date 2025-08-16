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
    window.addEventListener('keydown', this.onKeyDown);
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
    ]),
    ...mapGetters('round', [
      'getRoundResult',
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
  methods: {
    renderMap() {
      mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

      // Example coordinates (lng, lat)
      const point1 = [-79.3832, 43.6532]; // Toronto
      const point2 = [-73.5673, 45.5017]; // Montreal

      const defaultCenter = [-80.54478250141877, 43.47247223467783];
      // const center = this.getMapCenter ?? defaultCenter;
      // const zoom = this.getMapZoom ?? 17;

      const map = new mapboxgl.Map({
        container: 'answer-map',
        style: 'mapbox://styles/mapbox/streets-v12',
        center: defaultCenter,
        zoom: 17,
      });

      this.guessMarker = new mapboxgl.Marker({ color: 'blue' })
        .setLngLat(point1)
        .addTo(map);

      this.answerMarker = new mapboxgl.Marker({ color: 'red' })
        .setLngLat(point2)
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
              coordinates: [point1, point2]
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
  font-size: 12px;
  color: #bbb;
  font-weight: 700;
  letter-spacing: 0.08em;
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


#answer-map {
  width: 100%;
  height: 300px;
}
</style>
