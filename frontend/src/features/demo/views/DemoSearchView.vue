<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import SearchPanel from '@/shared/components/SearchPanel.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import {
  CATEGORIES,
  DEMO_ITEMS,
  STATUSES,
  formatPrice,
  statusMeta,
} from '@/features/demo/data'
import { View, EditPen } from '@element-plus/icons-vue'

const router = useRouter()
const { isMobile, device } = useBreakpoint()
const { tCode } = useI18n()

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
  <PageLayout
    title="검색 화면"
    :subtitle="`필터 + 결과 테이블 반응형 테스트 (현재: ${device})`"
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
    </SearchPanel>

    <ContentPanel title="검색 결과" :count="rows.length" :loading="loading">
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

      <div v-else class="table-scroll">
        <el-table :data="rows" stripe border style="width: 100%">
          <el-table-column prop="id" :label="tCode('table', 'id')" width="90" />
          <el-table-column prop="name" :label="tCode('table', 'productName')" min-width="160" />
          <el-table-column prop="category" :label="tCode('table', 'category')" width="110" />
          <el-table-column :label="tCode('table', 'status')" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="statusMeta(row.status).type">
                {{ statusMeta(row.status).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="tCode('table', 'price')" width="120" align="right">
            <template #default="{ row }">{{ formatPrice(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="stock" :label="tCode('table', 'stock')" width="80" align="right" />
          <el-table-column prop="owner" :label="tCode('table', 'owner')" width="100" />
          <el-table-column prop="updatedAt" :label="tCode('table', 'updatedAt')" width="120" />
          <el-table-column :label="tCode('table', 'actions')" width="160" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="goView(row)">조회</el-button>
              <el-button link type="primary" @click="goEdit(row)">수정</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentPanel>
  </PageLayout>
</template>
