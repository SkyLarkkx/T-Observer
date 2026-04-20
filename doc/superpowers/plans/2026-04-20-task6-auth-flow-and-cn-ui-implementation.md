# Task 6 Auth Flow And Chinese UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the current frontend auth foundation into a real login flow that calls the backend, validates persisted tokens through `/api/auth/me`, redirects all authenticated users to `/overview`, and shows Chinese UI copy throughout the login page and shell.

**Architecture:** Keep the current Vite proxy, Axios client, Pinia auth store, and Vue Router shell structure. Add a typed auth API module, extend the auth store with session-validation and logout behavior, move route protection into a router factory that can be tested with memory history, and upgrade the existing login page and main shell from static preview state to store-backed Chinese UI.

**Tech Stack:** Vue 3, TypeScript, Vite, Pinia, Vue Router, Axios, Element Plus, Vitest, `@vue/test-utils`, jsdom

---

## File Structure Map

- `t-observer-web/src/types/auth.ts`
  Shared frontend auth request and response types plus role-code and role-label mappings.
- `t-observer-web/src/api/auth.ts`
  Login and current-user API wrappers that unwrap the backend `ApiResponse<T>` envelope.
- `t-observer-web/src/api/auth.spec.ts`
  Focused tests proving the auth API module calls the correct endpoints and unwraps payloads.
- `t-observer-web/src/stores/auth.ts`
  Persisted auth identity, in-memory validation flag, role-label helper, login acceptance, current-user acceptance, and logout behavior.
- `t-observer-web/src/stores/auth.spec.ts`
  Store-level persistence, current-user refresh, logout, and Chinese role-label coverage.
- `t-observer-web/src/router/index.ts`
  Router factory, protected shell routes, `/login` guest route, `/overview` redirect, placeholder child routes, and auth guard logic.
- `t-observer-web/src/router/index.spec.ts`
  Route guard coverage for anonymous access, valid persisted token restore, and invalid token fallback.
- `t-observer-web/src/views/login/LoginView.vue`
  Working Chinese login form with validation, loading state, backend submission, and navigation.
- `t-observer-web/src/views/login/LoginView.spec.ts`
  Chinese login render and submit behavior tests.
- `t-observer-web/src/layouts/MainLayout.vue`
  Chinese shell copy, store-backed user display, Chinese role label, and logout action.
- `t-observer-web/src/layouts/MainLayout.spec.ts`
  Shell render test updated to Chinese visible copy and real store-backed user identity.
- `t-observer-web/src/App.vue`
  Kept as route outlet only; no behavior change needed unless a test reveals otherwise.
- `t-observer-web/src/main.ts`
  Kept as the app bootstrap; no behavior change expected unless a test reveals otherwise.
- `t-observer-web/src/api/http.ts`
  Existing shared Axios client reused without changing proxy behavior.
- `t-observer-web/src/stores/counter.ts`
  Unused Vue starter store that should be removed once Task 6 auth flow is complete.

## Scope Guardrails

- Do not add backend logout behavior in this task.
- Do not add role-specific landing pages yet.
- Do not bypass `/api/auth/me` on refresh.
- Do not reintroduce English user-facing copy unless the user explicitly asks for it.
- Keep `/overview`, `/tasks`, and `/records` as lightweight placeholder child pages for now.

## Task 1: Add Typed Auth API Coverage

**Files:**
- Create: `t-observer-web/src/types/auth.ts`
- Create: `t-observer-web/src/api/auth.ts`
- Create: `t-observer-web/src/api/auth.spec.ts`

- [ ] **Step 1: Write the failing auth API tests**

Create `t-observer-web/src/api/auth.spec.ts`:

