<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, EditPen } from '@element-plus/icons-vue'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import * as demoApi from '@/features/demo/api'
import { formatPrice, statusMeta } from '@/features/demo/data'

const route = useRoute()
const router = useRouter()
const { device } = useBreakpoint()

const loading = ref(false)
const item = ref(null)

async function load() {
  loading.value = true
  item.value = null
  try {
    if (route.params.id) {
      const { data } = await demoApi.getDemoItem(route.params.id)
      if (data.success) item.value = data.data
      return
    }
    const { data } = await demoApi.getDemoItems({ page: 1, size: 1 })
    if (data.success && data.data.items?.length) {
      item.value = data.data.items[0]
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.push({ name: 'demo-search' })
}

function goEdit() {
  if (!item.value) return
  router.push({ name: 'demo-edit', params: { id: String(item.value.id) } })
}

watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <PageLayout title="조회 화면" :subtitle="`상세 정보 읽기 전용 레이아웃 (현재: ${device})`">
    <template #actions>
      <el-button :icon="Back" @click="goBack">목록</el-button>
      <el-button type="primary" :icon="EditPen" :disabled="!item" @click="goEdit">수정</el-button>
    </template>

    <ContentPanel v-if="item" :title="item.name" :loading="loading">
      <template #header-actions>
        <el-tag :type="statusMeta(item.status).type">{{ statusMeta(item.status).label }}</el-tag>
      </template>

      <div class="detail-grid">
        <div class="field">
          <div class="label">상품 ID</div>
          <div class="value">{{ item.id }}</div>
        </div>
        <div class="field">
          <div class="label">카테고리</div>
          <div class="value">{{ item.category }}</div>
        </div>
        <div class="field">
          <div class="label">가격</div>
          <div class="value">{{ formatPrice(item.price) }}</div>
        </div>
        <div class="field">
          <div class="label">재고</div>
          <div class="value">{{ item.stock }}</div>
        </div>
        <div class="field">
          <div class="label">담당자</div>
          <div class="value">{{ item.owner }}</div>
        </div>
        <div class="field">
          <div class="label">추천</div>
          <div class="value">{{ item.featured ? 'Y' : 'N' }}</div>
        </div>
        <div class="field">
          <div class="label">최종 수정일</div>
          <div class="value">{{ item.updatedAt }}</div>
        </div>
        <div class="field form-grid--full">
          <div class="label">설명</div>
          <div class="value desc">{{ item.description }}</div>
        </div>
      </div>
    </ContentPanel>

    <ContentPanel v-else :show-header="false" :loading="loading">
      <el-empty v-if="!loading" description="대상을 찾을 수 없습니다.">
        <el-button type="primary" @click="goBack">검색으로 이동</el-button>
      </el-empty>
    </ContentPanel>
  </PageLayout>
</template>

<style scoped>
.field .label {
  font-size: 12px;
  color: var(--app-text-muted, #909399);
  margin-bottom: 4px;
}

.field .value {
  font-size: 15px;
  color: var(--app-text-primary, #303133);
  word-break: break-word;
}

.field .desc {
  line-height: 1.6;
  padding: 10px 12px;
  background: var(--app-surface-bg, #fafafa);
  border-radius: 6px;
  border: 1px solid var(--app-border-color, #ebeef5);
}
</style>
