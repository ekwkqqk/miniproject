<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Document, Delete, Download } from '@element-plus/icons-vue'
import * as fileApi from '@/features/file/api'

const props = defineProps({
  /** 업로드된 파일 목록 { id, originalName, contentType, sizeBytes, ... } */
  modelValue: {
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
  /** true면 서버에 즉시 업로드 */
  autoUpload: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['update:modelValue', 'success', 'error', 'remove'])

const dragging = ref(false)
const uploading = ref(false)
const inputRef = ref(null)

const files = computed({
  get: () => props.modelValue || [],
  set: (value) => emit('update:modelValue', value),
})

const remaining = computed(() => {
  if (!props.limit || props.limit <= 0) return Number.POSITIVE_INFINITY
  return Math.max(0, props.limit - files.value.length)
})

const canAdd = computed(() => !props.disabled && remaining.value > 0)

const tipText = computed(() => {
  if (props.tip) return props.tip
  const parts = []
  if (props.accept) parts.push(`허용: ${props.accept}`)
  if (props.limit > 0) parts.push(`최대 ${props.limit}개`)
  return parts.join(' · ') || '파일을 드래그하거나 클릭하여 선택하세요'
})

function openPicker() {
  if (!canAdd.value || uploading.value) return
  inputRef.value?.click()
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

async function handleSelected(selected) {
  if (!selected.length) return

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

  if (!props.autoUpload) {
    emit('success', accepted)
    return
  }

  uploading.value = true
  try {
    const limitForRequest = props.limit > 0 ? props.limit : undefined
    const res = await fileApi.uploadFiles(accepted, {
      accept: props.accept || undefined,
      limit: limitForRequest,
    })
    if (res.success) {
      const uploaded = res.data || []
      files.value = [...files.value, ...uploaded]
      emit('success', uploaded)
      ElMessage.success(`${uploaded.length}개 파일이 업로드되었습니다.`)
    }
  } catch (error) {
    emit('error', error)
    ElMessage.error(error.message || '업로드에 실패했습니다.')
  } finally {
    uploading.value = false
  }
}

async function removeFile(item) {
  if (props.disabled || uploading.value) return
  try {
    if (item.id != null) {
      await fileApi.deleteFile(item.id)
    }
    files.value = files.value.filter((f) => f.id !== item.id)
    emit('remove', item)
  } catch (error) {
    ElMessage.error(error.message || '삭제에 실패했습니다.')
  }
}

async function download(item) {
  if (item.id == null) return
  try {
    await fileApi.downloadFile(item.id, item.originalName)
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
</script>

<template>
  <div class="file-attachment" :class="{ 'is-disabled': disabled }">
    <div
      class="file-attachment__drop"
      :class="{ 'is-dragging': dragging, 'is-busy': uploading, 'is-full': !canAdd }"
      @click="openPicker"
      @dragover="onDragOver"
      @dragleave="onDragLeave"
      @drop="onDrop"
    >
      <el-icon class="file-attachment__icon" :size="36"><UploadFilled /></el-icon>
      <p class="file-attachment__title">
        {{ uploading ? '업로드 중…' : '파일을 여기로 드래그하거나 클릭하세요' }}
      </p>
      <p class="file-attachment__tip">{{ tipText }}</p>
      <input
        ref="inputRef"
        class="file-attachment__input"
        type="file"
        :accept="accept || undefined"
        :multiple="multiple && (limit <= 0 || limit > 1)"
        :disabled="!canAdd || uploading"
        @change="onInputChange"
      />
    </div>

    <ul v-if="files.length" class="file-attachment__list">
      <li v-for="item in files" :key="item.id ?? item.originalName" class="file-attachment__item">
        <el-icon class="file-attachment__doc"><Document /></el-icon>
        <div class="file-attachment__meta">
          <span class="file-attachment__name" :title="item.originalName">{{ item.originalName }}</span>
          <span class="file-attachment__size">{{ formatSize(item.sizeBytes) }}</span>
        </div>
        <div class="file-attachment__actions">
          <el-button
            v-if="item.id != null"
            link
            type="primary"
            :icon="Download"
            @click.stop="download(item)"
          />
          <el-button
            link
            type="danger"
            :icon="Delete"
            :disabled="disabled"
            @click.stop="removeFile(item)"
          />
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.file-attachment {
  width: 100%;
}

.file-attachment__drop {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 140px;
  padding: 20px 16px;
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
  cursor: pointer;
  transition: border-color 0.15s ease, background 0.15s ease;
}

.file-attachment__drop:hover:not(.is-full):not(.is-busy),
.file-attachment__drop.is-dragging {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.file-attachment__drop.is-full,
.file-attachment.is-disabled .file-attachment__drop {
  cursor: not-allowed;
  opacity: 0.65;
}

.file-attachment__icon {
  color: var(--el-color-primary);
}

.file-attachment__title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.file-attachment__tip {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.file-attachment__input {
  display: none;
}

.file-attachment__list {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
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

.file-attachment__size {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.file-attachment__actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}
</style>
