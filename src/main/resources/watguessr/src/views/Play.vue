<script setup>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import { onMounted, ref } from 'vue';

const markerCoordinates = ref([]);

onMounted(() => {
  mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

  let map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/streets-v12',
    center: [-80.54478250141877, 43.47247223467783 ],
    zoom: 17,
    pitch: 70,
    bearing: -60, // rotate map slightly
  })
  
  map.on('load', () => {    
    const layers = map.getStyle().layers ?? [];

    console.log(layers);
    const labelLayerId = layers?.find(
      layer => layer.type === 'symbol' && layer.layout?.['text-field']
    )?.id;

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
      labelLayerId, // put the 3D layer below labels
    );

    map.on('click', (e) => {
      const coords = [e.lngLat.lng, e.lngLat.lat];
      markerCoordinates.value.push(coords);

      new mapboxgl.Marker({ anchor: 'bottom' })
        .setLngLat(coords)
        .addTo(map);

      // Adjust to match your actual layer ID
      const features = map.queryRenderedFeatures(e.point, {
        layers: ['3d-buildings'], // ✅ this must match the layer ID from your style
      });

      if (features.length > 0) {
        console.log('🏢 Clicked building:', features);
      } else {
        console.log('❌ No building found at this location');
      }
    });
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