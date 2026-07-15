<script setup>
import { computed, onMounted, ref } from 'vue'
import * as adminApi from '@/features/admin/api'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import { ElMessage } from 'element-plus'

const { isMobile } = useBreakpoint()
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
  return type === 'MENU' ? '메뉴' : 'API'
}

onMounted(load)
</script>

<template>
  <div class="page-shell">
    <el-card shadow="never">
      <template #header>
        <div class="page-toolbar">
          <div>
            <span class="page-toolbar__title">메뉴 접근 이력</span>
            <p class="subtitle">인터셉터가 기록한 메뉴/관리 API 접근 로그</p>
          </div>
          <div class="page-toolbar__actions">
            <span class="count">총 {{ total }}건</span>
            <el-button @click="load">새로고침</el-button>
          </div>
        </div>
      </template>

      <div v-if="isMobile" v-loading="loading" class="mobile-list">
        <article v-for="row in items" :key="row.id" class="mobile-card">
          <div class="mobile-card__head">
            <strong>{{ row.menuName || row.menuUrl || row.requestUri }}</strong>
            <el-tag size="small" :type="row.accessType === 'MENU' ? 'primary' : 'info'">
              {{ accessTypeLabel(row.accessType) }}
            </el-tag>
          </div>
          <dl class="meta-grid">
            <div><dt>사용자</dt><dd>{{ row.userEmail }}</dd></div>
            <div><dt>시각</dt><dd>{{ row.accessedAt }}</dd></div>
            <div><dt>IP</dt><dd>{{ row.clientIp || '-' }}</dd></div>
            <div><dt>상태</dt><dd>{{ row.httpStatus }}</dd></div>
          </dl>
        </article>
        <el-empty v-if="!items.length && !loading" description="이력이 없습니다." />
      </div>

      <div v-else class="table-scroll">
        <el-table v-loading="loading" :data="items" stripe border style="width: 100%">
          <el-table-column prop="accessedAt" label="접근 시각" width="180" />
          <el-table-column prop="userEmail" label="사용자" min-width="180" />
          <el-table-column label="유형" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.accessType === 'MENU' ? 'primary' : 'info'">
                {{ accessTypeLabel(row.accessType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="menuName" label="메뉴명" min-width="140" />
          <el-table-column prop="menuUrl" label="메뉴 URL" min-width="160" />
          <el-table-column prop="httpMethod" label="Method" width="90" />
          <el-table-column prop="requestUri" label="Request URI" min-width="200" show-overflow-tooltip />
          <el-table-column prop="clientIp" label="IP" width="130" />
          <el-table-column prop="httpStatus" label="Status" width="90" align="center" />
        </el-table>
      </div>

      <div class="pager">
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
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.page-toolbar__title {
  font-size: 1.1rem;
  font-weight: 700;
}

.subtitle {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.count {
  color: #606266;
  font-size: 13px;
}

.mobile-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
}

.mobile-card__head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.meta-grid {
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  font-size: 13px;
}

.meta-grid dt {
  color: #909399;
  font-size: 11px;
}

.meta-grid dd {
  margin: 2px 0 0;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

.page-info {
  color: #909399;
  font-size: 13px;
}
</style>
