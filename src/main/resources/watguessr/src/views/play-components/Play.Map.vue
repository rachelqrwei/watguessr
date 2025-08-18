<template>
  <div class="map-root">
    <div id='map'></div>
  </div>
</template>
<script>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import * as turf from '@turf/turf';
import booleanPointInPolygon from '@turf/boolean-point-in-polygon';
import { point, polygon, multiPolygon } from '@turf/helpers';
import { mapMutations, mapGetters } from 'vuex';

export default {
  name: 'MapboxMap',

  data() {
    return {
      marker: null, // only one marker
      map: null,
      panStepPx: 80,
    };
  },

  computed: {
    ...mapGetters('gameInfo', [
      'getMapCenter',
      'getMapZoom'
    ]),
    ...mapGetters('guess', [
      'getGuessX',
      'getGuessY'
    ])
  },

  watch: {
    getGuessX(newVal) {
      // Clear marker when coordinates are reset (null/undefined)
      if (newVal === null || newVal === undefined) {
        this.clearMarker();
      }
    },
    getGuessY(newVal) {
      // Clear marker when coordinates are reset (null/undefined)
      if (newVal === null || newVal === undefined) {
        this.clearMarker();
      }
    }
  },

  mounted() {
    this.renderMap();
    window.addEventListener('keydown', this.onKeyDown);
  },

  beforeUnmount() {
    window.removeEventListener('keydown', this.onKeyDown);

    // persist map position when leaving the map view
    if (this.map) {
      const c = this.map.getCenter();
      const z = this.map.getZoom();
      this.SET_MAP_CENTER([c.lng, c.lat]);
      this.SET_MAP_ZOOM(z);
    }
  },

  methods: {
    ...mapMutations('guess', [
      'SET_BUILDING_AND_LOCATIONS'
    ]),
    ...mapMutations('gameInfo', [
      'SET_MAP_CENTER',
      'SET_MAP_ZOOM'
    ]),

    async reverseGeocode(lng, lat) {
      const token = import.meta.env.VITE_MAPBOX_TOKEN;
      const url = `https://api.mapbox.com/geocoding/v5/mapbox.places/${lng},${lat}.json?access_token=${token}&types=poi,address`;

      try {
        const response = await fetch(url);
        const data = await response.json();

        if (data?.features?.length > 0) {
          return data.features[0].text; // closest name
        } else {
          return 'Unknown Building';
        }
      } catch (error) {
        return 'Unknown Building';
      }
    },

    // Helper function to calculate screen pixel distance between a feature and a mouse click point
    distanceToPoint(coord, point, map) {
      const pixel = map.project(coord);
      const dx = pixel.x - point.x;
      const dy = pixel.y - point.y;
      return Math.sqrt(dx * dx + dy * dy);
    },
    renderMap() {
      console.log(this.getGuessX, this.getGuessY);
      mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

      const defaultCenter = [-80.54478250141877, 43.47247223467783];
      const center = this.getMapCenter ?? defaultCenter;
      const zoom = this.getMapZoom ?? 17;

      const map = new mapboxgl.Map({
        container: 'map',
        style: 'mapbox://styles/mapbox/streets-v12',
        center: center,
        zoom: zoom,
      });
      this.map = map;

      // Try to disable built-in keyboard panning (arrow keys)
      try {
        if (map && map.keyboard && typeof map.keyboard.disable === 'function') {
          map.keyboard.disable();
        }
      } catch (_) {}

      map.on('load', () => {
        const layers = map.getStyle().layers ?? [];
        const labelLayers = layers?.filter((layer) => layer.type === 'symbol') ?? [];
        const labelLayerIds = labelLayers.map((layer) => layer.id);
        const insertBeforeLayerId = labelLayerIds[0] || undefined;

        map.addLayer(
          {
            id: '3d-buildings',
            source: 'composite',
            'source-layer': 'building',
            filter: ['==', 'extrude', 'true'],
            type: 'fill-extrusion',
            minzoom: 15,
            paint: {
              'fill-extrusion-color': '#ffffff',
              'fill-extrusion-height': ['get', 'height'],
              'fill-extrusion-base': ['get', 'min_height'],
              'fill-extrusion-opacity': 0.6,
            },
          },
          insertBeforeLayerId
        );

        // Restore existing guess marker if present in store
        const x = this.getGuessX;
        const y = this.getGuessY;
        if (typeof x === 'number' && typeof y === 'number') {
          this.marker = new mapboxgl.Marker({ anchor: 'bottom' })
            .setLngLat([x, y])
            .addTo(map);
        }

        map.on('click', async (e) => {
          const coords = [e.lngLat.lng, e.lngLat.lat];

          // Remove old marker
          if (this.marker) this.marker.remove();

          // Add new marker
          this.marker = new mapboxgl.Marker({ anchor: 'bottom' })
            .setLngLat(coords)
            .addTo(map);

          const buildingFeatures = map.queryRenderedFeatures(e.point, { layers: ['building'] });
          let buildingName = null;

          if (buildingFeatures.length > 0) {
            const props = buildingFeatures[0].properties;
            buildingName = props?.name?.trim() || null;

            const geom = buildingFeatures[0].geometry;
            if (geom && Array.isArray(geom.coordinates)) {
              let buildingPolygon;
              if (geom.type === 'Polygon') {
                buildingPolygon = polygon(geom.coordinates);
              } else if (geom.type === 'MultiPolygon') {
                buildingPolygon = multiPolygon(geom.coordinates);
              }

              if (buildingPolygon) {
                const bbox = turf.bbox(buildingPolygon);
                const sw = map.project([bbox[0], bbox[1]]);
                const ne = map.project([bbox[2], bbox[3]]);
                const queryBox = [[sw.x, ne.y], [ne.x, sw.y]];

                const labelFeatures = map.queryRenderedFeatures(queryBox, { layers: labelLayerIds });
                const labelsInside = labelFeatures.filter(lf => {
                  let coords = lf.geometry?.coordinates;

                  // If it's a MultiPoint (array of arrays), take the first point
                  if (Array.isArray(coords) && Array.isArray(coords[0])) {
                    coords = coords[0];
                  }

                  // Validate coordinate format
                  if (
                    !Array.isArray(coords) ||
                    coords.length < 2 ||
                    typeof coords[0] !== 'number' ||
                    typeof coords[1] !== 'number'
                  ) {
                    return false;
                  }

                  return booleanPointInPolygon(point(coords), buildingPolygon);
                });

                const labelWithName = labelsInside.find(l => l.properties?.name?.trim());
                if (labelWithName) buildingName = labelWithName.properties.name;
              }
            }
          }

          // Nearby label fallback
          if (!buildingName) {
            const searchBox = [
              [e.point.x - 10, e.point.y - 10],
              [e.point.x + 10, e.point.y + 10]
            ];
            const nearbyLabels = map.queryRenderedFeatures(searchBox, { layers: labelLayerIds });
            if (nearbyLabels.length > 0) {
              nearbyLabels.sort((a, b) => {
                const distA = this.distanceToPoint(a.geometry.coordinates, e.point, map);
                const distB = this.distanceToPoint(b.geometry.coordinates, e.point, map);
                return distA - distB;
              });
              buildingName = nearbyLabels[0].properties?.name?.trim() || 'Unnamed Place';
            }
          }

          // Reverse geocode fallback
          if (!buildingName) {
            buildingName = await this.reverseGeocode(coords[0], coords[1]);
          }

          this.SET_BUILDING_AND_LOCATIONS({
            building: buildingName,
            guessX: coords[0],
            guessY: coords[1],
          });
        });
      });
    },
    clearMarker() {
      if (this.marker) {
        this.marker.remove();
        this.marker = null;
      }
    }
  }
};
</script>
<style scoped>
body { margin: 0; padding: 0; }

.map-root {
  position: relative;
  width: 100%;
  height: 100%;
}

#map {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}
</style>
