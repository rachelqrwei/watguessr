<script setup>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import { onMounted, ref } from 'vue';

const markerCoordinates = ref([]);

onMounted(() => {
  mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

  let map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/standard',
    center: [-80.54478250141877, 43.47247223467783 ],
    zoom: 16,
    pitch: 70,
    bearing: -60, // rotate map slightly
  })
  
  map.on('load', () => {    
    // Now safe to add click handler that queries the 'building' layer
    map.on('click', (e) => {
      const coords = [e.lngLat.lng, e.lngLat.lat];
      markerCoordinates.value.push(coords);

      new mapboxgl.Marker({ anchor: 'bottom' })
        .setLngLat(coords)
        .addTo(map);    
    });
  });

  map.on('style.load', () => {
    map.setConfigProperty('basemap', 'lightPreset', 'dusk');
  });
});
</script>
<template>
    <div id='map'></div>
</template>
<style>
body { margin: 0; padding: 0; }
#map { position: absolute; top: 0; bottom: 0; width: calc(100% - 200px); height: 800px;}
</style>