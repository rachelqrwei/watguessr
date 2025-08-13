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
import { point, polygon } from '@turf/helpers';
import { mapMutations } from 'vuex';

export default {
  name: 'MapboxMap',

  data() {
    return {
      marker: null, // only one marker
    };
  },

  mounted() {
    this.renderMap();
  },

  methods: {
    ...mapMutations('guess', [
      'SET_BUILDING_AND_LOCATIONS'
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
        console.error('Reverse geocoding failed:', error);
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
      mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

      const map = new mapboxgl.Map({
        container: 'map',
        style: 'mapbox://styles/mapbox/streets-v12',
        center: [-80.54478250141877, 43.47247223467783],
        zoom: 17,
      });

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

        map.on('click', async (e) => {
          const coords = [e.lngLat.lng, e.lngLat.lat];

          // ✅ Remove previous marker if it exists
          if (this.marker) {
            this.marker.remove();
          }

          // ✅ Add new marker
          this.marker = new mapboxgl.Marker({ anchor: 'bottom' })
            .setLngLat(coords)
            .addTo(map);

          // Query building polygon features at click point
          const buildingFeatures = map.queryRenderedFeatures(e.point, { layers: ['building'] });

          if (buildingFeatures.length === 0) {
            // fallback: no building here
            return;
          }

          // Pick the first building polygon feature
          const buildingFeature = buildingFeatures[0];

          // Construct turf polygon GeoJSON from feature geometry
          const buildingPolygon = polygon(buildingFeature.geometry.coordinates);

          // Query label features in viewport or a bounding box around building polygon
          const bbox = turf.bbox(buildingPolygon); // get bounding box [minX, minY, maxX, maxY]

          // Convert bbox to screen points for queryRenderedFeatures
          const sw = map.project([bbox[0], bbox[1]]);
          const ne = map.project([bbox[2], bbox[3]]);
          const queryBox = [[sw.x, ne.y], [ne.x, sw.y]]; // top-left, bottom-right in screen pixels

          // Get candidate labels in bbox
          const labelFeatures = map.queryRenderedFeatures(queryBox, { layers: labelLayerIds });

          // Filter labels whose point lies inside building polygon
          const labelsInside = labelFeatures.filter(labelFeature => {
            const labelPoint = point(labelFeature.geometry.coordinates);
            return booleanPointInPolygon(labelPoint, buildingPolygon);
          });

          if (labelsInside.length > 0) {
            // Use the first label inside building polygon (or pick best by other criteria)
            // Find the first label that has a non-empty name property
            const labelWithName = labelsInside.find(label => label.properties && label.properties.name && label.properties.name.trim() !== '');

            const labelName = labelWithName ? labelWithName.properties.name : 'Unnamed Place';

            console.log(labelName);
          } else {
            console.log('No label found inside building polygon');
          }

          const selectedData = {
            building: labelName,
            guessX: coords[0],
            guessY: coords[1],
          };

          this.SET_BUILDING_AND_LOCATIONS(selectedData);
        });
      });
    },
  },
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
