import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import LoginForm from './LoginForm.vue'

describe('LoginForm accessibility', () => {
  it('exposes error message via role="alert"', () => {
    const wrapper = mount(LoginForm, { props: { errorMessage: 'Invalid email or password.' } })
    const alert = wrapper.find('[role="alert"]')
    expect(alert.exists()).toBe(true)
    expect(alert.text()).toBe('Invalid email or password.')
  })

  it('does not render alert region when there is no error', () => {
    const wrapper = mount(LoginForm, { props: { errorMessage: '' } })
    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })
})
