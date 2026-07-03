<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { House, User } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeMenu = computed(() => route.path)

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">miniproject</div>
      <el-menu :default-active="activeMenu" router>
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <span>대시보드</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-right">
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
</style>
