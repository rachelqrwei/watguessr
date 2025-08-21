import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import store from '../stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: Home,
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('../views/Settings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/lobby',
      name: 'lobby',
      component: () => import('../views/Lobby.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/play',
      name: 'play',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/Play.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/leaderboard',
      name: 'leaderboard',
      component: () => import('../views/Leaderboard.vue'),
    },
    {
      path: '/singleplayer-game-end',
      name: 'singleplayerGameEnd',
      component: () => import('../views/game-end-components/SingleplayerGameEnd.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/multiplayer-game-end',
      name: 'multiplayerGameEnd',
      component: () => import('../views/game-end-components/MultiplayerGameEnd.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/ranked-game-end',
      name: 'rankedGameEnd',
      component: () => import('../views/game-end-components/RankedGameEnd.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile/:userId?',
      name: 'profile',
      component: () => import('../views/Profile.vue'),
      props: true
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { left: 0, top: 0 }
  },
})

// Navigation guard for authentication
router.beforeEach((to, from, next) => {
  // Check if route requires authentication
  if (to.meta.requiresAuth) {
    // Get authentication status from Vuex store
    const isAuthenticated = store.getters['user/isAuthenticated']

    // If user is not authenticated, redirect to home
    if (!isAuthenticated) {
      next({ name: 'home' })
      return
    }
  }

  // Allow navigation for authenticated users or public routes
  next()
})

export default router
