import { defineStore } from 'pinia'
import * as authApi from '../api/auth.api'

const STORAGE_KEY = 'taskora_admin_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    // The API has no "current user" endpoint, only /login and /logout.
    // We keep the logged-in admin's info in sessionStorage so a page
    // refresh doesn't kick them back to the login screen. The actual
    // permission check always happens server-side via the session cookie -
    // this is only used to decide what the UI shows.
    user: JSON.parse(sessionStorage.getItem(STORAGE_KEY) || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.user,
    isAdmin: (state) => state.user?.role === 'ADMIN'
  },

  actions: {
    async login(email, password) {
      const user = await authApi.login(email, password)
      this.user = user
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user))
      return user
    },

    async logout() {
      try {
        await authApi.logout()
      } finally {
        this.user = null
        sessionStorage.removeItem(STORAGE_KEY)
      }
    },

    // Called by the http interceptor when the server rejects a request
    // with 401 (session expired/invalid) so the UI state matches reality.
    clearSession() {
      this.user = null
      sessionStorage.removeItem(STORAGE_KEY)
    }
  }
})
