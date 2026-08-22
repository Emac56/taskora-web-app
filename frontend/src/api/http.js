import axios from 'axios'

let memoryCsrfToken = null

const http = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  withCredentials: true
})

function getCookie(name) {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'))
  return match ? decodeURIComponent(match[3]) : null
}

function extractToken(headers) {
  if (!headers) return null
  return (
    headers['x-xsrf-token'] ||
    headers['X-XSRF-TOKEN'] ||
    (typeof headers.get === 'function' ? headers.get('x-xsrf-token') : null)
  )
}

function saveCsrfToken(token) {
  if (token) {
    memoryCsrfToken = token
    if (typeof window !== 'undefined' && window.sessionStorage) {
      sessionStorage.setItem('csrf_token', token)
    }
  }
}

function getStoredCsrfToken() {
  return (
    memoryCsrfToken ||
    (typeof window !== 'undefined' && window.sessionStorage
      ? sessionStorage.getItem('csrf_token')
      : null) ||
    getCookie('XSRF-TOKEN')
  )
}

// Request Interceptor: Attach X-XSRF-TOKEN header to all state-changing requests
http.interceptors.request.use(
  (config) => {
    const token = getStoredCsrfToken()
    if (token) {
      config.headers['X-XSRF-TOKEN'] = token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response Interceptor: Capture X-XSRF-TOKEN header from any incoming response
http.interceptors.response.use(
  (response) => {
    const token = extractToken(response.headers)
    saveCsrfToken(token)
    return response
  },
  (error) => {
    const token = extractToken(error.response?.headers)
    saveCsrfToken(token)
    return Promise.reject(error)
  }
)

export default http
