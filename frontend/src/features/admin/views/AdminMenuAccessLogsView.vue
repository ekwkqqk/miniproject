<script setup>
import { computed, onMounted, ref } from 'vue'
import * as adminApi from '@/features/admin/api'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import { formatDateTime } from '@/shared/utils/date'
import { ElMessage } from 'element-plus'

const { isMobile } = useBreakpoint()
const { tCode } = useI18n()
const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

async function load() {
  loading.value = true
  try {
    const { data } = await adminApi.getMenuAccessLogs({ page: page.value, size: size.value })
    if (data.success) {
      items.value = data.data.items || []
      total.value = data.data.total || 0
      page.value = data.data.page || page.value
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function accessTypeLabel(type) {
  const value = String(type || '').toUpperCase()
  if (value === 'MENU') return tCode('table', 'typeMenu')
  return tCode('table', 'typeApi')
}

onMounted(load)
</script>

<template>
  <PageLayout
    title="메뉴 접근 이력"
    subtitle="인터셉터가 기록한 메뉴/관리 API 접근 로그"
    :count="total"
  >
    <template #actions>
      <el-button @click="load">{{ tCode('common', 'refresh') }}</el-button>
    </template>

    <ContentPanel title="이력 목록" :count="items.length" count-label="현재 페이지 {n}건" :loading="loading">
      <div v-if="isMobile" class="mobile-list">
        <article v-for="row in items" :key="row.id" class="mobile-card">
          <div class="mobile-card__head">
            <strong>{{ row.menuName || row.menuUrl || row.requestUri }}</strong>
            <span class="access-type">{{ accessTypeLabel(row.accessType) }}</span>
          </div>
          <dl class="meta-grid">
            <div><dt>사용자</dt><dd>{{ row.userEmail }}</dd></div>
            <div><dt>시각</dt><dd>{{ formatDateTime(row.accessedAt) }}</dd></div>
            <div><dt>IP</dt><dd>{{ row.clientIp || '-' }}</dd></div>
            <div><dt>상태</dt><dd>{{ row.httpStatus }}</dd></div>
          </dl>
        </article>
        <el-empty v-if="!items.length && !loading" description="이력이 없습니다." />
      </div>

      <div v-else class="table-scroll">
        <el-table :data="items" stripe border style="width: 100%">
          <el-table-column :label="tCode('table', 'accessedAt')" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.accessedAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="userEmail" :label="tCode('table', 'user')" min-width="180" />
          <el-table-column :label="tCode('table', 'type')" width="90" align="center">
            <template #default="{ row }">
              {{ accessTypeLabel(row.accessType) }}
            </template>
          </el-table-column>
          <el-table-column prop="menuName" :label="tCode('table', 'menuName')" min-width="140" />
          <el-table-column prop="menuUrl" :label="tCode('table', 'menuUrl')" min-width="160" />
          <el-table-column prop="httpMethod" :label="tCode('table', 'httpMethod')" width="90" />
          <el-table-column prop="requestUri" :label="tCode('table', 'requestUri')" min-width="200" show-overflow-tooltip />
          <el-table-column prop="clientIp" :label="tCode('table', 'clientIp')" width="130" />
          <el-table-column prop="httpStatus" :label="tCode('table', 'httpStatus')" width="90" align="center" />
        </el-table>
      </div>

      <template #footer>
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          :pager-count="5"
          layout="total, prev, pager, next"
          background
          @current-change="load"
        />
        <span class="page-info">{{ page }} / {{ totalPages }} 페이지</span>
      </template>
    </ContentPanel>
  </PageLayout>
</template>

<style scoped>
.page-info {
  color: #909399;
  font-size: 13px;
}
</style>
