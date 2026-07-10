<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const props = defineProps({
  code: {
    type: Number,
    required: true,
  },
  title: {
    type: String,
    required: true,
  },
  message: {
    type: String,
    required: true,
  },
})

const router = useRouter()
const authStore = useAuthStore()

const primaryAction = computed(() => {
  if (props.code === 401) {
    return { label: '로그인', to: 'login' }
  }
  return { label: '대시보드로', to: 'dashboard' }
})

function goPrimary() {
  router.push({ name: primaryAction.value.to })
}

function goHome() {
  if (authStore.isAuthenticated) {
    router.push({ name: 'dashboard' })
  } else {
    router.push({ name: 'login' })
  }
}
</script>

<template>
  <div class="error-page">
    <div class="error-card">
      <p class="code">{{ code }}</p>
      <h1>{{ title }}</h1>
      <p class="message">{{ message }}</p>
      <div class="actions">
        <el-button type="primary" @click="goPrimary">{{ primaryAction.label }}</el-button>
        <el-button @click="goHome">홈으로</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #f0f4f8 0%, #e8eef5 100%);
  padding: 24px;
}

.error-card {
  width: 100%;
  max-width: 440px;
  text-align: center;
  background: #fff;
  border-radius: 12px;
  padding: 48px 32px;
  box-shadow: 0 8px 24px rgba(31, 45, 61, 0.08);
}

.code {
  margin: 0;
  font-size: 72px;
  font-weight: 700;
  line-height: 1;
  color: #1f2d3d;
  letter-spacing: -2px;
}

h1 {
  margin: 16px 0 8px;
  font-size: 22px;
  color: #303133;
}

.message {
  margin: 0 0 28px;
  color: #606266;
  line-height: 1.6;
}

.actions {
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>
