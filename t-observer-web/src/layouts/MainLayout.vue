<script setup lang="ts">
import { ArrowDown, Expand, Fold } from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'

const route = useRoute()

const desktopCollapsed = ref(false)
const isMobile = ref(false)
const mobileDrawerOpen = ref(false)
const desktopSidebarId = 'app-sidebar-panel-desktop'
const mobileSidebarId = 'app-sidebar-panel-mobile'

const navItems = [
  {
    name: 'dashboard-overview',
    label: 'Overview',
    to: { name: 'dashboard-overview' },
  },
  {
    name: 'dashboard-tasks',
    label: 'Tasks',
    to: { name: 'dashboard-tasks' },
  },
  {
    name: 'dashboard-records',
    label: 'Records',
    to: { name: 'dashboard-records' },
  },
] as const

type NavItem = (typeof navItems)[number]
const defaultNavItem: NavItem = navItems[0]

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
const currentRouteName = computed(() => String(route?.name ?? ''))
const activeNavItem = computed<NavItem>(
  () => navItems.find((item) => item.name === currentRouteName.value) ?? defaultNavItem,
)

const toggleSidebar = () => {
  if (isMobile.value) {
    mobileDrawerOpen.value = !mobileDrawerOpen.value
    return
  }

  desktopCollapsed.value = !desktopCollapsed.value
}

const closeMobileDrawer = () => {
  mobileDrawerOpen.value = false
}

const handleNavClick = () => {
  if (isMobile.value) {
    closeMobileDrawer()
  }
}

const isNavActive = (name: string) => currentRouteName.value === name
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
      <aside
        :id="mobileSidebarId"
        class="sidebar sidebar--drawer"
        data-testid="app-sidebar"
      >
        <div class="sidebar__brand">
          <div class="sidebar__brand-mark">T</div>
          <div class="sidebar__brand-copy">
            <strong>T-Observer</strong>
            <span>Teacher dashboard</span>
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
            <span>Teacher dashboard</span>
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
            :aria-label="sidebarExpanded ? 'Collapse sidebar' : 'Expand sidebar'"
            @click="toggleSidebar"
          >
            <el-icon aria-hidden="true">
              <component :is="isMobile ? Expand : Fold" />
            </el-icon>
          </el-button>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item data-testid="app-breadcrumbs">Dashboard</el-breadcrumb-item>
            <el-breadcrumb-item>{{ activeNavItem.label }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-dropdown trigger="click">
          <button
            class="user-trigger"
            data-testid="user-menu-trigger"
            type="button"
            aria-haspopup="menu"
            aria-label="Leader Zhang user menu"
          >
            <el-avatar :size="32">LZ</el-avatar>
            <span class="user-trigger__name">Leader Zhang</span>
            <el-icon class="user-trigger__chevron" aria-hidden="true">
              <ArrowDown />
            </el-icon>
          </button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>Profile</el-dropdown-item>
              <el-dropdown-item divided>Sign Out</el-dropdown-item>
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
