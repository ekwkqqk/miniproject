<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageLayout from '@/shared/components/PageLayout.vue'
import SearchPanel from '@/shared/components/SearchPanel.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import { docClassLabel, statusMeta } from '@/features/approval/data'
import * as approvalApi from '@/features/approval/api'
import { useApprovalBadgeStore } from '@/features/approval/badgeStore'
import { formatDateTime } from '@/shared/utils/date'

const props = defineProps({
  title: { type: String, required: true },
  box: { type: String, required: true },
  showStatusFilter: { type: Boolean, default: true },
  noticeFilter: { type: Boolean, default: false },
  createPath: { type: String, default: '' },
})

const router = useRouter()
const badgeStore = useApprovalBadgeStore()
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const filters = reactive({ keyword: '', status: props.noticeFilter ? 'PENDING' : '' })

async function load() {
  loading.value = true
  try {
    const params = { box: props.box, page: page.value, size: size.value }
    if (filters.keyword?.trim()) params.keyword = filters.keyword.trim()
    if (filters.status) params.status = filters.status
    const res = props.box === 'notices'
      ? await approvalApi.getNotices(params)
      : await approvalApi.getDocuments(params)
    if (res.data.success) {
      rows.value = res.data.data.items || []
      total.value = res.data.data.total || 0
      page.value = res.data.data.page || page.value
    }
    badgeStore.refresh()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
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
  filters.status = props.noticeFilter ? 'PENDING' : ''
  page.value = 1
  load()
}

function openDetail(row) {
  router.push({ name: 'approval-detail', params: { id: row.id } })
}

onMounted(load)
</script>

<template>
  <PageLayout :title="title">
    <SearchPanel :searching="loading" @search="handleSearch" @reset="handleReset">
      <el-form-item label="검색">
        <el-input v-model="filters.keyword" clearable placeholder="제목/문서번호" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item v-if="noticeFilter" label="통보상태">
        <el-select v-model="filters.status" style="width: 140px">
          <el-option label="미확인" value="PENDING" />
          <el-option label="확인" value="ACKNOWLEDGED" />
          <el-option label="전체" value="ALL" />
        </el-select>
      </el-form-item>
      <el-form-item v-else-if="showStatusFilter" label="상태">
        <el-select v-model="filters.status" clearable placeholder="전체" style="width: 140px">
          <el-option label="임시저장" value="DRAFT" />
          <el-option label="예약상신" value="SCHEDULED" />
          <el-option label="진행중" value="IN_PROGRESS" />
          <el-option label="승인" value="APPROVED" />
          <el-option label="반려" value="REJECTED" />
        </el-select>
      </el-form-item>
    </SearchPanel>

    <ContentPanel :title="title" :count="total" :loading="loading">
      <template v-if="createPath" #header-actions>
        <el-button type="primary" @click="router.push({ name: 'approval-new' })">기안 작성</el-button>
      </template>
      <el-table :data="rows" stripe border style="width: 100%" @row-click="openDetail">
        <el-table-column prop="docNo" label="문서번호" width="160" />
        <el-table-column prop="title" label="제목" min-width="200" show-overflow-tooltip />
        <el-table-column label="종류" width="90" align="center">
          <template #default="{ row }">{{ docClassLabel(row.docClass) }}</template>
        </el-table-column>
        <el-table-column label="상태" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusMeta(row.status).type">{{ statusMeta(row.status).label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="예약상신" width="160">
          <template #default="{ row }">{{ row.scheduledSubmitAt ? formatDateTime(row.scheduledSubmitAt) : '-' }}</template>
        </el-table-column>
        <el-table-column label="상신일" width="160">
          <template #default="{ row }">{{ row.submittedAt ? formatDateTime(row.submittedAt) : '-' }}</template>
        </el-table-column>
        <el-table-column label="수정일" width="160">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="(p) => { page = p; load() }"
        />
      </template>
    </ContentPanel>
  </PageLayout>
</template>
