# Task 6 Auth UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Task 6 UI-only login page and application shell with Element Plus styling, responsive behavior, and render-level test coverage, without wiring the real auth request flow yet.

**Architecture:** Keep the existing HTTP client and auth store work in place, but limit this slice to presentation and routing entry points. The UI will use Element Plus as the structural base, a small shared token file for visual consistency, route-level rendering for `/login` and `/`, and component render tests that lock the approved shell landmarks in place.

**Tech Stack:** Vue 3, TypeScript, Vite, Vue Router, Pinia, Element Plus, `@element-plus/icons-vue`, Vitest, `@vue/test-utils`, jsdom

---

## File Structure Map

- `t-observer-web/package.json`
  Add the UI and test dependencies needed by this slice: `element-plus`, `@element-plus/icons-vue`, and `@vue/test-utils`.
- `t-observer-web/package-lock.json`
  Capture the resolved dependency graph after the install.
- `t-observer-web/src/main.ts`
  Register Element Plus globally and import the shared visual token stylesheet.
- `t-observer-web/src/App.vue`
  Replace the starter content with a route outlet only.
- `t-observer-web/src/router/index.ts`
  Add the UI preview routes for `LoginView` and `MainLayout` without auth guards.
- `t-observer-web/src/styles/ui-theme.css`
  Define the shared color, radius, spacing, and neutral surface tokens used by both Task 6 pages.
- `t-observer-web/src/views/login/LoginView.vue`
  Build the centered glassmorphism login card, brand backdrop, and form interaction styling.
- `t-observer-web/src/views/login/LoginView.spec.ts`
  Lock the login page title, fields, and primary action button in a render test.
- `t-observer-web/src/layouts/MainLayout.vue`
  Build the responsive sidebar/header shell, drawer behavior, breadcrumb skeleton, and user dropdown trigger.
- `t-observer-web/src/layouts/MainLayout.spec.ts`
  Lock the sidebar shell, breadcrumb region, and user menu trigger in a render test.

## Scope Guardrails

- This plan does not wire `src/api/auth.ts`, route guards, submit handlers, or real logout behavior.
- This plan keeps copy in ASCII English unless the user explicitly asks to switch the UI copy back to Chinese.
- This plan keeps the existing local `src/api/http.ts` and `src/stores/auth.ts` work untouched except where imports or route previews need to coexist with the new UI.

## Task 1: Add UI Dependencies and Write Failing Render Tests

**Files:**
- Modify: `t-observer-web/package.json`
- Modify: `t-observer-web/package-lock.json`
- Create: `t-observer-web/src/views/login/LoginView.spec.ts`
- Create: `t-observer-web/src/layouts/MainLayout.spec.ts`

- [ ] **Step 1: Add the UI dependencies needed by the new pages**

Update `t-observer-web/package.json` so the dependency sections include:

```json
{
  "dependencies": {
    "@element-plus/icons-vue": "^2.3.1",
    "axios": "^1.15.1",
    "element-plus": "^2.11.8",
    "pinia": "^3.0.4",
    "vue": "^3.5.31",
    "vue-router": "^5.0.4"
  },
  "devDependencies": {
    "@vue/test-utils": "^2.4.6",
    "jsdom": "^29.0.2",
    "vitest": "^4.1.4"
  }
}
```

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npm install
```

Expected: `package.json` and `package-lock.json` both change, and `element-plus`, `@element-plus/icons-vue`, and `@vue/test-utils` are present in the lockfile.

- [ ] **Step 2: Write the failing login view render test**

Create `t-observer-web/src/views/login/LoginView.spec.ts`:

```ts
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
    expect(wrapper.find('button').text()).toContain('Sign In')
  })
})
```

- [ ] **Step 3: Write the failing main layout render test**

Create `t-observer-web/src/layouts/MainLayout.spec.ts`:

```ts
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
```

- [ ] **Step 4: Run the new tests to confirm they fail before implementation**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/views/login/LoginView.spec.ts --environment jsdom
npx vitest run src/layouts/MainLayout.spec.ts --environment jsdom
```

Expected:

