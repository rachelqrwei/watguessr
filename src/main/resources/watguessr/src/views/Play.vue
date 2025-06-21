<script setup>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';
import { onMounted } from 'vue';

onMounted(() => {
  mapboxgl.accessToken = import.meta.env.VITE_MAPBOX_TOKEN;

  let map = new mapboxgl.Map({
    container: 'map',
    style: 'mapbox://styles/mapbox/standard',
    center: [-80.54478250141877, 43.47247223467783 ],
    zoom: 17,
    pitch: 70,
    bearing: -60, // rotate map slightly
  })
  
  map.on("load", () => {
    const layers = map.getStyle.layers;
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
          'fill-extrusion-color': '#aaa',
          'fill-extrusion-height': ['get', 'height'],
          'fill-extrusion-base': ['get', 'min_height'],
          'fill-extrusion-opacity': 0.6
        }
      },
      labelLayerId // put the 3D layer below labels
    );
  });

  map.on('style.load', () => {
    map.setConfigProperty('basemap', 'lightPreset', 'dusk');
  });
});
</script>
<template>
    <div id='map' style='width: 800px; height: 800px;'></div>
</template>
<style>
/* body { margin: 0; padding: 0; } */
/* #map { position: absolute; top: 0; bottom: 0; width: 100%; } */
</style>