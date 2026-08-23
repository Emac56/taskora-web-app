import { describe, it, expect, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import AdminDashboardView from './AdminDashboardView.vue'
import { getTutorialStats } from '../../api/tutorials.api'

vi.mock('../../api/tutorials.api', () => ({
  getTutorialStats: vi.fn()
}))

vi.mock('../../stores/auth', () => ({
  useAuthStore: () => ({ user: { name: 'Admin' } })
}))

describe('AdminDashboardView', () => {
  it('calls getTutorialStats exactly once (no N+1 requests)', async () => {
    getTutorialStats.mockResolvedValueOnce({
      totalTutorials: 5, publishedCount: 3, draftCount: 2, totalSteps: 17
    })

    mount(AdminDashboardView)
    await flushPromises()

    expect(getTutorialStats).toHaveBeenCalledTimes(1)
  })

  it('renders the returned stats into the StatCards', async () => {
    getTutorialStats.mockResolvedValueOnce({
      totalTutorials: 5, publishedCount: 3, draftCount: 2, totalSteps: 17
    })

    const wrapper = mount(AdminDashboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('5')
    expect(wrapper.text()).toContain('17')
  })

  it('shows an error message when the request fails', async () => {
    getTutorialStats.mockRejectedValueOnce(new Error('Network error'))

    const wrapper = mount(AdminDashboardView)
    await flushPromises()

    expect(wrapper.text()).toContain('Network error')
  })
})