```ts
import { describe, expect, it, vi, afterEach } from 'vitest'

import { http } from './http'
import { fetchCurrentUser, login } from './auth'

vi.mock('./http', () => ({
  http: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

describe('auth api', () => {
  afterEach(() => {
    vi.clearAllMocks()
  })

  it('posts login credentials to /auth/login and unwraps the payload', async () => {
    vi.mocked(http.post).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          token: 'token-1',
          userId: 1,
          realName: '张老师',
          roleCode: 'LEADER',
        },
      },
    })

    const result = await login({
      username: 'leader01',
      password: '123456',
    })

    expect(http.post).toHaveBeenCalledWith('/auth/login', {
      username: 'leader01',
      password: '123456',
    })
    expect(result.realName).toBe('张老师')
    expect(result.roleCode).toBe('LEADER')
  })

  it('gets /auth/me and unwraps the current user payload', async () => {
    vi.mocked(http.get).mockResolvedValue({
      data: {
        code: 200,
        message: 'success',
        data: {
          userId: 2,
          username: 'member01',
          realName: '李老师',
          roleCode: 'MEMBER',
        },
      },
    })

    const result = await fetchCurrentUser()

    expect(http.get).toHaveBeenCalledWith('/auth/me')
    expect(result.username).toBe('member01')
    expect(result.realName).toBe('李老师')
  })
})
```

- [ ] **Step 2: Run the auth API test to verify it fails**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/api/auth.spec.ts --environment jsdom
```

Expected: FAIL because `./auth` and `../types/auth` do not exist yet.

- [ ] **Step 3: Add shared auth types and API wrappers**

Create `t-observer-web/src/types/auth.ts`:

```ts
export type RoleCode = 'LEADER' | 'MEMBER' | 'ADMIN'

export type LoginRequest = {
  username: string
  password: string
}

export type LoginResponse = {
  token: string
  userId: number
  realName: string
  roleCode: RoleCode
}

export type CurrentUserResponse = {
  userId: number
  username: string
  realName: string
  roleCode: RoleCode
}

export type ApiEnvelope<T> = {
  code: number
  message: string
  data: T
}
```

Create `t-observer-web/src/api/auth.ts`:

```ts
import { http } from './http'
import type {
  ApiEnvelope,
  CurrentUserResponse,
  LoginRequest,
  LoginResponse,
} from '@/types/auth'

export async function login(payload: LoginRequest) {
  const response = await http.post<ApiEnvelope<LoginResponse>>('/auth/login', payload)
  return response.data.data
}

export async function fetchCurrentUser() {
  const response = await http.get<ApiEnvelope<CurrentUserResponse>>('/auth/me')
  return response.data.data
}
```

- [ ] **Step 4: Run the auth API test to verify it passes**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/api/auth.spec.ts --environment jsdom
```

Expected: PASS with both endpoint tests green.

- [ ] **Step 5: Commit the typed auth API slice**

```bash
git add t-observer-web/src/types/auth.ts t-observer-web/src/api/auth.ts t-observer-web/src/api/auth.spec.ts
git commit -m "feat: add typed auth api client"
```

## Task 2: Extend the Auth Store for Validation, Logout, and Chinese Role Labels

**Files:**
- Modify: `t-observer-web/src/stores/auth.ts`
- Modify: `t-observer-web/src/stores/auth.spec.ts`

- [ ] **Step 1: Expand the auth store test first**

Update `t-observer-web/src/stores/auth.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { afterEach, describe, expect, it } from 'vitest'

import { useAuthStore } from './auth'

describe('auth store', () => {
  afterEach(() => {
    window.localStorage.clear()
  })

  it('stores login response in state and marks the session validated', () => {
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptLogin({
      token: 'test-token',
      roleCode: 'LEADER',
      realName: '张老师',
      userId: 1,
    })

    expect(store.token).toBe('test-token')
    expect(store.realName).toBe('张老师')
    expect(store.isValidated).toBe(true)
    expect(store.roleLabel).toBe('组长')
  })

  it('refreshes user identity from /me without replacing the token', () => {
    window.localStorage.setItem('t-observer-token', 'cached-token')
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptCurrentUser({
      userId: 2,
      username: 'member01',
      realName: '李老师',
      roleCode: 'MEMBER',
    })

    expect(store.token).toBe('cached-token')
    expect(store.realName).toBe('李老师')
    expect(store.roleCode).toBe('MEMBER')
    expect(store.roleLabel).toBe('教师')
    expect(store.isValidated).toBe(true)
  })

  it('clears state and localStorage on logout', () => {
    setActivePinia(createPinia())
    const store = useAuthStore()

    store.acceptLogin({
      token: 'test-token',
      roleCode: 'ADMIN',
      realName: '管理员',
      userId: 3,
    })

    store.logout()

    expect(store.token).toBe('')
    expect(store.roleCode).toBe('')
    expect(store.realName).toBe('')
    expect(store.userId).toBe(0)
    expect(store.isValidated).toBe(false)
    expect(window.localStorage.getItem('t-observer-token')).toBe(null)
  })
})
```

