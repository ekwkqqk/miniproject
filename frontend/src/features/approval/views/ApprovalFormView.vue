<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import FileAttachment from '@/shared/components/FileAttachment.vue'
import { DOC_CLASSES, lineTypeLabel } from '@/features/approval/data'
import * as approvalApi from '@/features/approval/api'
import { useApprovalBadgeStore } from '@/features/approval/badgeStore'

const route = useRoute()
const router = useRouter()
const badgeStore = useApprovalBadgeStore()
const loading = ref(false)
const saving = ref(false)
const attachmentRef = ref(null)
const docId = computed(() => route.params.id)
const isEdit = computed(() => !!docId.value)

const form = reactive({
  title: '',
  content: '',
  fileGroupId: null,
  docClass: 'GENERAL',
  scheduledSubmitAt: null,
  lines: [],
})
const files = ref([])

/** 다음 추가할 결재선 유형 */
const pendingType = ref('APPROVE')
const selectedKeys = ref([])
const pickedUserId = ref(null)
const userOptions = ref([])
const userLoading = ref(false)
let keySeq = 0

function isBlocking(type) {
  return type === 'APPROVE' || type === 'AGREE'
}

function nextKey() {
  keySeq += 1
  return `line-${keySeq}`
}

function nextBlockingStep() {
  const blocking = form.lines.filter((l) => isBlocking(l.lineType))
  if (!blocking.length) return 1
  return Math.max(...blocking.map((l) => Number(l.stepOrder) || 0)) + 1
}

function nextNotifyStep() {
  const max = form.lines.reduce((m, l) => Math.max(m, Number(l.stepOrder) || 0), 0)
  return Math.max(max + 1, 100)
}

function createLine(type, user) {
  return {
    key: nextKey(),
    stepOrder: isBlocking(type) ? nextBlockingStep() : nextNotifyStep(),
    sortInStep: form.lines.length,
    lineType: type,
    approverId: user.id,
    approverLabel: `${user.name} (${user.email})`,
  }
}

async function searchUsers(query) {
  userLoading.value = true
  try {
    const { data } = await approvalApi.searchUsers({ keyword: query || '', limit: 20 })
    if (data.success) {
      const used = new Set(form.lines.map((l) => l.approverId))
      userOptions.value = (data.data || [])
        .filter((u) => !used.has(u.id))
        .map((u) => ({
          value: u.id,
          label: `${u.name} (${u.email})`,
          raw: u,
        }))
    }
  } finally {
    userLoading.value = false
  }
}

function setPendingType(type) {
  pendingType.value = type
}

function onUserPicked(userId) {
  if (userId == null) return
  const opt = userOptions.value.find((u) => u.value === userId)
  if (!opt?.raw) {
    pickedUserId.value = null
    return
  }
  if (form.lines.some((l) => l.approverId === userId)) {
    ElMessage.warning('이미 결재선에 지정된 사용자입니다.')
    pickedUserId.value = null
    return
  }
  form.lines.push(createLine(pendingType.value, opt.raw))
  pickedUserId.value = null
  searchUsers('')
}

function removeLine(key) {
  const idx = form.lines.findIndex((l) => l.key === key)
  if (idx < 0) return
  form.lines.splice(idx, 1)
  selectedKeys.value = selectedKeys.value.filter((k) => k !== key)
  searchUsers('')
}

function onLineClick(event, key) {
  if (event.ctrlKey || event.metaKey) {
    if (selectedKeys.value.includes(key)) {
      selectedKeys.value = selectedKeys.value.filter((k) => k !== key)
    } else {
      selectedKeys.value = [...selectedKeys.value, key]
    }
  } else {
    selectedKeys.value = selectedKeys.value.length === 1 && selectedKeys.value[0] === key
      ? []
      : [key]
  }
}

function selectedLines() {
  return form.lines.filter((l) => selectedKeys.value.includes(l.key))
}