- `LoginView.spec.ts` fails because `LoginView.vue` does not exist yet.
- `MainLayout.spec.ts` fails because `MainLayout.vue` does not exist yet.

- [ ] **Step 5: Commit the test-first dependency setup**

```bash
git add t-observer-web/package.json t-observer-web/package-lock.json t-observer-web/src/views/login/LoginView.spec.ts t-observer-web/src/layouts/MainLayout.spec.ts
git commit -m "test: add task6 auth ui render coverage"
```

## Task 2: Build the Shared Theme, App Shell Entry, and Login View

**Files:**
- Create: `t-observer-web/src/styles/ui-theme.css`
- Modify: `t-observer-web/src/main.ts`
- Modify: `t-observer-web/src/App.vue`
- Modify: `t-observer-web/src/router/index.ts`
- Create: `t-observer-web/src/views/login/LoginView.vue`
- Test: `t-observer-web/src/views/login/LoginView.spec.ts`

- [ ] **Step 1: Add the shared UI tokens**

Create `t-observer-web/src/styles/ui-theme.css`:

```css
:root {
  --app-primary: #409eff;
  --app-primary-soft: #e8f3ff;
  --app-page-bg: #f5f7fa;
  --app-card-bg: rgba(255, 255, 255, 0.72);
  --app-surface: #ffffff;
  --app-border: rgba(15, 23, 42, 0.08);
  --app-text: #1f2937;
  --app-text-muted: #6b7280;
  --app-shadow-soft: 0 18px 40px rgba(64, 158, 255, 0.12);
  --app-shadow-card: 0 10px 24px rgba(15, 23, 42, 0.08);
  --app-radius: 8px;
  --app-space-1: 12px;
  --app-space-2: 16px;
  --app-space-3: 24px;
  --app-space-4: 32px;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

body {
  margin: 0;
  background: var(--app-page-bg);
  color: var(--app-text);
}

#app {
  min-height: 100vh;
}
```

- [ ] **Step 2: Register Element Plus and the shared theme**

Update `t-observer-web/src/main.ts`:

```ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './styles/ui-theme.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
```

- [ ] **Step 3: Replace the starter app markup and add the login preview route**

Update `t-observer-web/src/App.vue`:

```vue
<template>
  <RouterView />
</template>
```

Update `t-observer-web/src/router/index.ts`:

```ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: {
        title: 'Login',
      },
    },
    {
      path: '/',
      redirect: '/login',
    },
  ],
})

export default router
```

- [ ] **Step 4: Implement the login page UI**

Create `t-observer-web/src/views/login/LoginView.vue`:

