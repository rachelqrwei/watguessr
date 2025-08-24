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
  faPen,
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
  faUserGroup,
  faTimes,
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

// Disable Mapbox telemetry to avoid CORS errors
(window as any).MapboxGLTelemetryDisabled = true;

// Add icons to the library
library.add(
  faPlay,
  faTrophy,
  faUser,
  faCog,
  faPen,
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
  faUserGroup,
  faTimes,
)

// Global fetch wrapper to detect expired/invalid sessions
const originalFetch = window.fetch.bind(window);
window.fetch = async (input: RequestInfo | URL, init?: RequestInit) => {
  const res = await originalFetch(input as any, init);
  try {
    const url = typeof input === 'string' ? input : (input as Request).url;
    const status = (res as Response).status;
    const isLogout = url?.includes('/api/auth/logout');
    const isPublicEndpoint = url?.includes('/api/user/leaderboard') || 
                            url?.includes('/api/round/by-game-with-guesses') ||
                            url?.includes('/api/auth/') ||
                            url?.includes('/api/user/') && url?.includes('/leaderboard') ||
                            url?.includes('/api/user/') && url?.includes('/match-history');
    
    // Only trigger logout for authenticated endpoints that return 401/403
    // Don't trigger logout for public endpoints or CORS preflight failures
    if (!isLogout && !isPublicEndpoint && (status === 401 || status === 403)) {
      // Check if this might be a CORS issue by looking at response headers
      const hasContentType = res.headers.get('content-type');
      
      // Only logout if we actually got a response from the server (not a CORS error)
      if (hasContentType) {
        console.log('Logging out because of 401/403');
        // Clear auth and prompt login with a reason message
        store.commit('user/CLEAR_AUTH');
        store.commit('user/OPEN_LOGIN', 'Your session expired. Please log in again.');
      }
    }
  } catch {
    // no-op
  }
  return res;
};

// clear profile state only when navigating away from the profile page
router.afterEach((to, from) => {
  const isToProfile = typeof to?.path === 'string' && to.path.startsWith('/profile')
  const isFromProfile = typeof from?.path === 'string' && from.path.startsWith('/profile')
  if (isFromProfile && !isToProfile) {
    store.commit('profile/CLEAR_PROFILE_USER_ID')
  }
})

const app = createApp(App)
  .use(store)
  .use(router)

// Register FontAwesome component globally
app.component('font-awesome-icon', FontAwesomeIcon)

app.mount('#app')
