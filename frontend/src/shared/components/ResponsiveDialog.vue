<script setup>
import { computed } from 'vue'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  /** Desktop preferred width in px */
  width: { type: [Number, String], default: 520 },
  destroyOnClose: { type: Boolean, default: false },
  /** true면 헤더를 Element Plus primary(테마) 색으로 표시 */
  themedHeader: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'open', 'opened', 'close', 'closed'])

const { isMobile, isTablet } = useBreakpoint()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const dialogWidth = computed(() => {
  if (isMobile.value) return '100%'
  if (isTablet.value) {
    const n = typeof props.width === 'number' ? props.width : parseInt(String(props.width), 10)
    return Number.isFinite(n) ? `${Math.min(n, 720)}px` : '80%'
  }
  return typeof props.width === 'number' ? `${props.width}px` : props.width
})

const dialogClass = computed(() => [
  'responsive-dialog',
  props.themedHeader ? 'responsive-dialog--themed-header' : '',
  isMobile.value ? 'responsive-dialog--sheet' : '',
])
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="title"
    :width="dialogWidth"
    :fullscreen="false"
    :draggable="!isMobile"
    :align-center="!isMobile"
    :destroy-on-close="destroyOnClose"
    append-to-body
    :class="dialogClass"
    @open="emit('open')"
    @opened="emit('opened')"
    @close="emit('close')"
    @closed="emit('closed')"
  >
    <slot />
    <template v-if="$slots.footer" #footer>
      <div class="responsive-dialog__footer">
        <slot name="footer" />
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.responsive-dialog__footer {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
  width: 100%;
}

.responsive-dialog__footer :deep(.el-button) {
  margin: 0 !important;
}

@media (max-width: 767px) {
  .responsive-dialog__footer {
    justify-content: stretch;
  }

  .responsive-dialog__footer :deep(.el-button) {
    flex: 1 1 calc(50% - 4px);
    min-height: 40px;
  }

  .responsive-dialog__footer :deep(.el-button.is-text),
  .responsive-dialog__footer :deep(.el-button.is-link) {
    flex: 1 1 100%;
  }
}
</style>

<!-- teleported dialog: unscoped so header styles apply -->
<style>
.el-dialog.responsive-dialog--themed-header {
  overflow: hidden;
}

.el-dialog.responsive-dialog--themed-header .el-dialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: 0;
  padding: 12px 16px;
  background: var(--el-color-primary);
  border-bottom: none;
}

.el-dialog.responsive-dialog--themed-header .el-dialog__title {
  flex: 1;
  min-width: 0;
  margin: 0;
  padding: 0;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
  line-height: 32px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.el-dialog.responsive-dialog--themed-header .el-dialog__headerbtn {
  position: static !important;
  inset: auto !important;
  top: auto !important;
  right: auto !important;
  width: 32px;
  height: 32px;
  margin: 0;
  padding: 0;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.el-dialog.responsive-dialog--themed-header .el-dialog__headerbtn .el-dialog__close {
  color: #fff;
  font-size: 18px;
}

.el-dialog.responsive-dialog--themed-header .el-dialog__headerbtn:hover .el-dialog__close {
  color: rgba(255, 255, 255, 0.85);
}

@media (max-width: 767px) {
  /*
   * 모바일 sheet 핸들(::before) + 다크모드 배경이 테마 헤더 위에 어두운 띠로 겹침.
   * 외부 핸들을 제거하고 헤더가 상단(둥근 모서리 포함)을 담당한다.
   */
  .el-dialog.responsive-dialog--themed-header::before,
  .el-dialog.responsive-dialog--themed-header.responsive-dialog--sheet::before,
  html.dark .el-dialog.responsive-dialog--themed-header::before {
    display: none !important;
    content: none !important;
    height: 0 !important;
    margin: 0 !important;
    padding: 0 !important;
    background: transparent !important;
  }

  .el-dialog.responsive-dialog--themed-header .el-dialog__header {
    position: relative;
    flex-shrink: 0;
    padding: 22px 16px 12px;
    background: var(--el-color-primary);
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
  }

  .el-dialog.responsive-dialog--themed-header .el-dialog__header::before {
    content: '';
    position: absolute;
    top: 10px;
    left: 50%;
    transform: translateX(-50%);
    width: 40px;
    height: 4px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.55);
    pointer-events: none;
    z-index: 1;
  }

  .el-dialog.responsive-dialog--themed-header .el-dialog__body {
    padding-top: 12px;
  }
}
</style>
