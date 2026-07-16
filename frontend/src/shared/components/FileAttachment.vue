<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Delete, Download } from '@element-plus/icons-vue'
import * as fileApi from '@/features/file/api'

let localSeq = 0

const props = defineProps({
  /**
   * edit: 첨부/삭제/업로드
   * view: 체크박스 + 다운로드 (읽기 전용)
   */
  mode: {
    type: String,
    default: 'edit',
    validator: (v) => ['edit', 'view'].includes(v),
  },
  /**
   * 파일 목록
   * - 대기: { localKey, originalName, sizeBytes, contentType, rawFile, pending: true }
   * - 완료: { id, fileGroupId, originalName, sizeBytes, contentType, ... }
   */
  modelValue: {
    type: Array,
    default: () => [],
  },
  /** view 모드에서 선택된 파일 id 목록 */
  selected: {
    type: Array,
    default: () => [],
  },
  /**
   * 허용 타입. 예: ".pdf,.png,image/*,application/pdf"
   * 비어 있으면 제한 없음
   */
  accept: {
    type: String,
    default: '',
  },
  /** 최대 파일 개수 (0 이하면 무제한) */
  limit: {
    type: Number,
    default: 5,
  },
  multiple: {
    type: Boolean,
    default: true,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  /** 드롭존 안내 문구 */
  tip: {
    type: String,
    default: '',
  },
  /** edit: 컴포넌트 내 저장(업로드) 버튼 표시 */
  showUploadButton: {
    type: Boolean,
    default: true,
  },
  /** view: 선택 다운로드 버튼 표시 */
  showDownloadButton: {
    type: Boolean,
    default: true,
  },
  /** 업로드 성공 콜백 (fileIds, fileGroupId) */
  onUploaded: {
    type: Function,
    default: null,
  },
})

const emit = defineEmits([
  'update:modelValue',
  'update:selected',
  'uploaded',
  'error',
  'remove',
  'download',
])

const isEdit = computed(() => props.mode !== 'view')
const isView = computed(() => props.mode === 'view')

const dragging = ref(false)
const uploading = ref(false)
const inputRef = ref(null)
const selectedIds = ref([...(props.selected || [])])

watch(
  () => props.selected,
  (ids) => {
    selectedIds.value = [...(ids || [])]
  },
)

const files = computed({
  get: () => props.modelValue || [],
  set: (value) => emit('update:modelValue', value),
})

const pendingFiles = computed(() => files.value.filter((f) => f.pending && f.rawFile))
const hasPending = computed(() => pendingFiles.value.length > 0)

const downloadableFiles = computed(() =>
  files.value.filter((f) => f.id != null && !f.pending),
)

const allSelected = computed(() => {
  const ids = downloadableFiles.value.map((f) => f.id)
  return ids.length > 0 && ids.every((id) => selectedIds.value.includes(id))
})

const hasSelection = computed(() => selectedIds.value.length > 0)

const remaining = computed(() => {
  if (!props.limit || props.limit <= 0) return Number.POSITIVE_INFINITY
  return Math.max(0, props.limit - files.value.length)
})

const canAdd = computed(() => isEdit.value && !props.disabled && remaining.value > 0)

const tipText = computed(() => {
  if (props.tip) return props.tip
  if (isView.value) {
    return files.value.length ? '파일을 선택해 다운로드할 수 있습니다' : '첨부된 파일이 없습니다'
  }
  const parts = []
  if (props.accept) parts.push(`허용: ${props.accept}`)
  if (props.limit > 0) parts.push(`최대 ${props.limit}개`)
  parts.push('선택 후 저장 시 업로드')
  return parts.join(' · ')
})

function setSelected(ids) {
  selectedIds.value = ids
  emit('update:selected', ids)
}

function isChecked(item) {
  return item.id != null && selectedIds.value.includes(item.id)
}

function toggleCheck(item, checked) {
  if (item.id == null) return
  const next = new Set(selectedIds.value)
  if (checked) next.add(item.id)
  else next.delete(item.id)
  setSelected([...next])
}

function toggleSelectAll(checked) {
  if (checked) {
    setSelected(downloadableFiles.value.map((f) => f.id))
  } else {
    setSelected([])
  }
}

function openPicker() {
  if (!canAdd.value || uploading.value) return
  inputRef.value?.click()
}

function onBoxClick() {
  if (!isEdit.value) return
  openPicker()
}

function onDragOver(e) {
  e.preventDefault()
  if (!canAdd.value) return
  dragging.value = true
}

function onDragLeave() {
  dragging.value = false
}

function onDrop(e) {
  e.preventDefault()
  dragging.value = false
  if (!canAdd.value) return
  const list = Array.from(e.dataTransfer?.files || [])
  handleSelected(list)
}

function onInputChange(e) {
  const list = Array.from(e.target.files || [])
  e.target.value = ''
  handleSelected(list)
}

function matchesAccept(file) {
  const rules = (props.accept || '')
    .split(',')
    .map((s) => s.trim().toLowerCase())
    .filter(Boolean)
  if (!rules.length) return true

  const name = (file.name || '').toLowerCase()
  const type = (file.type || '').toLowerCase()

  return rules.some((rule) => {
    if (rule.startsWith('.')) return name.endsWith(rule)
    if (rule.endsWith('/*')) return type.startsWith(rule.slice(0, -1))
    if (rule.includes('/')) return type === rule
    return name.endsWith(`.${rule}`)
  })
}

function handleSelected(selected) {
  if (!isEdit.value || !selected.length) return

  let next = selected
  if (!props.multiple) {
    next = selected.slice(0, 1)
  }

  const accepted = []
  for (const file of next) {
    if (!matchesAccept(file)) {
      ElMessage.warning(`허용되지 않은 형식: ${file.name}`)
      continue
    }
    accepted.push(file)
  }
  if (!accepted.length) return

  if (props.limit > 0 && accepted.length > remaining.value) {
    ElMessage.warning(`파일은 최대 ${props.limit}개까지 첨부할 수 있습니다.`)
    accepted.splice(remaining.value)
  }
  if (!accepted.length) return

  const pending = accepted.map((file) => ({
    localKey: `local-${++localSeq}`,
    originalName: file.name,
    sizeBytes: file.size,
    contentType: file.type || null,
    rawFile: file,
    pending: true,
  }))

  if (!props.multiple) {
    files.value = [...files.value.filter((f) => !f.pending), ...pending]
  } else {
    files.value = [...files.value, ...pending]
  }
}

/**
 * 대기 중인 로컬 파일을 서버에 업로드한다.
 * @returns {Promise<{ fileIds: number[], fileGroupId: number, files: object[] } | null>}
 */
async function upload() {
  if (!isEdit.value || uploading.value) return null
  const toUpload = pendingFiles.value
  if (!toUpload.length) {
    ElMessage.warning('업로드할 파일이 없습니다.')
    return null
  }

  uploading.value = true
  try {
    const rawFiles = toUpload.map((f) => f.rawFile)
    const limitForRequest = props.limit > 0 ? props.limit : undefined
    const res = await fileApi.uploadFiles(rawFiles, {
      accept: props.accept || undefined,
      limit: limitForRequest,
    })
    if (!res.success || !res.data) {
      throw new Error(res.message || '업로드에 실패했습니다.')
    }

    const { fileGroupId, fileIds, files: uploaded } = res.data
    const uploadedList = uploaded || []

    const kept = files.value.filter((f) => !f.pending)
    files.value = [...kept, ...uploadedList]

    const payload = {
      fileIds: fileIds || uploadedList.map((f) => f.id),
      fileGroupId,
      files: uploadedList,
    }
    emit('uploaded', payload.fileIds, payload.fileGroupId, payload)
    if (typeof props.onUploaded === 'function') {
      props.onUploaded(payload.fileIds, payload.fileGroupId)
    }
    ElMessage.success(`${uploadedList.length}개 파일이 업로드되었습니다.`)
    return payload
  } catch (error) {
    emit('error', error)
    ElMessage.error(error.message || '업로드에 실패했습니다.')
    throw error
  } finally {
    uploading.value = false
  }
}

async function removeFile(item) {
  if (!isEdit.value || props.disabled || uploading.value) return
  try {
    if (item.id != null && !item.pending) {
      await fileApi.deleteFile(item.id)
    }
    files.value = files.value.filter((f) => {
      if (item.localKey != null) return f.localKey !== item.localKey
      return f.id !== item.id
    })
    if (item.id != null) {
      setSelected(selectedIds.value.filter((id) => id !== item.id))
    }
    emit('remove', item)
  } catch (error) {
    ElMessage.error(error.message || '삭제에 실패했습니다.')
  }
}

async function download(item) {
  if (item.id == null || item.pending) return
  try {
    await fileApi.downloadFile(item.id, item.originalName)
    emit('download', [item.id])
  } catch (error) {
    ElMessage.error(error.message || '다운로드에 실패했습니다.')
  }
}

async function downloadSelected() {
  const targets = downloadableFiles.value.filter((f) => selectedIds.value.includes(f.id))
  if (!targets.length) {
    ElMessage.warning('다운로드할 파일을 선택하세요.')
    return
  }
  try {
    for (const item of targets) {
      await fileApi.downloadFile(item.id, item.originalName)
    }
    emit('download', targets.map((f) => f.id))
    ElMessage.success(`${targets.length}개 파일 다운로드를 시작했습니다.`)
  } catch (error) {
    ElMessage.error(error.message || '다운로드에 실패했습니다.')
  }
}

function formatSize(bytes) {
  if (bytes == null) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function itemKey(item) {
  return item.id ?? item.localKey ?? item.originalName
}

defineExpose({
  upload,
  hasPending,
  uploading,
  selectedIds,
  downloadSelected,
})
</script>

<template>
  <div
    class="file-attachment"
    :class="{
      'is-disabled': disabled,
      'is-view': isView,
      'is-edit': isEdit,
    }"
  >
    <div
      class="file-attachment__box"
      :class="{
        'is-dragging': dragging,
        'is-busy': uploading,
        'is-full': isEdit && !canAdd,
        'has-files': files.length > 0,
        'is-view': isView,
      }"
      @click="onBoxClick"
      @dragover="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
    >
      <!-- edit: 드롭/선택 안내 -->
      <div v-if="isEdit" class="file-attachment__hint">
        <el-icon class="file-attachment__icon" :size="files.length ? 22 : 36">
          <UploadFilled />
        </el-icon>
        <div class="file-attachment__hint-text">
          <p class="file-attachment__title">
            {{ uploading ? '업로드 중…' : '파일을 여기로 드래그하거나 클릭하세요' }}
          </p>
          <p class="file-attachment__tip">{{ tipText }}</p>
        </div>
      </div>

      <!-- view: 헤더 (전체선택) -->
      <div v-else class="file-attachment__view-header" @click.stop>
        <el-checkbox
          :model-value="allSelected"
          :indeterminate="hasSelection && !allSelected"
          :disabled="!downloadableFiles.length"
          @change="toggleSelectAll"
        >
          전체 선택
        </el-checkbox>
        <span class="file-attachment__tip">{{ tipText }}</span>
      </div>

      <ul v-if="files.length" class="file-attachment__list" @click.stop>
        <li v-for="item in files" :key="itemKey(item)" class="file-attachment__item">
          <el-checkbox
            v-if="isView"
            class="file-attachment__check"
            :model-value="isChecked(item)"
            :disabled="item.id == null || item.pending"
            @change="(val) => toggleCheck(item, val)"
          />
          <el-icon v-else class="file-attachment__doc"><Document /></el-icon>
          <div class="file-attachment__meta">
            <span class="file-attachment__name" :title="item.originalName">
              {{ item.originalName }}
              <em v-if="item.pending" class="file-attachment__badge">대기</em>
            </span>
            <span class="file-attachment__size">{{ formatSize(item.sizeBytes) }}</span>
          </div>
          <div class="file-attachment__actions">
            <!-- view: 개별 다운로드 -->
            <el-button
              v-if="isView && item.id != null && !item.pending"
              link
              type="primary"
              :icon="Download"
              @click.stop="download(item)"
            >
              다운로드
            </el-button>
            <!-- edit: 업로드된 파일 다운로드(선택) + 삭제 -->
            <template v-if="isEdit">
              <el-button
                v-if="item.id != null && !item.pending"
                link
                type="primary"
                :icon="Download"
                @click.stop="download(item)"
              />
              <el-button
                link
                type="danger"
                :icon="Delete"
                :disabled="disabled || uploading"
                @click.stop="removeFile(item)"
              />
            </template>
          </div>
        </li>
      </ul>

      <p v-else-if="isView" class="file-attachment__empty">첨부된 파일이 없습니다.</p>

      <div
        v-if="isEdit && showUploadButton && hasPending"
        class="file-attachment__footer"
        @click.stop
      >
        <el-button
          type="primary"
          size="small"
          :loading="uploading"
          :disabled="disabled"
          @click="upload"
        >
          저장 (업로드)
        </el-button>
      </div>

      <div
        v-if="isView && showDownloadButton && downloadableFiles.length"
        class="file-attachment__footer"
        @click.stop
      >
        <el-button
          type="primary"
          size="small"
          :icon="Download"
          :disabled="!hasSelection"
          @click="downloadSelected"
        >
          선택 다운로드
        </el-button>
      </div>

      <input
        v-if="isEdit"
        ref="inputRef"
        class="file-attachment__input"
        type="file"
        :accept="accept || undefined"
        :multiple="multiple && (limit <= 0 || limit > 1)"
        :disabled="!canAdd || uploading"
        @change="onInputChange"
      />
    </div>
  </div>
