<template>
  <div>
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

          // Try to get label from map
          const labelFeatures = map.queryRenderedFeatures(e.point, {
            layers: labelLayerIds,
          });

          let buildingName;

          if (labelFeatures.length > 0 && labelFeatures[0].properties.name) {
            buildingName = labelFeatures[0].properties.name;
          } else {
            // ❌ No building label on map —> use reverse geocoding
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

#map {
  position: absolute;
  left: 25px;
  width: calc(100vw - 50px);
  height: calc(100vh - 150px);
  z-index: 1;
}
</style>
