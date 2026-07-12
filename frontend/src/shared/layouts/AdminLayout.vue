<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store'
import { useMenuStore } from '@/features/menu/store'
import { useI18n } from '@/features/i18n/useI18n'
import { User } from '@element-plus/icons-vue'
import SidebarMenuNode from '@/features/menu/components/SidebarMenuNode.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const menuStore = useMenuStore()
const { locale, locales, setLocale } = useI18n()

const activeMenu = computed(() => route.path)

async function handleLogout() {
  await authStore.logout()
  router.push({ name: 'login' })
}

async function onLocaleChange(code) {
  await setLocale(code)
  // 언어 변경 후에도 메뉴 트리의 nameI18nKey 기준으로 라벨이 다시 그려지도록
  // (messages 갱신은 setLocale에서 처리)
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="240px" class="sidebar">
      <div class="logo">miniproject</div>
      <el-menu :default-active="activeMenu" router unique-opened>
        <SidebarMenuNode
          v-for="menu in menuStore.menus"
          :key="menu.id"
          :menu="menu"
        />
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
          <el-select
            v-model="locale"
            size="small"
            style="width: 120px"
            @change="onLocaleChange"
          >
            <el-option
              v-for="item in locales"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
          <el-icon><User /></el-icon>
          <span>{{ authStore.user?.name || '사용자' }}</span>
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
}

.sidebar {
  background: #1f2d3d;
  color: #fff;
}

.logo {
  padding: 20px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.header {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  background: #fff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.main {
  background: #f5f7fa;
}

:deep(.el-menu) {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #d3dce6;
}

:deep(.el-menu-item.is-active) {
  background: #263445 !important;
  color: #fff;
}
</style>