function applyParallel() {
  const lines = selectedLines()
  if (lines.length < 2) {
    ElMessage.warning('Ctrl로 2명 이상 선택한 뒤 병렬을 눌러주세요.')
    return
  }
  const type = lines[0].lineType
  const sameType = lines.every((l) => l.lineType === type)
  if (!sameType || !isBlocking(type)) {
    ElMessage.warning('같은 유형의 결재자 또는 합의자만 병렬로 묶을 수 있습니다.')
    return
  }
  const step = Math.min(...lines.map((l) => Number(l.stepOrder) || 1))
  lines.forEach((l) => {
    l.stepOrder = step
  })
  ElMessage.success(type === 'APPROVE' ? '병렬결재로 묶었습니다.' : '병렬합의로 묶었습니다.')
}

function releaseParallel() {
  const lines = selectedLines()
  if (!lines.length) {
    ElMessage.warning('해제할 대상을 Ctrl로 선택하세요.')
    return
  }
  let next = Math.max(0, ...form.lines.map((l) => Number(l.stepOrder) || 0))
  let changed = 0
  for (const line of lines) {
    if (!isBlocking(line.lineType)) continue
    const peers = form.lines.filter(
      (l) => l.lineType === line.lineType && l.stepOrder === line.stepOrder,
    )
    if (peers.length < 2) continue
    next += 1
    line.stepOrder = next
    changed += 1
  }
  if (!changed) {
    ElMessage.info('선택된 항목 중 병렬 묶음이 없습니다.')
    return
  }
  ElMessage.success('병렬을 해제했습니다.')
}

function parallelBadge(line) {
  if (!isBlocking(line.lineType)) return ''
  const peers = form.lines.filter(
    (l) => l.lineType === line.lineType && l.stepOrder === line.stepOrder,
  )
  if (peers.length < 2) return ''
  return line.lineType === 'APPROVE' ? '병렬결재' : '병렬합의'
}

function normalizeSteps() {
  form.lines.forEach((l, i) => {
    l.sortInStep = i
  })
}

function buildPayload() {
  normalizeSteps()
  return {
    title: form.title,
    content: form.content,
    docClass: form.docClass || 'GENERAL',
    scheduledSubmitAt: form.scheduledSubmitAt || null,
    fileGroupId: form.fileGroupId != null ? String(form.fileGroupId) : null,
    lines: form.lines.map((l, i) => ({
      stepOrder: Number(l.stepOrder) || i + 1,
      sortInStep: i,
      lineType: l.lineType,
      approverId: l.approverId,
    })),
  }
}

async function ensureFilesUploaded() {
  const pending = files.value.some((f) => f.pending && f.rawFile)
  if (pending && attachmentRef.value?.upload) {
    const result = await attachmentRef.value.upload()
    if (result?.fileGroupId != null) {
      form.fileGroupId = String(result.fileGroupId)
    }
  }
  if (!form.fileGroupId) {
    const existing = files.value.find((f) => f.fileGroupId != null)
    if (existing?.fileGroupId != null) {
      form.fileGroupId = String(existing.fileGroupId)
    }
  }
}

