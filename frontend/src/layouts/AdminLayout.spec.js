import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { setActivePinia, createPinia } from 'pinia'
import AdminLayout from './AdminLayout.vue'

const push = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push }),
  RouterLink: {
    name: 'RouterLink',
    props: ['to'],
    template: '<a @click="$emit(\'click\')"><slot /></a>'
  }
}))

function mountLayout() {
  return mount(AdminLayout, {
    slots: { default: '<p>Page content</p>' }
  })
}

describe('AdminLayout - responsive sidebar', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
  })

  it('starts closed on mount (off-canvas by default)', () => {
    const wrapper = mountLayout()

    const aside = wrapper.get('aside')
    expect(aside.classes()).toContain('-translate-x-full')
    expect(wrapper.find('.fixed.inset-0.z-30').exists()).toBe(false)
  })

  it('opens the sidebar and shows the backdrop when the toggle button is clicked', async () => {
    const wrapper = mountLayout()

    await wrapper.get('button[aria-label="Toggle sidebar"]').trigger('click')

    const aside = wrapper.get('aside')
    expect(aside.classes()).toContain('translate-x-0')
    expect(wrapper.find('.fixed.inset-0.z-30').exists()).toBe(true)
  })

  it('closes the sidebar when the backdrop is clicked', async () => {
    const wrapper = mountLayout()
    await wrapper.get('button[aria-label="Toggle sidebar"]').trigger('click')

    await wrapper.get('.fixed.inset-0.z-30').trigger('click')

    expect(wrapper.get('aside').classes()).toContain('-translate-x-full')
    expect(wrapper.find('.fixed.inset-0.z-30').exists()).toBe(false)
  })

  it('closes the sidebar when a nav link is clicked', async () => {
    const wrapper = mountLayout()
    await wrapper.get('button[aria-label="Toggle sidebar"]').trigger('click')

    await wrapper.findAll('a')[1].trigger('click') // "Tutorials" link

    expect(wrapper.get('aside').classes()).toContain('-translate-x-full')
  })

  it('keeps the toggle button hidden on desktop widths via the md:hidden class', () => {
    const wrapper = mountLayout()

    expect(wrapper.get('button[aria-label="Toggle sidebar"]').classes()).toContain('md:hidden')
  })

  it('keeps the sidebar static on desktop via md:relative md:translate-x-0', () => {
    const wrapper = mountLayout()

    const aside = wrapper.get('aside')
    expect(aside.classes()).toContain('md:relative')
    expect(aside.classes()).toContain('md:translate-x-0')
  })
})
                        