```vue
<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import { reactive } from 'vue'

const form = reactive({
  username: '',
  password: '',
})
</script>

<template>
  <section class="login-page">
    <div class="login-page__orb login-page__orb--left" />
    <div class="login-page__orb login-page__orb--right" />

    <div class="login-card">
      <div class="login-card__brand">T-Observer</div>
      <h1 class="login-card__title">Local account sign in</h1>
      <p class="login-card__subtitle">
        Use your local username and password to enter the observation workspace.
      </p>

      <ElForm :model="form" label-position="top" class="login-form">
        <ElFormItem label="Username">
          <ElInput
            v-model="form.username"
            placeholder="Enter username"
            :prefix-icon="User"
            size="large"
          />
        </ElFormItem>

        <ElFormItem label="Password">
          <ElInput
            v-model="form.password"
            type="password"
            show-password
            placeholder="Enter password"
            :prefix-icon="Lock"
            size="large"
          />
        </ElFormItem>

        <ElButton type="primary" size="large" class="login-form__submit">
          Sign In
        </ElButton>
      </ElForm>
    </div>
  </section>
</template>

<style scoped>
.login-page {
  position: relative;
  display: grid;
  min-height: 100vh;
  place-items: center;
  overflow: hidden;
  padding: var(--app-space-3);
  background:
    radial-gradient(circle at top left, rgba(64, 158, 255, 0.3), transparent 36%),
    linear-gradient(135deg, #eef7ff 0%, #f8fbff 48%, #eef4fb 100%);
}

.login-page__orb {
  position: absolute;
  border-radius: 999px;
  filter: blur(0);
  opacity: 0.6;
  pointer-events: none;
}

.login-page__orb--left {
  top: 8%;
  left: 10%;
  width: 220px;
  height: 220px;
  background: rgba(64, 158, 255, 0.16);
}

.login-page__orb--right {
  right: 8%;
  bottom: 12%;
  width: 280px;
  height: 280px;
  background: rgba(120, 190, 255, 0.12);
}

.login-card {
  position: relative;
  width: min(100%, 420px);
  padding: 36px;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 16px;
  background: var(--app-card-bg);
  box-shadow: var(--app-shadow-soft);
  backdrop-filter: blur(14px);
}

.login-card__brand {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--app-primary);
}

.login-card__title {
  margin: 0 0 8px;
  font-size: 18px;
  line-height: 1.3;
}

.login-card__subtitle {
  margin: 0 0 24px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--app-text-muted);
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: var(--app-radius);
  box-shadow: 0 0 0 1px transparent inset;
  transition:
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px var(--app-primary) inset,
    0 0 0 4px rgba(64, 158, 255, 0.12);
}

.login-form__submit {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--app-radius);
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.2);
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    filter 0.18s ease;
}

.login-form__submit:hover {
  filter: brightness(1.02);
  box-shadow: 0 14px 24px rgba(64, 158, 255, 0.24);
}

.login-form__submit:active {
  transform: translateY(1px) scale(0.995);
  box-shadow: 0 8px 14px rgba(64, 158, 255, 0.18);
}

@media (max-width: 640px) {
  .login-page {
    padding: 20px;
  }

  .login-card {
    padding: 28px 22px;
  }
}
</style>
```

- [ ] **Step 5: Run the login test and the basic app checks**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/views/login/LoginView.spec.ts --environment jsdom
npm run type-check
```

Expected:

- `LoginView.spec.ts` passes.
- `vue-tsc` passes with the new Element Plus imports and route definitions.

- [ ] **Step 6: Commit the login UI slice**

```bash
git add t-observer-web/src/styles/ui-theme.css t-observer-web/src/main.ts t-observer-web/src/App.vue t-observer-web/src/router/index.ts t-observer-web/src/views/login/LoginView.vue
git commit -m "feat: add task6 login ui shell"
```

## Task 3: Build the Responsive Main Layout Shell and Complete Verification

**Files:**
- Create: `t-observer-web/src/layouts/MainLayout.vue`
- Modify: `t-observer-web/src/router/index.ts`
- Test: `t-observer-web/src/layouts/MainLayout.spec.ts`
- Verify: `t-observer-web/src/views/login/LoginView.spec.ts`

- [ ] **Step 1: Implement the responsive application shell**

Create `t-observer-web/src/layouts/MainLayout.vue`:

```vue
<script setup lang="ts">
import {
  ArrowDown,
  Expand,
  Fold,
  HomeFilled,
  Menu,
  Monitor,
  SwitchButton,
  UserFilled,
} from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const collapsed = ref(false)
const mobileOpen = ref(false)
const isMobile = ref(false)

const menuItems = [
  { index: 'overview', label: 'Workspace', icon: Monitor },
  { index: 'tasks', label: 'Tasks', icon: HomeFilled },
]

function syncViewport() {
  isMobile.value = window.innerWidth < 960
  if (!isMobile.value) {
    mobileOpen.value = false
  }
}

function toggleSidebar() {
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value
    return
  }
  collapsed.value = !collapsed.value
}

const sidebarWidth = computed(() => (collapsed.value ? '88px' : '248px'))

