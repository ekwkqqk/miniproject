<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import FileAttachment from '@/shared/components/FileAttachment.vue'
import { useAuthStore } from '@/features/auth/store'
import { useApprovalBadgeStore } from '@/features/approval/badgeStore'
import { LINE_STATUS, lineTypeLabel, statusMeta } from '@/features/approval/data'
import * as approvalApi from '@/features/approval/api'
import { formatDateTime } from '@/shared/utils/date'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const badgeStore = useApprovalBadgeStore()
const loading = ref(false)
const acting = ref(false)
const doc = ref(null)
const comment = ref('')
const files = ref([])

const myId = computed(() => authStore.user?.id)

const myPendingLine = computed(() => {
  if (!doc.value || !myId.value) return null
  return (doc.value.lines || []).find((l) => {
    if (l.approverId !== myId.value || l.status !== 'PENDING') return false
    if (l.lineType === 'APPROVE' || l.lineType === 'AGREE') return l.active
    if (l.lineType === 'POST' || l.lineType === 'NOTIFY') return true
    return false
  })
})

const canEditDraft = computed(() => doc.value?.status === 'DRAFT' && doc.value?.drafterId === myId.value)
const canRecall = computed(() => doc.value?.status === 'IN_PROGRESS' && doc.value?.drafterId === myId.value)
const canNewDraft = computed(() => doc.value?.status === 'REJECTED' && doc.value?.drafterId === myId.value)

async function load() {
  loading.value = true
  try {
    const { data } = await approvalApi.getDocument(route.params.id)
    if (data.success) {
      doc.value = data.data
      if (data.data.fileGroupId) {
        const fileApi = await import('@/features/file/api')
        const res = await fileApi.getFilesByGroup(data.data.fileGroupId)
        if (res.success) files.value = res.data || []
      } else {
        files.value = []
      }
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    loading.value = false
  }
}

async function ensureMe() {
  if (authStore.user?.id) return
  try {
    const client = (await import('@/shared/api/client')).default
    const { data } = await client.get('/users/me')
    if (data.success) {
      authStore.user = data.data
    }
  } catch { /* ignore */ }
}

async function afterAction() {
  comment.value = ''
  await load()
  badgeStore.refresh()
}

async function doApprove() {
  if (!myPendingLine.value) return
  acting.value = true
  try {
    await approvalApi.approveDocument(doc.value.id, { lineId: myPendingLine.value.id, comment: comment.value || null })
    ElMessage.success('승인되었습니다.')
    await afterAction()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    acting.value = false
  }
}

async function doReject() {
  if (!myPendingLine.value) return
  if (!comment.value?.trim()) {
    ElMessage.warning('반려 사유를 입력하세요.')
    return
  }
  acting.value = true
  try {
    await approvalApi.rejectDocument(doc.value.id, { lineId: myPendingLine.value.id, comment: comment.value })
    ElMessage.success('반려되었습니다.')
    await afterAction()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    acting.value = false
  }
}

async function doAck() {
  if (!myPendingLine.value || myPendingLine.value.lineType !== 'NOTIFY') return
  acting.value = true
  try {
    await approvalApi.acknowledgeDocument(doc.value.id, { lineId: myPendingLine.value.id, comment: comment.value || null })
    ElMessage.success('확인되었습니다.')
    await afterAction()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    acting.value = false
  }
}

async function doRecall() {
  try {
    await ElMessageBox.confirm('문서를 회수하시겠습니까?', '회수', { type: 'warning' })
    await approvalApi.recallDocument(doc.value.id)
    ElMessage.success('회수되었습니다.')
    await afterAction()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.response?.data?.message || e.message)
  }
}

async function doSubmit() {
  try {
    await approvalApi.submitDocument(doc.value.id)
    ElMessage.success('상신되었습니다.')
    await afterAction()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  }
}

onMounted(async () => {
  await ensureMe()
  await load()
})
</script>

<template>
  <PageLayout title="결재 상세" :loading="loading">
    <template v-if="doc">
      <ContentPanel :title="doc.title">
        <template #header-actions>
          <el-tag :type="statusMeta(doc.status).type">{{ statusMeta(doc.status).label }}</el-tag>
          <el-button v-if="canEditDraft" @click="router.push({ name: 'approval-edit', params: { id: doc.id } })">수정</el-button>
          <el-button v-if="canEditDraft" type="primary" @click="doSubmit">상신</el-button>
          <el-button v-if="canRecall" @click="doRecall">회수</el-button>
          <el-button v-if="canNewDraft" type="primary" @click="router.push({ name: 'approval-new' })">새 기안</el-button>
        </template>

        <dl class="meta">
          <div><dt>문서번호</dt><dd>{{ doc.docNo }}</dd></div>
          <div><dt>기안자</dt><dd>{{ doc.drafterName }}</dd></div>
          <div><dt>상신일</dt><dd>{{ doc.submittedAt ? formatDateTime(doc.submittedAt) : '-' }}</dd></div>
        </dl>
        <div class="content">{{ doc.content }}</div>
        <div v-if="files.length" class="attach">
          <div class="label">첨부</div>
          <FileAttachment v-model="files" mode="view" />
        </div>
      </ContentPanel>

      <ContentPanel title="결재선">
        <el-table :data="doc.lines || []" border stripe>
          <el-table-column label="단계" prop="stepOrder" width="70" align="center" />
          <el-table-column label="유형" width="90">
            <template #default="{ row }">{{ lineTypeLabel(row.lineType) }}</template>
          </el-table-column>
          <el-table-column label="대상" min-width="160">
            <template #default="{ row }">{{ row.approverName }} ({{ row.approverEmail }})</template>
          </el-table-column>
          <el-table-column label="상태" width="110">
            <template #default="{ row }">
              {{ LINE_STATUS[row.status] || row.status }}
              <el-tag v-if="row.active" size="small" type="warning" style="margin-left: 4px">진행</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="의견" prop="comment" min-width="140" show-overflow-tooltip />
          <el-table-column label="처리시각" width="160">
            <template #default="{ row }">{{ row.actedAt ? formatDateTime(row.actedAt) : '-' }}</template>
          </el-table-column>
        </el-table>
      </ContentPanel>

      <ContentPanel v-if="myPendingLine" title="처리">
        <el-input v-model="comment" type="textarea" :rows="3" placeholder="의견 / 반려 사유" />
        <div class="actions">
          <template v-if="myPendingLine.lineType === 'NOTIFY'">
            <el-button type="primary" :loading="acting" @click="doAck">확인</el-button>
          </template>
          <template v-else>
            <el-button type="danger" :loading="acting" @click="doReject">반려</el-button>
            <el-button type="primary" :loading="acting" @click="doApprove">승인</el-button>
          </template>
        </div>
      </ContentPanel>

      <ContentPanel title="이력">
        <el-timeline>
          <el-timeline-item
            v-for="h in doc.histories || []"
            :key="h.id"
            :timestamp="formatDateTime(h.createdAt)"
          >
            {{ h.actorName }} — {{ h.action }}
            <span v-if="h.comment"> ({{ h.comment }})</span>
          </el-timeline-item>
        </el-timeline>
      </ContentPanel>
    </template>
  </PageLayout>
</template>

<style scoped>
.meta {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 8px 16px;
  margin-bottom: 16px;
}
.meta dt {
  font-size: 12px;
  color: var(--app-text-muted, #909399);
}
.meta dd {
  margin: 0;
}
.content {
  white-space: pre-wrap;
  line-height: 1.6;
  margin-bottom: 16px;
}
.attach .label {
  font-size: 13px;
  margin-bottom: 8px;
  color: var(--app-text-muted);
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}
</style>
