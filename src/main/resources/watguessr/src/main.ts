import './assets/main.css'

import { createApp } from 'vue'
import store from './stores'

import App from './App.vue'
import router from './router'

// FontAwesome imports
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import {
  faPlay,
  faTrophy,
  faUser,
  faCog,
  faMapMarkerAlt,
  faChevronDown,
  faStar,
  faClipboard,
  faUsers,
  faEye,
  faEyeSlash,
  faImage,
  faSearch,
  faExclamationTriangle,
  faFire,
  faChevronLeft,
  faChevronRight,
} from '@fortawesome/free-solid-svg-icons'

// Polyfills for Node.js globals (needed for WebSocket libraries)
if (typeof global === 'undefined') {
  (window as any).global = window;
}
if (typeof process === 'undefined') {
  (window as any).process = { env: {} };
}
if (typeof Buffer === 'undefined') {
  (window as any).Buffer = { isBuffer: () => false };
}

// Add icons to the library
library.add(
  faPlay,
  faTrophy,
  faUser,
  faCog,
  faMapMarkerAlt,
  faChevronDown,
  faStar,
  faClipboard,
  faUsers,
  faEye,
  faEyeSlash,
  faImage,
  faSearch,
  faExclamationTriangle,
  faFire,
  faChevronLeft,
  faChevronRight,
)

const app = createApp(App)
  .use(store)
  .use(router)

// Register FontAwesome component globally
app.component('font-awesome-icon', FontAwesomeIcon)

app.mount('#app')
