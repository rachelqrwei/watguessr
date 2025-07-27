<template>
  <div>
    <div id='map'></div>
  </div>
</template>
<script setup>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import { onMounted, ref } from 'vue';

const markerCoordinates = ref([]);

function renderMap() {
  mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

  let map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/streets-v12',
    center: [-80.54478250141877, 43.47247223467783],
    zoom: 17
  })

  map.on('load', () => {
    const layers = map.getStyle().layers ?? [];

    const labelLayers = layers?.filter(
      (layer) =>
        layer.type === 'symbol') ?? [];
    const labelLayerIds = labelLayers.map(layer => layer.id);
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
          'fill-extrusion-opacity': 0.6
        }
      },
      insertBeforeLayerId
    );

    map.on('click', (e) => {
      const coords = [e.lngLat.lng, e.lngLat.lat];
      markerCoordinates.value.push(coords);

      new mapboxgl.Marker({anchor: 'bottom'})
        .setLngLat(coords)
        .addTo(map);

      // const features = map.queryRenderedFeatures(e.point, {
      //   layers: ['3d-buildings'], // ✅ this must match the layer ID from your style
      // });

      // if (features.length > 0) {
      //   console.log('🏢 Clicked building:', features);
      // } else {
      //   console.log('❌ No building found at this location');
      // }

      const labelFeatures = map.queryRenderedFeatures(e.point, {
        layers: labelLayerIds,
      });

      if (labelFeatures.length > 0) {
        const buildingName = labelFeatures[0].properties.name;
        console.log(buildingName);
      } else {
        console.log('❌ No label found at this location');
      }
    });
  });
};

onMounted(() => {
  renderMap();
});
</script>
<style scoped>
body { margin: 0; padding: 0; }

#map {
  position: absolute;
  top: 100px;
  left: 25px;
  width: calc(100vw - 50px);
  height: calc(100vh - 150px);
}
</style>
