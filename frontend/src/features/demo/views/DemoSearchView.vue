<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import {
  CATEGORIES,
  DEMO_ITEMS,
  STATUSES,
  formatPrice,
  statusMeta,
} from '@/features/demo/data'
import { Search, RefreshRight, View, EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const { isMobile, device } = useBreakpoint()

const filters = reactive({
  keyword: '',
  category: '',
  status: '',
})

const applied = reactive({
  keyword: '',
  category: '',
  status: '',
})

const loading = ref(false)

const rows = computed(() => {
  return DEMO_ITEMS.filter((item) => {
    if (applied.keyword) {
      const q = applied.keyword.toLowerCase()
      const hay = `${item.name} ${item.owner} ${item.id}`.toLowerCase()
      if (!hay.includes(q)) return false
    }
    if (applied.category && item.category !== applied.category) return false
    if (applied.status && item.status !== applied.status) return false
    return true
  })
})

function handleSearch() {
  loading.value = true
  Object.assign(applied, { ...filters })
  setTimeout(() => {
    loading.value = false
  }, 200)
}

function handleReset() {
  filters.keyword = ''
  filters.category = ''
  filters.status = ''
  handleSearch()
}

function goView(row) {
  router.push({ name: 'demo-view', params: { id: String(row.id) } })
}

function goEdit(row) {
  router.push({ name: 'demo-edit', params: { id: String(row.id) } })
}
</script>

<template>
  <div class="page-shell">
    <div class="page-toolbar">
      <div>
        <h1 class="page-toolbar__title">검색 화면</h1>
        <p class="subtitle">필터 + 결과 테이블 반응형 테스트 (현재: {{ device }})</p>
      </div>
    </div>

    <el-card shadow="never">
      <div class="filter-panel">
        <el-form-item label="키워드" class="filter-item">
          <el-input
            v-model="filters.keyword"
            clearable
            placeholder="상품명 / 담당자 / ID"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="카테고리" class="filter-item">
          <el-select v-model="filters.category" clearable placeholder="전체" style="width: 100%">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="상태" class="filter-item">
          <el-select v-model="filters.status" clearable placeholder="전체" style="width: 100%">
            <el-option
              v-for="s in STATUSES"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </el-form-item>
        <div class="filter-panel__actions">
          <el-button type="primary" :icon="Search" @click="handleSearch">검색</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">초기화</el-button>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="page-toolbar">
          <span>검색 결과 {{ rows.length }}건</span>
        </div>
      </template>

      <!-- Mobile: card list -->
      <div v-if="isMobile" class="mobile-list">
        <article v-for="row in rows" :key="row.id" class="mobile-card">
          <div class="mobile-card__head">
            <strong>{{ row.name }}</strong>
            <el-tag size="small" :type="statusMeta(row.status).type">
              {{ statusMeta(row.status).label }}
            </el-tag>
          </div>
          <dl class="mobile-card__meta">
            <div><dt>ID</dt><dd>{{ row.id }}</dd></div>
            <div><dt>카테고리</dt><dd>{{ row.category }}</dd></div>
            <div><dt>가격</dt><dd>{{ formatPrice(row.price) }}</dd></div>
            <div><dt>재고</dt><dd>{{ row.stock }}</dd></div>
            <div><dt>담당</dt><dd>{{ row.owner }}</dd></div>
          </dl>
          <div class="mobile-card__actions">
            <el-button size="small" :icon="View" @click="goView(row)">조회</el-button>
            <el-button size="small" type="primary" :icon="EditPen" @click="goEdit(row)">수정</el-button>
          </div>
        </article>
        <el-empty v-if="!rows.length" description="검색 결과가 없습니다." />
      </div>

      <!-- Tablet / Desktop: table -->
      <div v-else class="table-scroll">
        <el-table :data="rows" stripe border style="width: 100%">
          <el-table-column prop="id" label="ID" width="90" />
          <el-table-column prop="name" label="상품명" min-width="160" />
          <el-table-column prop="category" label="카테고리" width="110" />
          <el-table-column label="상태" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusMeta(row.status).type">
                {{ statusMeta(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="가격" width="120" align="right">
            <template #default="{ row }">{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="stock" label="재고" width="80" align="right" />
          <el-table-column prop="owner" label="담당자" width="100" />
          <el-table-column prop="updatedAt" label="수정일" width="120" />
          <el-table-column label="작업" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="goView(row)">조회</el-button>
              <el-button link type="primary" @click="goEdit(row)">수정</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.subtitle {
  margin: 4px 0 0;
  color: #909399;
  font-size: 13px;
}

.filter-item {
  margin-bottom: 0;
}

.filter-item :deep(.el-form-item__label) {
  padding-bottom: 4px;
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
  background: #fff;
}

.mobile-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.mobile-card__meta {
  margin: 0;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  font-size: 13px;
}

.mobile-card__meta dt {
  color: #909399;
  font-size: 11px;
}

.mobile-card__meta dd {
  margin: 2px 0 0;
}

.mobile-card__actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.mobile-card__actions .el-button {
  flex: 1;
}
</style>