</template>

<style scoped>
.file-attachment {
  width: 100%;
}

.file-attachment__box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 140px;
  padding: 16px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
  box-sizing: border-box;
}

.file-attachment__box.is-view {
  cursor: default;
  border-style: solid;
  min-height: auto;
}

.file-attachment__box:not(.is-view):hover:not(.is-full):not(.is-busy),
.file-attachment__box.is-dragging {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.file-attachment__box.is-full,
.file-attachment.is-disabled .file-attachment__box {
  cursor: not-allowed;
  opacity: 0.65;
}

.file-attachment.is-disabled .file-attachment__box.is-view {
  cursor: default;
}

.file-attachment__hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  text-align: center;
  flex-shrink: 0;
}

.file-attachment__box.has-files .file-attachment__hint {
  flex-direction: row;
  justify-content: flex-start;
  gap: 10px;
  text-align: left;
}

.file-attachment__view-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.file-attachment__icon {
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.file-attachment__hint-text {
  min-width: 0;
}

.file-attachment__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.file-attachment__box.has-files .file-attachment__title {
  font-size: 13px;
}

.file-attachment__tip {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.file-attachment__empty {
  margin: 0;
  padding: 8px 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.file-attachment__input {
  display: none;
}

.file-attachment__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
  max-height: 220px;
  overflow-y: auto;
  cursor: default;
}

.file-attachment__item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-bg-color);
}

.file-attachment__check {
  flex-shrink: 0;
  margin-right: 0;
}

.file-attachment__doc {
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.file-attachment__meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-attachment__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: var(--el-text-color-primary);
}

.file-attachment__badge {
  margin-left: 6px;
  font-style: normal;
  font-size: 11px;
  color: var(--el-color-warning);
}

.file-attachment__size {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.file-attachment__actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
  align-items: center;
}

.file-attachment__footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
  cursor: default;
}
</style>
