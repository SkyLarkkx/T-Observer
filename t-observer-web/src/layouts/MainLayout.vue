<script setup lang="ts">
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterView, useRoute, useRouter, type RouteLocationRaw } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

type BreadcrumbItem = {
  label: string
  to?: RouteLocationRaw
  testId?: string
}

type NavItem = {
  names: string[]
  label: string
  to: RouteLocationRaw
  testId: string
}

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const desktopCollapsed = ref(false)
const isMobile = ref(false)
const mobileDrawerOpen = ref(false)
const desktopSidebarId = 'app-sidebar-panel-desktop'
const mobileSidebarId = 'app-sidebar-panel-mobile'

const taskRouteTarget = computed<RouteLocationRaw>(() => {
  if (authStore.roleCode === 'LEADER') {
    return { name: 'leader-task-manage' }
  }

  if (authStore.roleCode === 'MEMBER') {
    return { name: 'member-task-list' }
  }

  return { name: 'dashboard-tasks' }
})

const baseNavItems = computed<NavItem[]>(() => [
  {
    names: ['dashboard-overview'],
    label: '概览',
    to: { name: 'dashboard-overview' as const },
    testId: 'nav-overview',
  },
  {
    names: ['dashboard-tasks', 'member-task-list', 'member-record-form', 'leader-task-manage'],
    label: '任务',
    to: taskRouteTarget.value,
    testId: 'nav-tasks',
  },
])

const reviewRouteTarget = computed<RouteLocationRaw>(() => ({
  name: 'leader-review-form',
  params: { recordId: 104 },
}))

const navItems = computed<NavItem[]>(() => {
  if (authStore.roleCode !== 'LEADER') {
    return baseNavItems.value
  }

  return [
    ...baseNavItems.value,
    {
      names: ['leader-review-form'],
      label: '评审',
      to: reviewRouteTarget.value,
      testId: 'nav-leader-reviews',
    },
    {
      names: ['leader-analytics'],
      label: '分析',
      to: { name: 'leader-analytics' as const },
      testId: 'nav-leader-analytics',
    },
  ]
})

const breadcrumbItems = computed<BreadcrumbItem[]>(() => {
  switch (route.name) {
    case 'member-record-form':
      return [
        {
          label: '任务',
          to: { name: 'member-task-list' },
          testId: 'breadcrumb-link-member-task-list',
        },
        { label: '记录填写' },
      ]
    case 'leader-review-form':
      return [
        {
          label: '任务',
          to: { name: 'leader-task-manage' },
          testId: 'breadcrumb-link-leader-task-manage',
        },
        { label: '评审' },
      ]
    case 'leader-analytics':
      return [{ label: '分析' }]
    case 'member-task-list':
    case 'leader-task-manage':
    case 'dashboard-tasks':
      return [{ label: '任务' }]
    case 'dashboard-overview':
    default:
      return [{ label: '概览' }]
  }
})

const previousBreadcrumb = computed(() => {
  if (breadcrumbItems.value.length < 2) {
    return null
  }

  return breadcrumbItems.value[breadcrumbItems.value.length - 2] ?? null
})

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
const sidebarExpanded = computed(() =>
  isMobile.value ? mobileDrawerOpen.value : !desktopCollapsed.value,
)
const sidebarControlsId = computed(() => (isMobile.value ? mobileSidebarId : desktopSidebarId))
const sidebarWidth = computed(() => (sidebarCollapsed.value ? '88px' : '244px'))
const currentRouteName = computed(() => String(route.name ?? ''))

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

function isNavActive(names: string[]) {
  return names.includes(currentRouteName.value)
}

async function navigateByBreadcrumb(target?: RouteLocationRaw) {
  if (!target) {
    return
  }

  await router.push(target)
}

async function navigateByNav(target?: RouteLocationRaw) {
  if (!target) {
    return
  }

  await router.push(target)
  handleNavClick()
}

async function goBackByBreadcrumb() {
  if (previousBreadcrumb.value?.to) {
    await navigateByBreadcrumb(previousBreadcrumb.value.to)
    return
  }

  await router.back()
}

