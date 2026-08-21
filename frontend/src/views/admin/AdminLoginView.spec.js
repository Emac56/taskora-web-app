import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import AdminLoginView from './AdminLoginView.vue'
import { useAuthStore } from '../../stores/auth'
import * as authApi from '../../api/auth.api'

vi.mock('../../api/auth.api')

const push = vi.fn()
let currentQuery = {}

vi.mock('vue-router', () => ({
  useRouter: () => ({ push }),
  useRoute: () => ({ query: currentQuery })
}))

function mountView() {
  return mount(AdminLoginView)
}

async function submitLogin(wrapper) {
  await wrapper.find('input[type="email"]').setValue('admin@taskora.dev')
  await wrapper.find('input[type="password"]').setValue('correct-password')
  await wrapper.find('form').trigger('submit')
  await flushPromises()
}

describe('AdminLoginView - post-login redirect validation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    currentQuery = {}
    authApi.login.mockResolvedValue({ id: 1, name: 'Admin', role: 'ADMIN' })
  })

  it('falls back to the dashboard when there is no redirect query param', async () => {
    const wrapper = mountView()

    await submitLogin(wrapper)

    expect(push).toHaveBeenCalledWith({ name: 'admin-dashboard' })
  })

  it('honors a redirect that points to a real internal admin path', async () => {
    currentQuery = { redirect: '/admin/tutorials' }
    const wrapper = mountView()

    await submitLogin(wrapper)

    expect(push).toHaveBeenCalledWith('/admin/tutorials')
  })

  it('falls back to the dashboard when redirect does not start with /admin', async () => {
    currentQuery = { redirect: '/tutorials/999' }
    const wrapper = mountView()

    await submitLogin(wrapper)

    expect(push).toHaveBeenCalledWith({ name: 'admin-dashboard' })
  })

  it('falls back to the dashboard for a protocol-relative redirect value', async () => {
    currentQuery = { redirect: '//evil.example.com' }
    const wrapper = mountView()

    await submitLogin(wrapper)

    expect(push).toHaveBeenCalledWith({ name: 'admin-dashboard' })
  })

  it('falls back to the dashboard when redirect is an array (duplicate query key)', async () => {
    currentQuery = { redirect: ['/admin', '/admin/tutorials'] }
    const wrapper = mountView()

    await submitLogin(wrapper)

    expect(push).toHaveBeenCalledWith({ name: 'admin-dashboard' })
  })

  it('does not navigate when login fails', async () => {
    authApi.login.mockRejectedValueOnce(new Error('Invalid email or password.'))
    currentQuery = { redirect: '/admin/tutorials' }
    const wrapper = mountView()

    await submitLogin(wrapper)

    expect(push).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('Invalid email or password.')
  })
})
