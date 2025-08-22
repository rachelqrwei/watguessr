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
)

// Global fetch wrapper to detect expired/invalid sessions
const originalFetch = window.fetch.bind(window);
window.fetch = async (input: RequestInfo | URL, init?: RequestInit) => {
  const res = await originalFetch(input as any, init);
  try {
    const url = typeof input === 'string' ? input : (input as Request).url;
    const status = (res as Response).status;
    const isLogout = url?.includes('/api/auth/logout');
    if (!isLogout && (status === 401 || status === 403)) {
      // Clear auth and prompt login with a reason message
      store.commit('user/CLEAR_AUTH');
      store.commit('user/OPEN_LOGIN', 'Your session expired. Please log in again.');
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
