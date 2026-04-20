import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import LoginView from './LoginView.vue'

describe('LoginView', () => {
  it('renders the login title, fields, and primary action', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('T-Observer')
    expect(wrapper.text()).toContain('Local account sign in')
    expect(wrapper.find('input[placeholder="Enter username"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="Enter password"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="login-submit"]').text()).toContain('Sign In')
  })
})
