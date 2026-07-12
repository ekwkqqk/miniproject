<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useMenuAuth } from '@/composables/useMenuAuth'
import { useI18n } from '@/composables/useI18n'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const { canRead, canUpdate, canDelete, canUpload, canDownload, canOther } = useMenuAuth()
const { tCode } = useI18n()

const welcome = computed(() =>
  tCode('common', 'welcome', { name: authStore.user?.name || '' })
)

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
    <p>{{ welcome }}</p>
    <el-descriptions :column="1" border style="margin-top: 16px">
      <el-descriptions-item label="이메일">{{ authStore.user?.email }}</el-descriptions-item>
      <el-descriptions-item label="Role">
        <el-tag v-for="role in authStore.user?.roles || []" :key="role.id" style="margin-right: 4px">
          {{ role.name }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="가입일">{{ authStore.user?.createdAt }}</el-descriptions-item>
    </el-descriptions>

    <div class="button-demo" style="margin-top: 20px">
      <p>현재 메뉴 버튼 권한 (useMenuAuth)</p>
      <el-button v-if="canRead" type="info">조회</el-button>
      <el-button v-if="canUpdate" type="primary">수정</el-button>
      <el-button v-if="canDelete" type="danger">삭제</el-button>
      <el-button v-if="canUpload">업로드</el-button>
      <el-button v-if="canDownload">다운로드</el-button>
      <el-button v-if="canOther">기타</el-button>
    </div>
  </el-card>
</template>
