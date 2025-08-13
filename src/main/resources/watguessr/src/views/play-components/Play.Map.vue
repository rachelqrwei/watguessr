<template>
  <div class="map-root">
    <div id='map'></div>
  </div>
</template>
<script>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
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

          // Query building polygons at click
          const buildingFeatures = map.queryRenderedFeatures(e.point, {
            layers: ['building'],
          });

          let buildingName;

          if (buildingFeatures.length > 0) {
            const props = buildingFeatures[0].properties;
            buildingName = props.name || null;
          }

          // If buildingName is null or generic, try finding closest label nearby
          if (!buildingName) {
            // Search a small bbox around click point for poi-label or place-label layers
            const bbox = [
              [e.point.x - 10, e.point.y - 10],
              [e.point.x + 10, e.point.y + 10]
            ];

            const nearbyLabels = map.queryRenderedFeatures(bbox, {
              layers: labelLayerIds
            });

            if (nearbyLabels.length > 0) {
              // Find closest feature by distance in screen pixels
              nearbyLabels.sort((a, b) => {
                const distA = this.distanceToPoint(a.geometry.coordinates, e.point, map);
                const distB = this.distanceToPoint(b.geometry.coordinates, e.point, map);
                return distA - distB;
              });

              const bestLabel = nearbyLabels[0];
              buildingName = bestLabel.properties.name || 'Unnamed Place';
            }
          }

          // Final fallback: reverse geocode
          if (!buildingName) {
            buildingName = await this.reverseGeocode(coords[0], coords[1]);
          }

          const selectedData = {
            building: buildingName,
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