- [ ] **Step 2: Run the store test to verify it fails**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/stores/auth.spec.ts --environment jsdom
```

Expected: FAIL because `acceptCurrentUser`, `logout`, `isValidated`, or `roleLabel` do not exist yet.

- [ ] **Step 3: Implement the expanded auth store**

Update `t-observer-web/src/stores/auth.ts`:

```ts
import { defineStore } from 'pinia'
import type { CurrentUserResponse, LoginResponse, RoleCode } from '@/types/auth'

type AuthState = {
  token: string
  roleCode: RoleCode | ''
  realName: string
  userId: number
  isValidated: boolean
}

const STORAGE_KEYS = {
  token: 't-observer-token',
  roleCode: 't-observer-role',
  realName: 't-observer-name',
  userId: 't-observer-user-id',
} as const

const ROLE_LABELS: Record<RoleCode, string> = {
  LEADER: '组长',
  MEMBER: '教师',
  ADMIN: '管理员',
}

function readAuthState(): AuthState {
  return {
    token: window.localStorage.getItem(STORAGE_KEYS.token) ?? '',
    roleCode: (window.localStorage.getItem(STORAGE_KEYS.roleCode) as RoleCode | null) ?? '',
    realName: window.localStorage.getItem(STORAGE_KEYS.realName) ?? '',
    userId: Number(window.localStorage.getItem(STORAGE_KEYS.userId) ?? 0),
    isValidated: false,
  }
}

function persistAuthState(state: Pick<AuthState, 'token' | 'roleCode' | 'realName' | 'userId'>) {
  if (state.token) {
    window.localStorage.setItem(STORAGE_KEYS.token, state.token)
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.token)
  }

  if (state.roleCode) {
    window.localStorage.setItem(STORAGE_KEYS.roleCode, state.roleCode)
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.roleCode)
  }

  if (state.realName) {
    window.localStorage.setItem(STORAGE_KEYS.realName, state.realName)
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.realName)
  }

  if (state.userId) {
    window.localStorage.setItem(STORAGE_KEYS.userId, String(state.userId))
  } else {
    window.localStorage.removeItem(STORAGE_KEYS.userId)
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => readAuthState(),
  getters: {
    roleLabel(state) {
      return state.roleCode ? ROLE_LABELS[state.roleCode] : ''
    },
    isAuthenticated(state) {
      return Boolean(state.token)
    },
  },
  actions: {
    acceptLogin(payload: LoginResponse) {
      this.token = payload.token
      this.roleCode = payload.roleCode
      this.realName = payload.realName
      this.userId = payload.userId
      this.isValidated = true
      persistAuthState(this.$state)
    },
    acceptCurrentUser(payload: CurrentUserResponse) {
      this.roleCode = payload.roleCode
      this.realName = payload.realName
      this.userId = payload.userId
      this.isValidated = true
      persistAuthState(this.$state)
    },
    logout() {
      this.token = ''
      this.roleCode = ''
      this.realName = ''
      this.userId = 0
      this.isValidated = false
      persistAuthState(this.$state)
    },
  },
})
```

- [ ] **Step 4: Run the store test to verify it passes**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/stores/auth.spec.ts --environment jsdom
```

Expected: PASS with login, current-user refresh, logout, and Chinese role label coverage all green.

- [ ] **Step 5: Commit the auth store slice**

```bash
git add t-observer-web/src/stores/auth.ts t-observer-web/src/stores/auth.spec.ts
git commit -m "feat: extend auth store for session validation"
```

## Task 3: Add Protected Routing and Session Restore Through `/api/auth/me`

**Files:**
- Modify: `t-observer-web/src/router/index.ts`
- Create: `t-observer-web/src/router/index.spec.ts`

- [ ] **Step 1: Write the failing router guard tests**

Create `t-observer-web/src/router/index.spec.ts`:

