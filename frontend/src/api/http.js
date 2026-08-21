import axios from 'axios'

// The Taskora API uses Spring Security session cookies (JSESSIONID), not
// bearer tokens. `withCredentials: true` is required so the browser sends
// and stores that cookie on every request.
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  withCredentials: true,
  // Frontend (localhost:5173) and backend (localhost:8080) are different
  // origins (different port), so axios treats this as cross-origin and
  // won't attach the XSRF-TOKEN cookie as an X-XSRF-TOKEN header by default
  // (security default since axios 1.6.0 / CVE-2023-45857). Safe to force
  // this on here since baseURL is fixed to our own backend only, never an
  // arbitrary host.
  withXSRFToken: true,
  headers: {
    'Content-Type': 'application/json'
  }
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


