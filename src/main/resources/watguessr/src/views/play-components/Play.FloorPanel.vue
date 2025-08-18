<template>
  <div class="selection-display">
    <div class="selection-header">
      <div class="selection-round">ROUND {{ round }}</div>
      <div class="selection-building">{{ building || 'Select a building' }}</div>
      <div class="selection-coords">Lat: {{ formattedLat }} · Long: {{ formattedLong }}</div>
    </div>
    <div class="selection-divider"></div>
    <div class="floors-title">FLOORS</div>
    <div class="floors-list">
      <button
        v-for="opt in floorOptions"
        :key="opt.value"
        type="button"
        class="floor-row"
        :class="{ 'is-selected': floor === opt.value }"
        @click="selectFloor(opt.value)"
      >
        <span class="floor-dot" :style="{ backgroundColor: opt.color, boxShadow: `0 0 12px ${opt.color}55` }"></span>
        <span class="floor-label">{{ opt.value }}</span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PlayFloorPanel',
  props: {
    round: { type: [Number, String], required: true },
    building: { type: String, default: '' },
    lat: { type: [Number, String], default: null },
    lng: { type: [Number, String], default: null },
    floors: { type: Array, default: () => [] },
    floor: { type: [String, Number], default: '' }
  },
  emits: ['update:floor'],
  computed: {
    formattedLat() {
      const v = this.lat;
      const n = Number(v);
      return isNaN(n) ? '-' : n.toFixed(13);
    },
    formattedLong() {
      const v = this.lng;
      const n = Number(v);
      return isNaN(n) ? '-' : n.toFixed(13);
    },
    floorOptions() {
      const floors = [...this.floors].sort((a, b) => {
        const na = parseFloat(a);
        const nb = parseFloat(b);
        if (isNaN(na) || isNaN(nb)) return String(a).localeCompare(String(b));
        return na - nb;
      });
      const count = floors.length || 1;
      return floors.map((f, idx) => ({ value: f, color: this.getFloorColor(idx, count) }));
    }
  },
  methods: {
    selectFloor(value) {
      this.$emit('update:floor', value);
    },
    getFloorColor(index, count) {
      const palette = ['#B6FF7F', '#FFE37F', '#FFB07F', '#FF7F7F'];
      if (count <= 1) return palette[0];
      const scaled = (index * (palette.length - 1)) / Math.max(count - 1, 1);
      const li = Math.floor(scaled);
      const ri = Math.min(li + 1, palette.length - 1);
      const t = scaled - li;
      const hexToRgb = (hex) => {
        const n = hex.replace('#','');
        return { r: parseInt(n.slice(0,2),16), g: parseInt(n.slice(2,4),16), b: parseInt(n.slice(4,6),16) };
      };
      const rgbToHex = (r,g,b) => '#' + [r,g,b].map(x => x.toString(16).padStart(2,'0')).join('');
      const lerp = (a,b,t) => Math.round(a + (b - a) * t);
      const a = hexToRgb(palette[li]);
      const b = hexToRgb(palette[ri]);
      return rgbToHex(lerp(a.r,b.r,t), lerp(a.g,b.g,t), lerp(a.b,b.b,t));
    }
  }
}
</script>

<style scoped>
.selection-display {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 14px;
  width: 320px;
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  color: var(--white);
}

.selection-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.selection-round {
  font-size: 1.25rem;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.selection-building {
  font-size: 16px;
  font-weight: 400;
  color: #d6d6d6;
}

.selection-coords {
  font-family: "Red Hat Text", sans-serif;
  font-style: normal;
  font-weight: 400;
  font-size: 11px;
  letter-spacing: 0.8px;
  color: var(--light-grey);
  line-height: 1.6;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
}

.selection-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.15);
  margin: 2px 0 2px;
}

.floors-title {
  font-weight: 800;
  letter-spacing: 0.5px;
  font-size: 13px;
}

.floors-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.floor-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  width: 100%;
  color: var(--white);
  background: transparent;
  border: 1px solid transparent;
  border-radius: 9px;
  cursor: pointer;
}

.floor-row.is-selected {
  border-color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.08);
}

.floor-dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  flex: 0 0 18px;
}

.floor-label {
  font-size: 16px;
  font-weight: 400;
  color: #d6d6d6;
}
</style> 