```ts
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory } from 'vue-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { fetchCurrentUser } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { createAppRouter } from './index'

vi.mock('@/api/auth', () => ({
  fetchCurrentUser: vi.fn(),
}))

describe('app router auth guard', () => {
  afterEach(() => {
    window.localStorage.clear()
    vi.clearAllMocks()
  })

  it('redirects anonymous users from protected routes to /login', async () => {
    setActivePinia(createPinia())
    const router = createAppRouter(createMemoryHistory())

    await router.push('/overview')
    await router.isReady()

    expect(router.currentRoute.value.fullPath).toBe('/login')
  })

  it('restores a persisted session through /api/auth/me before entering /overview', async () => {
    window.localStorage.setItem('t-observer-token', 'cached-token')
    setActivePinia(createPinia())
    const router = createAppRouter(createMemoryHistory())

    vi.mocked(fetchCurrentUser).mockResolvedValue({
      userId: 1,
      username: 'leader01',
      realName: '张老师',
      roleCode: 'LEADER',
    })

    await router.push('/overview')
    await router.isReady()

    const store = useAuthStore()
    expect(fetchCurrentUser).toHaveBeenCalledTimes(1)
    expect(router.currentRoute.value.fullPath).toBe('/overview')
    expect(store.realName).toBe('张老师')
    expect(store.isValidated).toBe(true)
  })

  it('clears auth and falls back to /login when /api/auth/me fails', async () => {
    window.localStorage.setItem('t-observer-token', 'expired-token')
    setActivePinia(createPinia())
    const router = createAppRouter(createMemoryHistory())

    vi.mocked(fetchCurrentUser).mockRejectedValue(new Error('401'))

    await router.push('/tasks')
    await router.isReady()

    const store = useAuthStore()
    expect(router.currentRoute.value.fullPath).toBe('/login')
    expect(store.token).toBe('')
    expect(store.isValidated).toBe(false)
  })
})
```

- [ ] **Step 2: Run the router test to verify it fails**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/router/index.spec.ts --environment jsdom
```

Expected: FAIL because `createAppRouter` is not exported and the current router has no auth guard behavior.

- [ ] **Step 3: Implement the router factory, placeholders, and guard**

Update `t-observer-web/src/router/index.ts`:

```ts
import { defineComponent, h } from 'vue'
import {
  createRouter,
  createWebHistory,
  type RouterHistory,
  type RouteRecordRaw,
} from 'vue-router'

import { fetchCurrentUser } from '@/api/auth'
import MainLayout from '@/layouts/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/login/LoginView.vue'

const createPreviewPage = (title: string, description: string) =>
  defineComponent({
    name: `${title}Preview`,
    setup() {
      return () =>
        h('div', { class: 'route-preview' }, [
          h('h1', { class: 'route-preview__title' }, title),
          h('p', { class: 'route-preview__description' }, description),
        ])
    },
  })

const OverviewPreview = createPreviewPage('概览', '这是当前主布局的占位预览页。')
const TasksPreview = createPreviewPage('任务', '任务页面尚未接入，本页用于保持主布局内容完整。')
const RecordsPreview = createPreviewPage('记录', '记录页面尚未接入，本页用于保持主布局内容完整。')

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: { name: 'dashboard-overview' } },
      {
        path: 'overview',
        name: 'dashboard-overview',
        component: OverviewPreview,
        meta: { requiresAuth: true },
      },
      {
        path: 'tasks',
        name: 'dashboard-tasks',
        component: TasksPreview,
        meta: { requiresAuth: true },
      },
      {
        path: 'records',
        name: 'dashboard-records',
        component: RecordsPreview,
        meta: { requiresAuth: true },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true },
  },
]

export function createAppRouter(
  history: RouterHistory = createWebHistory(import.meta.env.BASE_URL),
) {
  const router = createRouter({
    history,
    routes,
  })

  router.beforeEach(async (to) => {
    const authStore = useAuthStore()
    const hasToken = Boolean(authStore.token)
    const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)
    const guestOnly = to.matched.some((record) => record.meta.guestOnly)

    if (!hasToken) {
      return requiresAuth ? { name: 'login' } : true
    }

    if (!authStore.isValidated) {
      try {
        const currentUser = await fetchCurrentUser()
        authStore.acceptCurrentUser(currentUser)
      } catch {
        authStore.logout()
        return guestOnly ? true : { name: 'login' }
      }
    }

    if (guestOnly) {
      return { name: 'dashboard-overview' }
    }

    return true
  })

  return router
}

const router = createAppRouter()

export default router
```

- [ ] **Step 4: Run the router test to verify it passes**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/router/index.spec.ts --environment jsdom
```

