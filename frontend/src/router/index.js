import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    name: 'tutorials-list',
    component: () => import('../views/public/TutorialsListView.vue')
  },
  {
    path: '/tutorials/:id',
    name: 'tutorial-detail',
    component: () => import('../views/public/TutorialDetailView.vue'),
    props: true
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('../views/admin/AdminLoginView.vue')
  },
  {
    path: '/admin',
    name: 'admin-dashboard',
    component: () => import('../views/admin/AdminDashboardView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tutorials',
    name: 'admin-tutorials-list',
    component: () => import('../views/admin/AdminTutorialsListView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tutorials/new',
    name: 'admin-tutorial-create',
    component: () => import('../views/admin/AdminTutorialFormView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/tutorials/:id/edit',
    name: 'admin-tutorial-edit',
    component: () => import('../views/admin/AdminTutorialFormView.vue'),
    props: true,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    // Catch-all: must stay last. Vue Router 4 matches routes in array order,
    // so anything above this still wins; unmatched paths fall through here
    // instead of rendering a blank <router-view />.
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/public/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Client-side gate for UX only. The real enforcement is Spring Security on
// the backend - every protected endpoint checks the session cookie and role
// regardless of this guard. This guard exists purely so the wrong audience
// never sees UI they can't actually use.
router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'admin-login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAdmin && authStore.isLoggedIn && !authStore.isAdmin) {
    return { name: 'tutorials-list' }
  }

  if (to.name === 'admin-login' && authStore.isLoggedIn) {
    return authStore.isAdmin
      ? { name: 'admin-dashboard' }
      : { name: 'tutorials-list' }
  }

  return true
})

export default router
