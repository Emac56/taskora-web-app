import axios from 'axios'

// The Taskora API uses Spring Security session cookies (JSESSIONID), not
// bearer tokens. `withCredentials: true` is required so the browser sends
// and stores that cookie on every request.
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// In-memory only, refreshed from every response (see interceptor below).
// A page reload just goes back to null and re-fills on the next request
// instead of going stale.
let csrfToken = null

// We used to rely on axios's built-in `withXSRFToken`, which reads the
// XSRF-TOKEN cookie via document.cookie and mirrors it into the
// X-XSRF-TOKEN header. That only works when frontend and backend share a
// domain. In production, frontend (Vercel) and backend (Render) are
// different domains — cookies are scoped to the domain that set them, so
// this page's JS can never see that cookie no matter what CORS allows.
// The backend mirrors the token onto an X-XSRF-TOKEN *response* header
// instead (see CsrfCookieFilter), which we capture here and re-attach
// ourselves on the next request.
http.interceptors.response.use((response) => {
  const token = response.headers['x-xsrf-token']
  if (token) {
    csrfToken = token
  }
  return response
})

http.interceptors.request.use((config) => {
  if (csrfToken) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }
  return config
})

// Extract the backend's { success, message } error shape (ApiErrorResponse)
// so components can just show error.message, and clear the local session
// if the server says we're no longer authenticated.
http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const backendMessage = error.response?.data?.message
    error.message = backendMessage || error.message

    if (error.response?.status === 401) {
      // Dynamic import avoids a circular import at module-load time
      // (store -> auth.api -> http -> store).
      const { useAuthStore } = await import('../stores/auth')
      useAuthStore().clearSession()
    }

    return Promise.reject(error)
  }
)

export default http

