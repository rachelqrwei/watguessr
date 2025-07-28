import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
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
  faImage,
  faSearch,
  faExclamationTriangle,
  faFire,
  faChevronLeft,
  faChevronRight
} from '@fortawesome/free-solid-svg-icons'

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
  faImage,
  faSearch,
  faExclamationTriangle,
  faFire,
  faChevronLeft,
  faChevronRight
)

const app = createApp(App)
  .use(store);

app.use(createPinia())
app.use(router)

// Register FontAwesome component globally
app.component('font-awesome-icon', FontAwesomeIcon)

app.mount('#app')
