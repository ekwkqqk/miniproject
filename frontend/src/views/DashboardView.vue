<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()

onMounted(async () => {
  try {
    await authStore.fetchMe()
  } catch (error) {
    ElMessage.error(error.message)
  }
})
</script>

<template>
  <el-card>
    <template #header>
      <span>대시보드</span>
    </template>
    <p>안녕하세요, <strong>{{ authStore.user?.name }}</strong>님!</p>
    <el-descriptions :column="1" border style="margin-top: 16px">
      <el-descriptions-item label="이메일">{{ authStore.user?.email }}</el-descriptions-item>
      <el-descriptions-item label="역할">{{ authStore.user?.role }}</el-descriptions-item>
      <el-descriptions-item label="가입일">{{ authStore.user?.createdAt }}</el-descriptions-item>
    </el-descriptions>
  </el-card>
</template>
