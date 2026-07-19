<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { View, EditPen, Download } from '@element-plus/icons-vue'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import { useExcelDownload } from '@/shared/composables/useExcelDownload'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import SearchPanel from '@/shared/components/SearchPanel.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import { formatDateTime } from '@/shared/utils/date'
import * as demoApi from '@/features/demo/api'
import { CATEGORIES, STATUSES, formatPrice, statusMeta } from '@/features/demo/data'

const { isMobile, device } = useBreakpoint()
const { canDownload } = useMenuAuth()
const { tCode } = useI18n()
const { exporting, download: downloadExcel } = useExcelDownload()

const filters = reactive({
  keyword: '',
  category: '',
  status: '',
  dateRange: null,
  featuredOnly: false,
  inStockOnly: false,
})

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sort = ref('updatedAt')
const order = ref('desc')
const tableKey = ref(0)

const viewOpen = ref(false)
const editOpen = ref(false)
const detailLoading = ref(false)
const viewItem = ref(null)

const editForm = reactive({
  id: null,
  name: '',
  category: '',
  status: 'ACTIVE',
  price: 0,
  stock: 0,
  owner: '',
  description: '',
  featured: false,
})

function buildFilterParams() {
  const params = {}
  if (filters.keyword?.trim()) params.keyword = filters.keyword.trim()
  if (filters.category) params.category = filters.category
  if (filters.status) params.status = filters.status
  if (filters.dateRange?.[0]) params.dateFrom = filters.dateRange[0]
  if (filters.dateRange?.[1]) params.dateTo = filters.dateRange[1]
  if (filters.featuredOnly) params.featured = true
  if (filters.inStockOnly) params.inStock = true
  if (sort.value) params.sort = sort.value
  if (order.value) params.order = order.value
  return params
}

function buildParams() {
  return {
    ...buildFilterParams(),
    page: page.value,
    size: size.value,
  }
}

async function handleExcelDownload() {
  try {
    await downloadExcel({
      url: '/demo/items/export',
      params: buildFilterParams(),
      fallbackFilename: 'demo-items.xlsx',
    })
  } catch {
    // 메시지는 useExcelDownload에서 처리
  }
}

