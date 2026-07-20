<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store'
import { useSettingsStore } from '@/features/settings/store'
import { useI18n } from '@/features/i18n/useI18n'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import AppSidebarNav from '@/shared/components/AppSidebarNav.vue'
import { Expand, Fold, Moon, Sunny, User } from '@element-plus/icons-vue'

const SIDEBAR_STORAGE_KEY = 'app.sidebarVisible'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const settingsStore = useSettingsStore()
const { locale, locales, setLocale, tCode, switching } = useI18n()
const { isCompact, isMobile, device } = useBreakpoint()

const drawerOpen = ref(false)
const sidebarVisible = ref(readSidebarVisible())
const activeMenu = computed(() => route.path)
const deviceLabel = computed(() => {
  if (device.value === 'mobile') return 'Mobile'
  if (device.value === 'tablet') return 'Tablet'
  return 'Desktop'
})
const menuOpen = computed(() => (isCompact.value ? drawerOpen.value : sidebarVisible.value))

function readSidebarVisible() {
  try {
    const raw = localStorage.getItem(SIDEBAR_STORAGE_KEY)
    if (raw === null) return true
    return raw !== 'false'
  } catch {
    return true
  }
}

function persistSidebarVisible(value) {
  try {
    localStorage.setItem(SIDEBAR_STORAGE_KEY, String(value))
  } catch {
    // ignore
  }
}

watch(
  () => route.fullPath,
  () => {
    drawerOpen.value = false
  },
)

watch(isCompact, (compact) => {
  if (!compact) drawerOpen.value = false
})

watch(sidebarVisible, (value) => {
  persistSidebarVisible(value)
})

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'login' })
}

async function onLocaleChange(code) {
  await setLocale(code)
}

function onUserCommand(command) {
  if (command === 'change-password') {
    router.push({ name: 'change-password' })
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}

function toggleNav() {
  if (isCompact.value) {
    drawerOpen.value = !drawerOpen.value
    return
  }
  sidebarVisible.value = !sidebarVisible.value
}
</script>

<template>
  <el-container class="layout">
    <el-aside
      v-if="!isCompact"
      class="sidebar desktop-sidebar"
      :class="{ 'desktop-sidebar--hidden': !sidebarVisible }"
      :width="sidebarVisible ? 'var(--app-sidebar-width)' : '0'"
    >
      <AppSidebarNav v-show="sidebarVisible" :active-menu="activeMenu" />
    </el-aside>

    <el-drawer
      v-model="drawerOpen"
      direction="ltr"
      size="260px"
      :with-header="false"
      class="nav-drawer"
      append-to-body
    >
      <AppSidebarNav :active-menu="activeMenu" @navigate="drawerOpen = false" />
    </el-drawer>

    <el-container class="content-shell">
      <el-header class="header" height="var(--app-header-height)">
        <div class="header-left">
          <el-button
            class="nav-toggle"
            text
            :aria-label="menuOpen ? tCode('common', 'hideMenu') : tCode('common', 'showMenu')"
            :title="menuOpen ? tCode('common', 'hideMenu') : tCode('common', 'showMenu')"
            @click="toggleNav"
          >
            <el-icon :size="22">
              <Fold v-if="menuOpen" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <span v-if="isCompact || !sidebarVisible" class="brand-inline">miniproject</span>
          <el-tag
            v-if="!isMobile"
            size="small"
            type="info"
            effect="plain"
            class="device-badge"
          >
            {{ deviceLabel }}
          </el-tag>
        </div>

        <div class="header-right">
          <el-button
            class="theme-toggle"
            text
            :aria-label="settingsStore.isDark ? '라이트 모드' : '다크 모드'"
            :title="settingsStore.isDark ? '라이트 모드' : '다크 모드'"
            @click="settingsStore.toggleDarkMode()"
          >
            <el-icon :size="18">
              <Sunny v-if="settingsStore.isDark" />
              <Moon v-else />
            </el-icon>
          </el-button>
          <el-select
            :model-value="locale"
            size="small"
            class="locale-select"
            :loading="switching"
            :disabled="switching"
            @change="onLocaleChange"
          >
            <el-option
              v-for="item in locales"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>

          <el-dropdown trigger="click" @command="onUserCommand">
            <el-button
              class="user-menu-trigger"
              text
              :aria-label="authStore.user?.name || tCode('common', 'user')"
            >
              <el-icon :size="18"><User /></el-icon>
              <span v-if="!isMobile" class="user-name">{{ authStore.user?.name || tCode('common', 'user') }}</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  {{ authStore.user?.name || tCode('common', 'user') }}
                </el-dropdown-item>
                <el-dropdown-item command="change-password" divided>
                  {{ tCode('common', 'changePassword') }}
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  {{ tCode('common', 'logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100%;
  max-width: 100vw;
  overflow: hidden;
}

.sidebar {
  background: var(--app-sidebar-bg, #1f2d3d);
  color: #fff;
  height: 100%;
  overflow: hidden;
  transition: width 0.2s ease;
  flex-shrink: 0;
}

.desktop-sidebar--hidden {
  border: none;
  min-width: 0;
  padding: 0;
}

.content-shell {
  min-width: 0;
  min-height: 0;
  flex: 1;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border-bottom: 1px solid var(--app-border-color, #ebeef5);
  background: var(--app-header-bg, #fff);
  overflow: hidden;
  flex-shrink: 0;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.header-left {
  flex: 1 1 auto;
  overflow: hidden;
}

.header-right {
  flex: 0 0 auto;
}

.nav-toggle,
.theme-toggle,
.user-menu-trigger {
  padding: 4px;
  flex-shrink: 0;
}

.user-menu-trigger {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.brand-inline {
  font-weight: 700;
  font-size: 15px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}

.device-badge {
  flex-shrink: 0;
}

.locale-select {
  width: 110px;
  flex-shrink: 0;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.main {
  background: var(--app-page-bg, #f5f7fa);
  padding: var(--page-padding);
  min-width: 0;
  min-height: 0;
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

@media (max-width: 767px) {
  .locale-select {
    width: 72px;
  }

  .header {
    padding: 0 8px;
    gap: 4px;
  }

  .header-left,
  .header-right {
    gap: 4px;
  }
}
</style>
<style>
/* Drawer body flush with dark nav */
.nav-drawer.el-drawer {
  background: var(--app-sidebar-bg, #1f2d3d);
}

.nav-drawer .el-drawer__body {
  padding: 0;
  height: 100%;
}
</style>
