import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from './auth'
import * as authApi from '../api/auth.api'

vi.mock('../api/auth.api')

const STORAGE_KEY = 'taskora_admin_user'

describe('auth store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    vi.clearAllMocks()
  })

  it('starts logged out when sessionStorage is empty', () => {
    const store = useAuthStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.isAdmin).toBe(false)
  })

  it('logs in, updates state, and persists to sessionStorage', async () => {
    const user = { id: 1, name: 'Admin', email: 'admin@taskora.dev', role: 'ADMIN' }
    authApi.login.mockResolvedValue(user)

    const store = useAuthStore()
    const result = await store.login('admin@taskora.dev', 'secret')

    expect(authApi.login).toHaveBeenCalledWith('admin@taskora.dev', 'secret')
    expect(result).toEqual(user)
    expect(store.isLoggedIn).toBe(true)
    expect(store.isAdmin).toBe(true)
    expect(JSON.parse(sessionStorage.getItem(STORAGE_KEY))).toEqual(user)
  })

  it('does not update state when login fails', async () => {
    authApi.login.mockRejectedValue(new Error('Invalid credentials'))
    const store = useAuthStore()

    await expect(store.login('admin@taskora.dev', 'wrong')).rejects.toThrow('Invalid credentials')
    expect(store.isLoggedIn).toBe(false)
    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull()
  })

  it('clears state on logout even if the API call fails', async () => {
    const store = useAuthStore()
    store.user = { id: 1, role: 'ADMIN' }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(store.user))
    authApi.logout.mockRejectedValue(new Error('Network error'))

    await expect(store.logout()).rejects.toThrow('Network error')
    expect(store.user).toBeNull()
    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull()
  })

  it('clearSession wipes state without calling the API', () => {
    const store = useAuthStore()
    store.user = { id: 1, role: 'ADMIN' }
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(store.user))

    store.clearSession()

    expect(store.user).toBeNull()
    expect(sessionStorage.getItem(STORAGE_KEY)).toBeNull()
    expect(authApi.logout).not.toHaveBeenCalled()
  })
})
