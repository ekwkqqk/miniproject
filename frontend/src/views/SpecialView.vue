<script setup>
import { onMounted, ref } from 'vue'
import * as specialApi from '@/api/special'
import { ElMessage } from 'element-plus'

const info = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await specialApi.getSpecialInfo()
    if (data.success) {
      info.value = data.data
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <span>특별 사용자 영역</span>
    </template>
    <template v-if="info">
      <h3>{{ info.title }}</h3>
      <p>{{ info.message }}</p>
    </template>
  </el-card>
</template>
