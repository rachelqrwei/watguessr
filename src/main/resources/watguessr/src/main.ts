import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

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
  faUsers
} from '@fortawesome/free-solid-svg-icons'

// Add icons to the library
library.add(faPlay, faTrophy, faUser, faCog, faMapMarkerAlt, faChevronDown, faStar, faClipboard, faUsers)

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Register FontAwesome component globally
app.component('font-awesome-icon', FontAwesomeIcon)

app.mount('#app')
