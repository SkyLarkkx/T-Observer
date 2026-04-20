import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

import MainLayout from './MainLayout.vue'

describe('MainLayout', () => {
  it('renders the sidebar shell, breadcrumb region, and user trigger', () => {
    const wrapper = mount(MainLayout, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: {
            template: '<a data-testid="router-link-stub"><slot /></a>',
          },
          RouterView: {
            template: '<div data-testid="layout-slot">Route content</div>',
          },
        },
      },
    })

    expect(wrapper.find('[data-testid="app-sidebar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="app-breadcrumbs"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="user-menu-trigger"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Leader Zhang')
  })
})
