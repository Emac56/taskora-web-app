import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import router from './index'
import { useAuthStore } from '../stores/auth'

describe('router navigation guard', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    await router.push('/')
    await router.isReady()
  })

  it('redirects a logged-out visitor away from a protected route', async () => {
    await router.push({ name: 'admin-dashboard' })

    expect(router.currentRoute.value.name).toBe('admin-login')
    expect(router.currentRoute.value.query.redirect).toBe('/admin')
  })

  it('lets a logged-in admin reach a protected route', async () => {
    useAuthStore().user = { id: 1, name: 'Admin', role: 'ADMIN' }

    await router.push({ name: 'admin-dashboard' })

    expect(router.currentRoute.value.name).toBe('admin-dashboard')
  })

  it('bounces a logged-in admin away from the login page', async () => {
    useAuthStore().user = { id: 1, name: 'Admin', role: 'ADMIN' }

    await router.push({ name: 'admin-login' })

    expect(router.currentRoute.value.name).toBe('admin-dashboard')
  })

  it('lets a logged-out visitor reach the login page', async () => {
    await router.push({ name: 'admin-login' })

    expect(router.currentRoute.value.name).toBe('admin-login')
  })

  it('redirects a logged-in non-admin (CLIENT) away from a protected admin route', async () => {
    useAuthStore().user = { id: 2, name: 'Some Client', role: 'CLIENT' }

    await router.push({ name: 'admin-dashboard' })

    expect(router.currentRoute.value.name).toBe('tutorials-list')
  })

  it('redirects a logged-in non-admin (CLIENT) away from the admin login page too', async () => {
    useAuthStore().user = { id: 2, name: 'Some Client', role: 'CLIENT' }

    await router.push({ name: 'admin-login' })

    expect(router.currentRoute.value.name).toBe('tutorials-list')
  })

  it('still lets a logged-in admin reach the tutorial creation route', async () => {
    useAuthStore().user = { id: 1, name: 'Admin', role: 'ADMIN' }

    await router.push({ name: 'admin-tutorial-create' })

    expect(router.currentRoute.value.name).toBe('admin-tutorial-create')
  })
})

describe('router catch-all (404)', () => {
  beforeEach(async () => {
    setActivePinia(createPinia())
    sessionStorage.clear()
    await router.push('/')
    await router.isReady()
  })

  it('resolves an undefined top-level path to the not-found route', async () => {
    await router.push('/this-does-not-exist')

    expect(router.currentRoute.value.name).toBe('not-found')
  })

  it('resolves an undefined deep/nested path to the not-found route', async () => {
    await router.push('/admin/tutorials/does/not/exist')

    expect(router.currentRoute.value.name).toBe('not-found')
  })

  it('does not hijack a real route that happens to share a prefix', async () => {
    useAuthStore().user = { id: 1, name: 'Admin', role: 'ADMIN' }

    await router.push({ name: 'admin-tutorials-list' })

    expect(router.currentRoute.value.name).toBe('admin-tutorials-list')
  })
})