async function load() {
  loading.value = true
  try {
    const { data } = await demoApi.getDemoItems(buildParams())
    if (data.success) {
      rows.value = data.data.items || []
      total.value = data.data.total || 0
      page.value = data.data.page || page.value
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  load()
}

function handleReset() {
  filters.keyword = ''
  filters.category = ''
  filters.status = ''
  filters.dateRange = null
  filters.featuredOnly = false
  filters.inStockOnly = false
  sort.value = 'updatedAt'
  order.value = 'desc'
  tableKey.value += 1
  page.value = 1
  load()
}

function handlePageChange(next) {
  page.value = next
  load()
}

function handleSortChange({ prop, order: sortOrder }) {
  let nextSort = 'updatedAt'
  let nextOrder = 'desc'
  if (prop && sortOrder) {
    nextSort = prop
    nextOrder = sortOrder === 'ascending' ? 'asc' : 'desc'
  }
  if (sort.value === nextSort && order.value === nextOrder) {
    return
  }
  sort.value = nextSort
  order.value = nextOrder
  page.value = 1
  load()
}

function applyEditForm(item) {
  Object.assign(editForm, {
    id: item.id,
    name: item.name,
    category: item.category,
    status: item.status,
    price: item.price,
    stock: item.stock,
    owner: item.owner,
    description: item.description || '',
    featured: !!item.featured,
  })
}

async function fetchItem(id) {
  detailLoading.value = true
  try {
    const { data } = await demoApi.getDemoItem(id)
    if (data.success) return data.data
    return null
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
    return null
  } finally {
    detailLoading.value = false
  }
}

async function openView(row) {
  viewItem.value = null
  viewOpen.value = true
  const item = await fetchItem(row.id)
  if (item) {
    viewItem.value = item
  } else {
    viewOpen.value = false
  }
}

async function openEdit(row) {
  editOpen.value = true
  const item = await fetchItem(row.id)
  if (item) {
    applyEditForm(item)
  } else {
    editOpen.value = false
  }
}

function openEditFromView() {
  if (!viewItem.value) return
  applyEditForm(viewItem.value)
  viewOpen.value = false
  editOpen.value = true
}

function closeView() {
  viewOpen.value = false
}

function closeEdit() {
  editOpen.value = false
}

function handleSave() {
  if (!editForm.name?.trim()) {
    ElMessage.warning('상품명을 입력해주세요.')
    return
  }
  ElMessage.success('저장되었습니다. (데모 — 실제 DB 반영 없음)')
  viewItem.value = { ...editForm }
  editOpen.value = false
  viewOpen.value = true
}

function formatUpdatedAt(value) {
  if (!value) return '-'
  if (typeof value === 'string' && value.length <= 10) return value
  return formatDateTime(value)
}

onMounted(load)
</script>

<template>
  <PageLayout
    title="검색 화면"
    :subtitle="`백엔드 연동 검색 템플릿 (현재: ${device})`"
  >
    <SearchPanel title="검색 조건" :searching="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="키워드" class="filter-item">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="상품명 / 담당자 / ID"
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <el-form-item label="카테고리" class="filter-item">
        <el-select v-model="filters.category" clearable placeholder="전체">
          <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>

      <el-form-item label="수정일" class="filter-item filter-item--wide">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="시작일"
          end-placeholder="종료일"
          unlink-panels
        />
      </el-form-item>

      <el-form-item label="상태" class="filter-item filter-item--wide">
        <el-radio-group v-model="filters.status">
          <el-radio-button value="">전체</el-radio-button>
          <el-radio-button v-for="s in STATUSES" :key="s.value" :value="s.value">
            {{ s.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="옵션" class="filter-item filter-item--wide">
        <div class="filter-checks">
          <el-checkbox v-model="filters.featuredOnly">추천 상품만</el-checkbox>
          <el-checkbox v-model="filters.inStockOnly">재고 있음만</el-checkbox>
        </div>
      </el-form-item>
    </SearchPanel>

    <ContentPanel title="검색 결과" :count="total" :loading="loading">
      <template v-if="canDownload" #header-actions>
        <el-button
          type="success"
          plain
          :icon="Download"
          :loading="exporting"
          @click="handleExcelDownload"
        >
          엑셀 다운로드
        </el-button>
      </template>

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
            <div><dt>추천</dt><dd>{{ row.featured ? 'Y' : 'N' }}</dd></div>
            <div><dt>수정일</dt><dd>{{ formatUpdatedAt(row.updatedAt) }}</dd></div>
          </dl>
          <div class="mobile-card__actions">
            <el-button size="small" :icon="View" @click="openView(row)">조회</el-button>
            <el-button size="small" type="primary" :icon="EditPen" @click="openEdit(row)">수정</el-button>
          </div>
        </article>
        <el-empty v-if="!rows.length && !loading" description="검색 결과가 없습니다." />
      </div>

      <div v-else class="table-scroll">
        <el-table
          :key="tableKey"
          :data="rows"
          stripe
          border
          style="width: 100%"
          :default-sort="{ prop: 'updatedAt', order: 'descending' }"
          @sort-change="handleSortChange"
        >
          <el-table-column prop="id" :label="tCode('table', 'id')" width="90" sortable="custom" />
          <el-table-column prop="name" :label="tCode('table', 'productName')" min-width="160" sortable="custom" />
          <el-table-column prop="category" :label="tCode('table', 'category')" width="120" sortable="custom" />
          <el-table-column prop="status" :label="tCode('table', 'status')" width="110" sortable="custom">
            <template #default="{ row }">
              <el-tag size="small" :type="statusMeta(row.status).type">
                {{ statusMeta(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="price" :label="tCode('table', 'price')" width="130" align="right" sortable="custom">
            <template #default="{ row }">{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="stock" :label="tCode('table', 'stock')" width="100" align="right" sortable="custom" />
          <el-table-column prop="featured" label="추천" width="100" align="center" sortable="custom">
            <template #default="{ row }">
              <el-tag v-if="row.featured" size="small" type="warning">Y</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="owner" :label="tCode('table', 'owner')" width="110" sortable="custom" />
          <el-table-column prop="updatedAt" :label="tCode('table', 'updatedAt')" width="140" sortable="custom">
            <template #default="{ row }">{{ formatUpdatedAt(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column :label="tCode('table', 'actions')" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openView(row)">조회</el-button>
              <el-button link type="primary" @click="openEdit(row)">수정</el-button>
            </template>
          </el-table-column>
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
          @current-change="handlePageChange"
        />
      </template>
    </ContentPanel>

    <ResponsiveDialog
      v-model="viewOpen"
      :title="viewItem?.name || '상품 조회'"
      :width="720"
      themed-header
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <template v-if="viewItem">
          <div class="dialog-status">
            <el-tag :type="statusMeta(viewItem.status).type">
              {{ statusMeta(viewItem.status).label }}
            </el-tag>
          </div>
          <div class="detail-grid">
            <div class="field">
              <div class="label">상품 ID</div>
              <div class="value">{{ viewItem.id }}</div>
            </div>
            <div class="field">
              <div class="label">카테고리</div>
              <div class="value">{{ viewItem.category }}</div>
            </div>
            <div class="field">
              <div class="label">가격</div>
              <div class="value">{{ formatPrice(viewItem.price) }}</div>
            </div>
            <div class="field">
              <div class="label">재고</div>
              <div class="value">{{ viewItem.stock }}</div>
            </div>
            <div class="field">
              <div class="label">담당자</div>
              <div class="value">{{ viewItem.owner }}</div>
            </div>
            <div class="field">
              <div class="label">추천</div>
              <div class="value">{{ viewItem.featured ? 'Y' : 'N' }}</div>
            </div>
            <div class="field">
              <div class="label">최종 수정일</div>
              <div class="value">{{ formatUpdatedAt(viewItem.updatedAt) }}</div>
            </div>
            <div class="field form-grid--full">
              <div class="label">설명</div>
              <div class="value desc">{{ viewItem.description || '-' }}</div>
            </div>
          </div>
        </template>
        <el-empty v-else-if="!detailLoading" description="대상을 찾을 수 없습니다." />
      </div>
      <template #footer>
        <el-button @click="closeView">닫기</el-button>
        <el-button type="primary" :disabled="!viewItem" @click="openEditFromView">수정</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog
      v-model="editOpen"
      title="상품 수정"
      :width="720"
      themed-header
      destroy-on-close
    >
      <div v-loading="detailLoading">
        <el-form
          v-if="editForm.id"
          label-position="top"
          class="form-grid"
          @submit.prevent="handleSave"
        >
          <el-form-item label="상품 ID">
            <el-input :model-value="String(editForm.id)" disabled />
          </el-form-item>
          <el-form-item label="상태">
            <el-radio-group v-model="editForm.status">
              <el-radio v-for="s in STATUSES" :key="s.value" :value="s.value">
                {{ s.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="상품명" class="form-grid--full">
            <el-input v-model="editForm.name" maxlength="100" show-word-limit />
          </el-form-item>
          <el-form-item label="카테고리">
            <el-select v-model="editForm.category" style="width: 100%">
              <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="담당자">
            <el-input v-model="editForm.owner" />
          </el-form-item>
          <el-form-item label="가격">
            <el-input-number v-model="editForm.price" :min="0" :step="1000" style="width: 100%" />
          </el-form-item>
          <el-form-item label="재고">
            <el-input-number v-model="editForm.stock" :min="0" style="width: 100%" />
          </el-form-item>
          <el-form-item label="추천">
            <el-checkbox v-model="editForm.featured">추천 상품</el-checkbox>
          </el-form-item>
          <el-form-item label="설명" class="form-grid--full">
            <el-input
              v-model="editForm.description"
              type="textarea"
              :rows="isMobile ? 4 : 5"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <el-empty v-else-if="!detailLoading" description="수정할 대상을 찾을 수 없습니다." />
      </div>
      <template #footer>
        <el-button @click="closeEdit">취소</el-button>
        <el-button type="primary" :disabled="!editForm.id" @click="handleSave">저장</el-button>
      </template>
    </ResponsiveDialog>
  </PageLayout>
</template>

<style scoped>
.filter-item--wide {
  grid-column: span 2;
}

.filter-checks {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 20px;
  min-height: 32px;
  width: 100%;
}

.table-scroll :deep(.el-table .cell) {
  white-space: nowrap;
}

.table-scroll :deep(.el-table th.el-table__cell > .cell) {
  display: inline-flex;
  align-items: center;
  flex-wrap: nowrap;
  white-space: nowrap;
  line-height: 1.2;
}

.dialog-status {
  margin-bottom: 12px;
}

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

@media (max-width: 1023px) {
  .filter-item--wide {
    grid-column: span 1;
  }
}
</style>
