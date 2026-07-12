<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store'
import { useI18n } from '@/features/i18n/useI18n'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import AppSidebarNav from '@/shared/components/AppSidebarNav.vue'
import { Expand, Fold, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { locale, locales, setLocale } = useI18n()
const { isCompact, isMobile, device } = useBreakpoint()

const drawerOpen = ref(false)
const activeMenu = computed(() => route.path)
const deviceLabel = computed(() => {
  if (device.value === 'mobile') return 'Mobile'
  if (device.value === 'tablet') return 'Tablet'
  return 'Desktop'
})

watch(
  () => route.fullPath,
  () => {
    drawerOpen.value = false
  },
)

watch(isCompact, (compact) => {
  if (!compact) drawerOpen.value = false
})

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'login' })
}

async function onLocaleChange(code) {
  await setLocale(code)
}

function toggleNav() {
  drawerOpen.value = !drawerOpen.value
}
</script>

<template>
  <el-container class="layout">
    <el-aside v-if="!isCompact" :width="'var(--app-sidebar-width)'" class="sidebar desktop-sidebar">
      <AppSidebarNav :active-menu="activeMenu" />
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
            v-if="isCompact"
            class="nav-toggle"
            text
            :aria-label="drawerOpen ? '메뉴 닫기' : '메뉴 열기'"
            @click="toggleNav"
          >
            <el-icon :size="22">
              <Fold v-if="drawerOpen" />
              <Expand v-else />
            </el-icon>
          </el-button>
          <span v-if="isCompact" class="brand-inline">miniproject</span>
          <el-tag size="small" type="info" effect="plain" class="device-badge">{{ deviceLabel }}</el-tag>
        </div>

        <div class="header-right">
          <el-select
            v-model="locale"
            size="small"
            class="locale-select"
            @change="onLocaleChange"
          >
            <el-option
              v-for="item in locales"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
          <el-icon class="user-icon"><User /></el-icon>
          <span v-if="!isMobile" class="user-name">{{ authStore.user?.name || '사용자' }}</span>
          <el-button link @click="router.push({ name: 'change-password' })">비밀번호 변경</el-button>
          <el-button type="danger" link @click="handleLogout">로그아웃</el-button>
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
  min-height: 100vh;
  max-width: 100vw;
  overflow-x: hidden;
}

.sidebar {
  background: #1f2d3d;
  color: #fff;
  height: 100vh;
  position: sticky;
  top: 0;
  overflow: hidden;
}

.content-shell {
  min-width: 0;
  flex: 1;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.header-right {
  flex-shrink: 0;
}

.nav-toggle {
  padding: 4px;
}

.brand-inline {
  font-weight: 700;
  font-size: 15px;
  white-space: nowrap;
}

.device-badge {
  flex-shrink: 0;
}

.locale-select {
  width: 110px;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main {
  background: #f5f7fa;
  padding: var(--page-padding);
  min-width: 0;
}

@media (max-width: 767px) {
  .locale-select {
    width: 88px;
  }

  .header {
    padding: 0 8px;
  }
}
</style>

<style>
/* Drawer body flush with dark nav */
.nav-drawer.el-drawer {
  background: #1f2d3d;
}

.nav-drawer .el-drawer__body {
  padding: 0;
  height: 100%;
}
</style>