Expected: PASS with anonymous redirect, valid session restore, and invalid token fallback all green.

- [ ] **Step 5: Commit the guarded router slice**

```bash
git add t-observer-web/src/router/index.ts t-observer-web/src/router/index.spec.ts
git commit -m "feat: protect shell routes with auth guard"
```

## Task 4: Wire the Login Page and Main Layout Into the Real Auth Flow With Chinese UI

**Files:**
- Modify: `t-observer-web/src/views/login/LoginView.vue`
- Modify: `t-observer-web/src/views/login/LoginView.spec.ts`
- Modify: `t-observer-web/src/layouts/MainLayout.vue`
- Modify: `t-observer-web/src/layouts/MainLayout.spec.ts`
- Modify: `t-observer-web/src/App.vue`
- Delete: `t-observer-web/src/stores/counter.ts`

- [ ] **Step 1: Expand the login and shell render tests first**

Update `t-observer-web/src/views/login/LoginView.spec.ts`:

```ts
import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it, vi, beforeEach } from 'vitest'
import { createRouter, createMemoryHistory } from 'vue-router'

import { login } from '@/api/auth'
import LoginView from './LoginView.vue'

vi.mock('@/api/auth', () => ({
  login: vi.fn(),
}))

describe('LoginView', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('renders Chinese login copy and fields', () => {
    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus],
      },
    })

    expect(wrapper.text()).toContain('本地账号登录')
    expect(wrapper.find('input[placeholder="请输入用户名"]').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="请输入密码"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="login-submit"]').text()).toContain('登录')
  })

  it('submits credentials, stores login state, and jumps to /overview', async () => {
    vi.mocked(login).mockResolvedValue({
      token: 'token-1',
      userId: 1,
      realName: '张老师',
      roleCode: 'LEADER',
    })

    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/login', component: LoginView },
        { path: '/overview', component: { template: '<div>overview</div>' } },
      ],
    })

    const wrapper = mount(LoginView, {
      global: {
        plugins: [ElementPlus, createPinia(), router],
      },
    })

    await wrapper.find('input[placeholder="请输入用户名"]').setValue('leader01')
    await wrapper.find('input[placeholder="请输入密码"]').setValue('123456')
    await wrapper.find('[data-testid="login-submit"]').trigger('click')
    await Promise.resolve()
    await router.isReady()

    expect(login).toHaveBeenCalledWith({
      username: 'leader01',
      password: '123456',
    })
    expect(router.currentRoute.value.fullPath).toBe('/overview')
  })
})
```

Update `t-observer-web/src/layouts/MainLayout.spec.ts`:

```ts
import ElementPlus from 'element-plus'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { describe, expect, it } from 'vitest'

import { useAuthStore } from '@/stores/auth'
import MainLayout from './MainLayout.vue'

describe('MainLayout', () => {
  it('renders the sidebar shell with Chinese user identity', () => {
    setActivePinia(createPinia())
    const authStore = useAuthStore()
    authStore.acceptLogin({
      token: 'token-1',
      userId: 1,
      realName: '张老师',
      roleCode: 'LEADER',
    })

    const wrapper = mount(MainLayout, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          RouterLink: {
            template: '<a><slot /></a>',
          },
          RouterView: {
            template: '<div data-testid="layout-slot">路由内容</div>',
          },
        },
      },
    })

    expect(wrapper.find('[data-testid="app-sidebar"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="app-breadcrumbs"]').exists()).toBe(true)
    expect(wrapper.find('[data-testid="user-menu-trigger"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('张老师')
    expect(wrapper.text()).toContain('组长')
  })
})
```