async function load() {
  if (!isEdit.value) {
    form.lines = []
    return
  }
  loading.value = true
  try {
    const { data } = await approvalApi.getDocument(docId.value)
    if (!data.success) return
    const d = data.data
    if (d.status !== 'DRAFT' && d.status !== 'SCHEDULED') {
      ElMessage.warning('임시저장/예약 문서만 수정할 수 있습니다.')
      router.replace({ name: 'approval-detail', params: { id: d.id } })
      return
    }
    form.title = d.title
    form.content = d.content
    form.docClass = d.docClass || 'GENERAL'
    form.scheduledSubmitAt = d.scheduledSubmitAt || null
    form.fileGroupId = d.fileGroupId
    form.lines = (d.lines || []).map((l) => ({
      key: nextKey(),
      stepOrder: l.stepOrder,
      sortInStep: l.sortInStep,
      lineType: l.lineType,
      approverId: l.approverId,
      approverLabel: l.approverName ? `${l.approverName} (${l.approverEmail})` : '',
    }))
    if (d.fileGroupId) {
      const fileApi = await import('@/features/file/api')
      const res = await fileApi.getFilesByGroup(d.fileGroupId)
      if (res.success) files.value = res.data || []
    } else {
      files.value = []
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    loading.value = false
  }
}

async function persistDraft() {
  await ensureFilesUploaded()
  const payload = buildPayload()
  let id = docId.value
  if (isEdit.value) {
    await approvalApi.updateDocument(id, payload)
  } else {
    const { data } = await approvalApi.createDocument(payload)
    id = data.data.id
  }
  return id
}

function validateBeforeSubmit() {
  if (!form.title?.trim() || !form.content?.trim()) {
    ElMessage.warning('제목과 내용을 입력하세요.')
    return false
  }
  if (!form.lines.length) {
    ElMessage.warning('결재선을 추가하세요.')
    return false
  }
  if (form.lines.some((l) => !l.approverId)) {
    ElMessage.warning('결재선 대상자를 모두 지정하세요.')
    return false
  }
  if (!form.lines.some((l) => isBlocking(l.lineType))) {
    ElMessage.warning('상신하려면 결재 또는 합의 라인이 필요합니다.')
    return false
  }
  return true
}

async function save(mode = 'draft') {
  if (mode === 'draft') {
    if (!form.title?.trim() || !form.content?.trim()) {
      ElMessage.warning('제목과 내용을 입력하세요.')
      return
    }
  } else if (!validateBeforeSubmit()) {
    return
  }
  if (mode === 'schedule' && !form.scheduledSubmitAt) {
    ElMessage.warning('예약 상신 날짜/시간을 지정하세요.')
    return
  }
  saving.value = true
  try {
    const id = await persistDraft()
    if (mode === 'submit') {
      await approvalApi.submitDocument(id)
      ElMessage.success('상신되었습니다.')
      badgeStore.refresh()
      router.push({ name: 'approval-detail', params: { id } })
    } else if (mode === 'schedule') {
      await approvalApi.scheduleSubmitDocument(id, { scheduledSubmitAt: form.scheduledSubmitAt })
      ElMessage.success('예약 상신되었습니다.')
      badgeStore.refresh()
      router.push({ name: 'approval-detail', params: { id } })
    } else {
      ElMessage.success('저장되었습니다.')
      router.push({ name: 'approval-edit', params: { id } })
    }
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message)
  } finally {
    saving.value = false
  }
}

function onUploaded(fileIds, groupId) {
  if (groupId != null) form.fileGroupId = String(groupId)
}

onMounted(async () => {
  await searchUsers('')
  await load()
})
</script>

