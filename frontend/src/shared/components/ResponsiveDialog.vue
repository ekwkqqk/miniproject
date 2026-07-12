<script setup>
import { computed } from 'vue'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  /** Desktop preferred width in px */
  width: { type: [Number, String], default: 520 },
  destroyOnClose: { type: Boolean, default: false },
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