onMounted(() => {
  syncViewport()
  window.addEventListener('resize', syncViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', syncViewport)
})
</script>

<template>
  <div class="app-shell">
    <aside
      v-if="!isMobile"
      class="app-shell__sidebar"
      :style="{ width: sidebarWidth }"
      data-testid="app-sidebar"
    >
      <div class="app-shell__brand">
        <span class="app-shell__brand-mark">TO</span>
        <div v-show="!collapsed" class="app-shell__brand-copy">
          <strong>T-Observer</strong>
          <span>Teaching workspace</span>
        </div>
      </div>

      <ElMenu
        class="app-shell__menu"
        :collapse="collapsed"
        default-active="overview"
      >
        <ElMenuItem v-for="item in menuItems" :key="item.index" :index="item.index">
          <ElIcon><component :is="item.icon" /></ElIcon>
          <template #title>{{ item.label }}</template>
        </ElMenuItem>
      </ElMenu>
    </aside>

    <ElDrawer
      v-model="mobileOpen"
      direction="ltr"
      size="260px"
      :with-header="false"
      class="app-shell__drawer"
    >
      <aside class="app-shell__drawer-inner" data-testid="app-sidebar">
        <div class="app-shell__brand">
          <span class="app-shell__brand-mark">TO</span>
          <div class="app-shell__brand-copy">
            <strong>T-Observer</strong>
            <span>Teaching workspace</span>
          </div>
        </div>

        <ElMenu class="app-shell__menu" default-active="overview">
          <ElMenuItem v-for="item in menuItems" :key="item.index" :index="item.index">
            <ElIcon><component :is="item.icon" /></ElIcon>
            <template #title>{{ item.label }}</template>
          </ElMenuItem>
        </ElMenu>
      </aside>
    </ElDrawer>

    <div class="app-shell__main">
      <header class="app-shell__header">
        <div class="app-shell__header-left">
          <ElButton text class="app-shell__toggle" @click="toggleSidebar">
            <ElIcon>
              <component :is="isMobile ? Menu : collapsed ? Expand : Fold" />
            </ElIcon>
          </ElButton>

          <ElBreadcrumb data-testid="app-breadcrumbs" separator="/">
            <ElBreadcrumbItem>Home</ElBreadcrumbItem>
            <ElBreadcrumbItem>Workspace</ElBreadcrumbItem>
          </ElBreadcrumb>
        </div>

        <ElDropdown trigger="click">
          <button class="user-trigger" type="button" data-testid="user-menu-trigger">
            <span class="user-trigger__avatar">
              <ElIcon><UserFilled /></ElIcon>
            </span>
            <span class="user-trigger__copy">
              <strong>Leader Zhang</strong>
              <small>LEADER</small>
            </span>
            <ElIcon class="user-trigger__chevron"><ArrowDown /></ElIcon>
          </button>

          <template #dropdown>
            <ElDropdownMenu>
              <ElDropdownItem>
                <ElIcon><UserFilled /></ElIcon>
                Profile
              </ElDropdownItem>
              <ElDropdownItem divided>
                <ElIcon><SwitchButton /></ElIcon>
                Sign Out
              </ElDropdownItem>
            </ElDropdownMenu>
          </template>
        </ElDropdown>
      </header>

      <main class="app-shell__content">
        <section class="content-card">
          <h1>Workspace Preview</h1>
          <p>
            Task 6 shell preview for the upcoming member and leader pages.
          </p>
          <RouterView />
        </section>
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  min-height: 100vh;
  background: var(--app-page-bg);
}

.app-shell__sidebar,
.app-shell__drawer-inner {
  display: flex;
  flex-direction: column;
  gap: var(--app-space-3);
  padding: 20px 16px;
  background: linear-gradient(180deg, #f8fbff 0%, #f0f7ff 100%);
  box-shadow: inset -1px 0 0 rgba(15, 23, 42, 0.04);
  transition: width 0.24s ease;
}

.app-shell__brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.68);
}

.app-shell__brand-mark {
  display: inline-grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 12px;
  background: var(--app-primary);
  color: #fff;
  font-weight: 700;
}

.app-shell__brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.app-shell__brand-copy strong {
  font-size: 14px;
}

.app-shell__brand-copy span {
  font-size: 12px;
  color: var(--app-text-muted);
}

.app-shell__menu {
  border: none;
  background: transparent;
}

.app-shell__menu :deep(.el-menu-item) {
  margin-bottom: 8px;
  border-radius: var(--app-radius);
}

.app-shell__menu :deep(.el-menu-item.is-active) {
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

.app-shell__main {
  flex: 1;
  min-width: 0;
}

.app-shell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--app-space-2);
  padding: 20px 24px 12px;
}

