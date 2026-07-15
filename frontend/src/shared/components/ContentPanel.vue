<script setup>
defineProps({
  title: { type: String, default: '' },
  /** 건수 표시. null이면 숨김 */
  count: { type: [Number, String], default: null },
  countLabel: { type: String, default: '{n}건' },
  loading: { type: Boolean, default: false },
  /** false면 헤더 영역 자체를 숨김 (슬롯도 없음) */
  showHeader: { type: Boolean, default: true },
})

function formatCount(count, label) {
  const text = String(label || '{n}건')
  return text.includes('{n}') ? text.replace('{n}', String(count)) : `${text} ${count}`
}
</script>

<template>
  <el-card shadow="never" class="content-panel" v-loading="loading">
    <template v-if="showHeader && (title || count !== null || $slots['header-actions'] || $slots.header)" #header>
      <div class="page-toolbar content-panel__header">
        <slot name="header">
          <div class="content-panel__heading">
            <span v-if="title" class="content-panel__title">{{ title }}</span>
            <span
              v-if="count !== null && count !== undefined"
              class="content-panel__count"
            >
              {{ formatCount(count, countLabel) }}
            </span>
          </div>
        </slot>
        <div v-if="$slots['header-actions']" class="page-toolbar__actions">
          <slot name="header-actions" />
        </div>
      </div>
    </template>

    <div class="content-panel__body">
      <slot />
    </div>

    <div v-if="$slots.footer" class="content-panel__footer">
      <slot name="footer" />
    </div>
  </el-card>
</template>

<style scoped>
.content-panel__header {
  width: 100%;
}

.content-panel__heading {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
}

.content-panel__title {
  font-weight: 600;
}

.content-panel__count {
  color: #606266;
  font-size: 13px;
}

.content-panel__footer {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 4px;
}

.content-panel :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
</style>