async function handleLogout() {
  authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <el-container class="main-layout">
    <el-drawer
      v-model="mobileDrawerOpen"
      class="main-layout__drawer"
      direction="ltr"
      size="264px"
      :with-header="false"
      :append-to-body="true"
    >
      <aside :id="mobileSidebarId" class="sidebar sidebar--drawer" data-testid="app-sidebar">
        <div class="sidebar__brand">
          <div class="sidebar__brand-mark">T</div>
          <div class="sidebar__brand-copy">
            <strong>T-Observer</strong>
            <span>听评课工作台</span>
          </div>
        </div>

        <nav class="sidebar__nav" aria-label="Primary">
          <button
            v-for="item in navItems"
            :key="item.label"
            type="button"
            :class="['sidebar__nav-item', { 'sidebar__nav-item--active': isNavActive(item.names) }]"
            :data-testid="item.testId"
            :aria-current="isNavActive(item.names) ? 'page' : undefined"
            @click="navigateByNav(item.to)"
          >
            {{ item.label }}
          </button>
        </nav>
      </aside>
    </el-drawer>

    <el-aside
      v-if="!isMobile"
      :id="desktopSidebarId"
      class="sidebar"
      data-testid="app-sidebar"
      :style="{ width: sidebarWidth }"
    >
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
        <button
          v-for="item in navItems"
          :key="item.label"
          type="button"
          :class="['sidebar__nav-item', { 'sidebar__nav-item--active': isNavActive(item.names) }]"
          :data-testid="item.testId"
          :aria-current="isNavActive(item.names) ? 'page' : undefined"
          @click="navigateByNav(item.to)"
        >
          {{ item.label }}
        </button>
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

          <div class="main-layout__breadcrumbs">
            <el-button
              v-if="previousBreadcrumb"
              text
              circle
              data-testid="breadcrumb-back"
              aria-label="返回上一级"
              @click="goBackByBreadcrumb"
            >
              ←
            </el-button>

            <el-breadcrumb separator="/">
              <el-breadcrumb-item data-testid="app-breadcrumbs">工作台</el-breadcrumb-item>
              <el-breadcrumb-item
                v-for="item in breadcrumbItems"
                :key="item.label"
              >
                <button
                  v-if="item.to"
                  type="button"
                  class="breadcrumb-link"
                  :data-testid="item.testId"
                  @click="navigateByBreadcrumb(item.to)"
                >
                  {{ item.label }}
                </button>
                <span v-else>{{ item.label }}</span>
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>

        <el-dropdown trigger="click">
          <button
            class="user-trigger"
            data-testid="user-menu-trigger"
            type="button"
            aria-haspopup="menu"
            :aria-label="`${authStore.realName} 用户菜单`"
          >
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

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f5f7fa;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px 16px;
  background: #fff;
  border-right: 1px solid rgba(24, 38, 60, 0.08);
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.04);
  overflow: hidden;
}

.sidebar--drawer {
  width: 100%;
  border-right: none;
  box-shadow: none;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar__brand-mark {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: linear-gradient(135deg, #1f6feb, #59a6ff);
  color: #fff;
  font-weight: 700;
  letter-spacing: 0.02em;
  box-shadow: 0 10px 20px rgba(31, 111, 235, 0.24);
}

.sidebar__brand-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.sidebar__brand-copy strong,
.sidebar__brand-copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar__brand-copy span {
  font-size: 12px;
  color: #6b7280;
}

.sidebar__nav {
  display: grid;
  gap: 8px;
}

.sidebar__nav-item {
  padding: 12px 14px;
  border-radius: 12px;
  color: #334155;
  text-decoration: none;
  background: #f8fafc;
  border: 1px solid transparent;
  transition:
    background 0.18s ease,
    color 0.18s ease,
    border-color 0.18s ease;
}

.sidebar__nav-item:hover {
  color: #1f6feb;
  background: #eff6ff;
}

.sidebar__nav-item--active {
  color: #1f6feb;
  background: #eff6ff;
  border-color: rgba(31, 111, 235, 0.12);
}

.main-layout__content-shell {
  min-width: 0;
  background: #f5f7fa;
}

.main-layout__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: auto;
  padding: 20px 24px;
  background: transparent;
}

.main-layout__header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.main-layout__toggle {
  color: #334155;
}

.main-layout__breadcrumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.breadcrumb-link {
  border: none;
  padding: 0;
  background: transparent;
  color: var(--ui-color-primary);
  cursor: pointer;
  font: inherit;
}

.user-trigger {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border: 1px solid rgba(24, 38, 60, 0.08);
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 10px 24px rgba(16, 24, 40, 0.05);
  color: #0f172a;
  cursor: pointer;
}

.user-trigger__name {
  font-weight: 600;
}

.user-trigger__role {
  font-size: 12px;
  color: #64748b;
}

.user-trigger__chevron {
  font-size: 12px;
  color: #64748b;
}

.main-layout__main {
  padding: 0 24px 24px;
  background: #f5f7fa;
}

.main-layout__card {
  min-height: calc(100vh - 112px);
  padding: 24px;
  background: #fff;
  border: 1px solid rgba(24, 38, 60, 0.08);
  border-radius: 20px;
  box-shadow: 0 14px 34px rgba(16, 24, 40, 0.06);
}

:global(.main-layout__drawer.el-drawer) {
  background: #fff;
}

@media (max-width: 991px) {
  .main-layout__header {
    padding: 16px 16px 12px;
  }

  .main-layout__main {
    padding: 0 16px 16px;
  }

  .main-layout__card {
    min-height: calc(100vh - 96px);
    padding: 20px;
    border-radius: 18px;
  }
}

@media (max-width: 639px) {
  .main-layout__header {
    gap: 12px;
  }

  .user-trigger__name {
    display: none;
  }

  .main-layout__card {
    padding: 16px;
  }
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity 0.18s ease,
    transform 0.18s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-6px);
}
</style>