- [ ] **Step 2: Run the view tests to verify they fail**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/views/login/LoginView.spec.ts --environment jsdom
npx vitest run src/layouts/MainLayout.spec.ts --environment jsdom
```

Expected: FAIL because the current views still render English static copy and do not submit the auth flow or read the auth store.

- [ ] **Step 3: Implement the working Chinese login page, Chinese shell, and cleanup**

Update `t-observer-web/src/views/login/LoginView.vue`:

```vue
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref()
const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submitLogin() {
  await formRef.value?.validate()
  loading.value = true

  try {
    const payload = await login({
      username: form.username,
      password: form.password,
    })

    authStore.acceptLogin(payload)
    await router.push('/overview')
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名或密码')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card" aria-labelledby="login-title">
      <div class="login-copy">
        <p class="login-eyebrow">T-Observer</p>
        <h1 id="login-title">本地账号登录</h1>
        <p class="login-subtitle">使用本地账号进入听评课工作台</p>
        <p class="login-helper">登录后将进入系统概览页，后续功能页面将继续接入。</p>
      </div>

      <el-form ref="formRef" class="login-form" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" autocomplete="username" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            show-password
            @keyup.enter="submitLogin"
          />
        </el-form-item>

        <el-button
          class="login-submit"
          type="primary"
          native-type="button"
          :loading="loading"
          data-testid="login-submit"
          @click="submitLogin"
        >
          登录
        </el-button>
      </el-form>
    </section>
  </main>
</template>
```

Update `t-observer-web/src/layouts/MainLayout.vue`:

```vue
<script setup lang="ts">
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const desktopCollapsed = ref(false)
const isMobile = ref(false)
const mobileDrawerOpen = ref(false)
const desktopSidebarId = 'app-sidebar-panel-desktop'
const mobileSidebarId = 'app-sidebar-panel-mobile'

const navItems = [
  { name: 'dashboard-overview', label: '概览', to: { name: 'dashboard-overview' } },
  { name: 'dashboard-tasks', label: '任务', to: { name: 'dashboard-tasks' } },
  { name: 'dashboard-records', label: '记录', to: { name: 'dashboard-records' } },
] as const

const updateViewport = () => {
  isMobile.value = window.innerWidth < 992
  if (!isMobile.value) {
    mobileDrawerOpen.value = false
  }
}

onMounted(() => {
  updateViewport()
  window.addEventListener('resize', updateViewport)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateViewport)
})

const sidebarCollapsed = computed(() => (!isMobile.value ? desktopCollapsed.value : false))
const sidebarExpanded = computed(() => (isMobile.value ? mobileDrawerOpen.value : !desktopCollapsed.value))
const sidebarControlsId = computed(() => (isMobile.value ? mobileSidebarId : desktopSidebarId))
const sidebarWidth = computed(() => (sidebarCollapsed.value ? '88px' : '244px'))
const currentRouteName = computed(() => String(route.name ?? ''))
const activeNavItem = computed(() => navItems.find((item) => item.name === currentRouteName.value) ?? navItems[0])

function toggleSidebar() {
  if (isMobile.value) {
    mobileDrawerOpen.value = !mobileDrawerOpen.value
    return
  }
  desktopCollapsed.value = !desktopCollapsed.value
}

function handleNavClick() {
  if (isMobile.value) {
    mobileDrawerOpen.value = false
  }
}

function isNavActive(name: string) {
  return currentRouteName.value === name
}

async function handleLogout() {
  authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <el-container class="main-layout">
    <el-drawer v-model="mobileDrawerOpen" class="main-layout__drawer" direction="ltr" size="264px" :with-header="false" :append-to-body="true">
      <aside :id="mobileSidebarId" class="sidebar sidebar--drawer" data-testid="app-sidebar">
        <div class="sidebar__brand">
          <div class="sidebar__brand-mark">T</div>
          <div class="sidebar__brand-copy">
            <strong>T-Observer</strong>
            <span>听评课工作台</span>
          </div>
        </div>

        <nav class="sidebar__nav" aria-label="Primary">
          <RouterLink
            v-for="item in navItems"
            :key="item.name"
            :to="item.to"
            :class="['sidebar__nav-item', { 'sidebar__nav-item--active': isNavActive(item.name) }]"
            :aria-current="isNavActive(item.name) ? 'page' : undefined"
            @click="handleNavClick"
          >
            {{ item.label }}
          </RouterLink>
        </nav>
      </aside>
    </el-drawer>

    <el-aside v-if="!isMobile" :id="desktopSidebarId" class="sidebar" data-testid="app-sidebar" :style="{ width: sidebarWidth }">
      <div class="sidebar__brand">
        <div class="sidebar__brand-mark">T</div>
        <transition name="fade-slide">
          <div v-if="!sidebarCollapsed" class="sidebar__brand-copy">
            <strong>T-Observer</strong>
            <span>听评课工作台</span>
          </div>
        </transition>
      </div>

      <nav class="sidebar__nav" aria-label="Primary">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="item.to"
          :class="['sidebar__nav-item', { 'sidebar__nav-item--active': isNavActive(item.name) }]"
          :aria-current="isNavActive(item.name) ? 'page' : undefined"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </el-aside>

    <el-container class="main-layout__content-shell">
      <el-header class="main-layout__header">
        <div class="main-layout__header-left">
          <el-button
            class="main-layout__toggle"
            text
            :aria-controls="sidebarControlsId"
            :aria-expanded="sidebarExpanded"
            :aria-label="sidebarExpanded ? '收起侧边栏' : '展开侧边栏'"
            @click="toggleSidebar"
          >
            <el-icon aria-hidden="true">
              <component :is="isMobile ? Expand : Fold" />
            </el-icon>
          </el-button>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item data-testid="app-breadcrumbs">工作台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ activeNavItem.label }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-dropdown trigger="click">
          <button class="user-trigger" data-testid="user-menu-trigger" type="button" aria-haspopup="menu" :aria-label="`${authStore.realName} 用户菜单`">
            <el-avatar :size="32">{{ authStore.realName.slice(0, 1) || '用' }}</el-avatar>
            <span class="user-trigger__name">{{ authStore.realName }}</span>
            <span class="user-trigger__role">{{ authStore.roleLabel }}</span>
            <el-icon class="user-trigger__chevron" aria-hidden="true">
              <ArrowDown />
            </el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人信息</el-dropdown-item>
              <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main-layout__main">
        <section class="main-layout__card">
          <RouterView />
        </section>
      </el-main>
    </el-container>
  </el-container>
</template>
```

Update `t-observer-web/src/App.vue`:

```vue
<template>
  <RouterView />
</template>
```

Delete the unused starter store:

```bash
git rm t-observer-web/src/stores/counter.ts
```

- [ ] **Step 4: Run the focused view checks and full frontend verification**

Run:

```bash
cd D:\JavaProjects\T-Observer\.worktrees\task6-frontend-foundation\t-observer-web
npx vitest run src/api/auth.spec.ts --environment jsdom
npx vitest run src/stores/auth.spec.ts --environment jsdom
npx vitest run src/router/index.spec.ts --environment jsdom
npx vitest run src/views/login/LoginView.spec.ts --environment jsdom
npx vitest run src/layouts/MainLayout.spec.ts --environment jsdom
npm run type-check
npm run build
```

Expected:

- all five focused Vitest specs pass
- `vue-tsc` passes
- Vite build succeeds

- [ ] **Step 5: Commit the real login flow and Chinese UI**

```bash
git add t-observer-web/src/api/auth.ts t-observer-web/src/api/auth.spec.ts t-observer-web/src/types/auth.ts t-observer-web/src/stores/auth.ts t-observer-web/src/stores/auth.spec.ts t-observer-web/src/router/index.ts t-observer-web/src/router/index.spec.ts t-observer-web/src/views/login/LoginView.vue t-observer-web/src/views/login/LoginView.spec.ts t-observer-web/src/layouts/MainLayout.vue t-observer-web/src/layouts/MainLayout.spec.ts t-observer-web/src/App.vue
git commit -m "feat: wire real auth flow into frontend shell"
```

## Plan Self-Review

### Spec Coverage

- Real login against `/api/auth/login` is covered by Task 1 and Task 4.
- Session restore through `/api/auth/me` is covered by Task 3.
- Protected shell routing and guest login routing are covered by Task 3.
- Chinese role mapping and Chinese user display are covered by Task 2 and Task 4.
- Chinese UI copy across login page, shell, and placeholder child routes is covered by Task 3 and Task 4.
- Logout behavior is covered by Task 2 and Task 4.

### Placeholder Scan

- Replaced vague auth tasks with exact file paths, test cases, endpoint calls, and route names.
- Included concrete route names, Chinese role labels, and exact commands.
- Kept the scope constrained to existing placeholder shell routes rather than inventing role-specific business pages.

### Type Consistency

- Backend-auth response fields match the current Java `LoginResponse` and `CurrentUserVo` classes:
  - `token`
  - `userId`
  - `realName`
  - `roleCode`
  - `username` on `/me`
- Frontend route names are consistent across nav items, guards, and router tests:
  - `dashboard-overview`
  - `dashboard-tasks`
  - `dashboard-records`
- The Chinese role label mapping is defined once in the store and reused by the shell.
