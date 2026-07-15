<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '@/features/auth/store'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
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
  <PageLayout title="대시보드" subtitle="내 계정 정보 및 메뉴 버튼 권한">
    <ContentPanel title="내 정보">
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
    </ContentPanel>

    <ContentPanel title="버튼 권한 (v-can)">
      <el-button v-can:read type="info">조회</el-button>
      <el-button v-can:update type="primary">수정</el-button>
      <el-button v-can:delete type="danger">삭제</el-button>
      <el-button v-can:upload>업로드</el-button>
      <el-button v-can:download>다운로드</el-button>
      <el-button v-can:other>기타</el-button>
    </ContentPanel>
  </PageLayout>
</template>