<template>
  <PageLayout :title="isEdit ? '기안 수정' : '기안 작성'" :loading="loading">
    <ContentPanel title="기안">
      <el-form label-position="top" class="draft-form">
        <el-form-item label="제목" required>
          <el-input v-model="form.title" maxlength="300" show-word-limit placeholder="제목을 입력하세요" />
        </el-form-item>

        <div class="options-row">
          <el-form-item label="결재 종류" required class="option-item">
            <el-select v-model="form.docClass" style="width: 100%">
              <el-option
                v-for="c in DOC_CLASSES"
                :key="c.value"
                :label="c.label"
                :value="c.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="예약 상신" class="option-item">
            <el-date-picker
              v-model="form.scheduledSubmitAt"
              type="datetime"
              placeholder="예약 날짜/시간"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
        </div>

        <el-form-item label="결재선" required>
          <div class="line-builder">
            <div class="line-toolbar">
              <el-button
                :type="pendingType === 'APPROVE' ? 'primary' : 'default'"
                @click="setPendingType('APPROVE')"
              >
                결재
              </el-button>
              <el-button
                :type="pendingType === 'AGREE' ? 'primary' : 'default'"
                @click="setPendingType('AGREE')"
              >
                합의
              </el-button>
              <el-button
                :type="pendingType === 'NOTIFY' ? 'primary' : 'default'"
                @click="setPendingType('NOTIFY')"
              >
                통보
              </el-button>
              <el-button @click="applyParallel">병렬</el-button>
              <el-button @click="releaseParallel">해제</el-button>
              <el-select
                v-model="pickedUserId"
                class="user-picker"
                filterable
                remote
                clearable
                :remote-method="searchUsers"
                :loading="userLoading"
                :placeholder="`${lineTypeLabel(pendingType)} 대상자 검색`"
                @change="onUserPicked"
              >
                <el-option
                  v-for="u in userOptions"
                  :key="u.value"
                  :label="u.label"
                  :value="u.value"
                />
              </el-select>
            </div>

            <div class="line-board" tabindex="0">
              <div v-if="!form.lines.length" class="line-empty">
                유형(결재/합의/통보)을 선택한 뒤 사용자를 검색·추가하세요.
                <br />
                Ctrl+클릭으로 여러 명을 선택한 뒤 병렬/해제를 사용할 수 있습니다.
              </div>
              <div
                v-for="line in form.lines"
                :key="line.key"
                class="line-chip"
                :class="{
                  'is-selected': selectedKeys.includes(line.key),
                  'is-parallel': !!parallelBadge(line),
                  [`type-${line.lineType.toLowerCase()}`]: true,
                }"
                @click="onLineClick($event, line.key)"
              >
                <el-tag size="small" effect="plain">{{ lineTypeLabel(line.lineType) }}</el-tag>
                <el-tag v-if="parallelBadge(line)" size="small" type="warning">
                  {{ parallelBadge(line) }}
                </el-tag>
                <span class="line-name">{{ line.approverLabel }}</span>
                <span class="line-step">단계 {{ line.stepOrder }}</span>
                <el-button
                  class="line-remove"
                  :icon="Close"
                  link
                  type="danger"
                  @click.stop="removeLine(line.key)"
                />
              </div>
            </div>
            <p class="hint">
              Ctrl로 결재자·합의자를 복수 선택 → 병렬(병렬결재/병렬합의) · 해제.
              자기결재는 불가합니다.
            </p>
          </div>
        </el-form-item>

        <el-form-item label="파일첨부">
          <FileAttachment
            ref="attachmentRef"
            v-model="files"
            mode="edit"
            @uploaded="onUploaded"
          />
        </el-form-item>

        <el-form-item label="내용" required class="content-item">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="16"
            maxlength="5000"
            show-word-limit
            resize="vertical"
            class="content-textarea"
            placeholder="내용을 입력하세요"
          />
        </el-form-item>
      </el-form>
    </ContentPanel>

    <div class="actions">
      <el-button @click="router.back()">취소</el-button>
      <el-button :loading="saving" @click="save('draft')">임시저장</el-button>
      <el-button :loading="saving" @click="save('schedule')">예약 상신</el-button>
      <el-button type="primary" :loading="saving" @click="save('submit')">상신</el-button>
    </div>
  </PageLayout>
</template>

<style scoped>
.page-layout :deep(.content-panel),
.page-layout :deep(.el-card),
.draft-form {
  width: 100%;
  max-width: none;
}

.content-item {
  width: 100%;
}

.content-textarea :deep(.el-textarea__inner) {
  width: 100%;
  min-height: 280px;
}

.options-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px 16px;
}

.option-item {
  margin-bottom: 18px;
}

.line-builder {
  width: 100%;
}

.line-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 10px;
}

.user-picker {
  min-width: 240px;
  flex: 1;
}

.line-board {
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  background: var(--el-fill-color-blank);
  min-height: 140px;
  max-height: 240px;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.line-empty {
  color: var(--app-text-muted, #909399);
  font-size: 13px;
  line-height: 1.6;
  padding: 12px 4px;
}

.line-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-bg-color);
  cursor: pointer;
  user-select: none;
}

.line-chip:hover {
  border-color: var(--el-color-primary-light-5);
}

.line-chip.is-selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: inset 0 0 0 1px var(--el-color-primary-light-5);
}

.line-chip.is-parallel {
  border-style: dashed;
}

.line-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-step {
  font-size: 12px;
  color: var(--app-text-muted, #909399);
  white-space: nowrap;
}

.line-remove {
  margin-left: 2px;
}

.hint {
  margin: 8px 0 0;
  color: var(--app-text-muted, #909399);
  font-size: 13px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}
</style>
