import { describe, it, expect, beforeEach, vi } from 'vitest'
import http from './http'

describe('HTTP Client & Cross-Origin CSRF Interceptors', () => {
  beforeEach(() => {
    // Linisin ang storage at cookies bago ang bawat test
    sessionStorage.clear()
    document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
    vi.restoreAllMocks()
  })

  it('1. should not attach X-XSRF-TOKEN header when no token is present', async () => {
    let capturedConfig = null

    // Mock custom adapter para ma-inspect ang outgoing request headers
    http.defaults.adapter = async (config) => {
      capturedConfig = config
      return {
        data: { success: true },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      }
    }

    await http.get('/api/v1/tutorials')

    expect(capturedConfig.headers['X-XSRF-TOKEN']).toBeUndefined()
  })

  it('2. should extract x-xsrf-token from response headers and save to sessionStorage', async () => {
    const mockCsrfToken = 'backend-csrf-token-abc-123'

    http.defaults.adapter = async (config) => {
      return {
        data: { success: true },
        status: 200,
        statusText: 'OK',
        headers: {
          'x-xsrf-token': mockCsrfToken
        },
        config
      }
    }

    // Unang request (hal. Login o initial GET)
    await http.get('/api/v1/tutorials')

    // Tiyaking na-save ang token sa sessionStorage para sa cross-origin SPA
    expect(sessionStorage.getItem('csrf_token')).toBe(mockCsrfToken)
  })

  it('3. should attach stored X-XSRF-TOKEN to subsequent mutating requests', async () => {
    const mockCsrfToken = 'backend-csrf-token-xyz-789'
    let postRequestConfig = null

    // Step A: Tumanggap ng CSRF token mula sa server
    http.defaults.adapter = async (config) => {
      return {
        data: { success: true },
        status: 200,
        statusText: 'OK',
        headers: { 'x-xsrf-token': mockCsrfToken },
        config
      }
    }
    await http.get('/api/v1/tutorials')

    // Step B: Magpadala ng POST request (hal. Tutorial creation)
    http.defaults.adapter = async (config) => {
      postRequestConfig = config
      return {
        data: { id: 1, title: 'New Tutorial' },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      }
    }
    await http.post('/api/v1/tutorials', { title: 'New Tutorial' })

    // Tiyaking naidagdag ang X-XSRF-TOKEN header sa outgoing POST request
    expect(postRequestConfig.headers['X-XSRF-TOKEN']).toBe(mockCsrfToken)
  })

  it('4. should capture CSRF token even if the response returns an HTTP error', async () => {
    const freshTokenOnError = 'error-response-token-999'

    http.defaults.adapter = async (config) => {
      const error = new Error('Request failed with status code 401')
      error.response = {
        data: { message: 'Unauthorized' },
        status: 401,
        statusText: 'Unauthorized',
        headers: {
          'x-xsrf-token': freshTokenOnError
        },
        config
      }
      throw error
    }

    try {
      await http.post('/api/v1/tutorials', {})
    } catch (err) {
      expect(err.response.status).toBe(401)
    }

    // Nahuli pa rin ang token kahit nag-reject ang API
    expect(sessionStorage.getItem('csrf_token')).toBe(freshTokenOnError)
  })

  it('5. should restore and use CSRF token from sessionStorage on hard reload', async () => {
    const persistedToken = 'persisted-storage-token-555'
    sessionStorage.setItem('csrf_token', persistedToken)

    let capturedConfig = null
    http.defaults.adapter = async (config) => {
      capturedConfig = config
      return {
        data: { success: true },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      }
    }

    await http.post('/api/v1/tutorials', { title: 'After Reload' })

    expect(capturedConfig.headers['X-XSRF-TOKEN']).toBe(persistedToken)
  })

  it('6. should fallback to document.cookie in local dev environment when sessionStorage is empty', async () => {
    document.cookie = 'XSRF-TOKEN=cookie-token-local-444; path=/'

    let capturedConfig = null
    http.defaults.adapter = async (config) => {
      capturedConfig = config
      return {
        data: { success: true },
        status: 200,
        statusText: 'OK',
        headers: {},
        config
      }
    }

    await http.post('/api/v1/tutorials', { title: 'Local Dev Post' })

    expect(capturedConfig.headers['X-XSRF-TOKEN']).toBe('cookie-token-local-444')
  })
})