.app-shell__header-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.app-shell__toggle {
  width: 40px;
  height: 40px;
  border-radius: var(--app-radius);
  background: rgba(255, 255, 255, 0.86);
  box-shadow: var(--app-shadow-card);
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border: none;
  border-radius: var(--app-radius);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--app-shadow-card);
  cursor: pointer;
}

.user-trigger__avatar {
  display: inline-grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

.user-trigger__copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.user-trigger__copy strong {
  font-size: 14px;
}

.user-trigger__copy small {
  font-size: 12px;
  color: var(--app-text-muted);
}

.app-shell__content {
  padding: 12px 24px 24px;
}

.content-card {
  min-height: calc(100vh - 108px);
  padding: 28px;
  border-radius: var(--app-radius);
  background: var(--app-surface);
  box-shadow: var(--app-shadow-card);
}

.content-card h1 {
  margin: 0 0 8px;
  font-size: 18px;
}

.content-card p {
  margin: 0;
  font-size: 14px;
  color: var(--app-text-muted);
}

@media (max-width: 959px) {
  .app-shell__header {
    padding: 16px 16px 10px;
  }

  .app-shell__content {
    padding: 10px 16px 16px;
  }

  .content-card {
    min-height: calc(100vh - 90px);
    padding: 22px;
  }

  .user-trigger__copy {
    display: none;
  }
}

@media (max-width: 640px) {
  .app-shell__header {
    align-items: flex-start;
  }

  .app-shell__header-left {
    flex-wrap: wrap;
  }
}
</style>
```

- [ ] **Step 2: Expose the main layout through the router**

Update `t-observer-web/src/router/index.ts`:

```ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'shell-home',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: {
        title: 'Workspace',
      },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: {
        title: 'Login',
      },
    },
  ],
})

export default router
```

- [ ] **Step 3: Run the main layout test to confirm the shell landmarks render**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/layouts/MainLayout.spec.ts --environment jsdom
```

Expected: PASS with the sidebar, breadcrumb container, and user menu trigger all rendered.

- [ ] **Step 4: Run the UI verification set**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/views/login/LoginView.spec.ts --environment jsdom
npx vitest run src/layouts/MainLayout.spec.ts --environment jsdom
npm run type-check
npm run build
```

Expected:

- Both render specs pass.
- `vue-tsc` passes.
- Vite build completes without route, style, or Element Plus integration errors.

- [ ] **Step 5: Commit the Task 6 UI shell**

```bash
git add t-observer-web/src/layouts/MainLayout.vue t-observer-web/src/layouts/MainLayout.spec.ts t-observer-web/src/router/index.ts
git commit -m "feat: add task6 main layout shell"
```

## Plan Self-Review

### Spec Coverage

- The login page visual requirements are covered in Task 2:
  - centered card
  - light blue gradient backdrop
  - glassmorphism surface
  - Element Plus inputs and button
  - focus and click states
- The main layout shell requirements are covered in Task 3:
  - sidebar layout
  - desktop collapse
  - mobile drawer
  - breadcrumb header
  - avatar dropdown
  - light gray content background with white content card
- The shared UI consistency requirements are covered in Task 2 through `src/styles/ui-theme.css`:
  - `#409EFF` brand color
  - `#f5f7fa` content background
  - `8px` component radius tokens
  - system sans-serif typography
  - larger spacing scale
- The requested verification level is covered by:
  - `LoginView.spec.ts`
  - `MainLayout.spec.ts`
  - `npm run type-check`
  - `npm run build`

### Placeholder Scan

- Removed auth API wiring, route guards, and business-page assumptions from the scope.
- Used exact file paths, exact commands, and concrete render assertions instead of vague “style it later” language.
- Kept each task isolated to one small deliverable set: test-first setup, login UI, then main shell.

### Type Consistency

- Route component paths match the existing alias setup in `tsconfig.app.json` and `vite.config.ts`.
- CSS token names are shared across both new UI files.
- The render test selectors (`app-sidebar`, `app-breadcrumbs`, `user-menu-trigger`) are defined directly in the planned component markup.
