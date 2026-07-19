<script setup>
import { Search, RefreshRight } from '@element-plus/icons-vue'

defineProps({
  title: { type: String, default: '' },
  /** false면 기본 검색/초기화 버튼을 숨기고 #actions만 사용 */
  showActions: { type: Boolean, default: true },
  searching: { type: Boolean, default: false },
  searchLabel: { type: String, default: '검색' },
  resetLabel: { type: String, default: '초기화' },
})

const emit = defineEmits(['search', 'reset'])
</script>

<template>
  <el-card shadow="never" class="search-panel">
    <template v-if="title || $slots['header-extra']" #header>
      <div class="search-panel__header">
        <span v-if="title" class="search-panel__title">{{ title }}</span>
        <slot name="header-extra" />
      </div>
    </template>

    <el-form
      class="search-panel__form"
      label-position="right"
      label-width="72px"
      @submit.prevent="emit('search')"
    >
      <div class="filter-panel search-panel__grid">
        <slot />
      </div>
    </el-form>

    <div
      v-if="showActions || $slots.actions"
      class="search-panel__actions"
    >
      <slot name="actions">
        <el-button :icon="RefreshRight" @click="emit('reset')">
          {{ resetLabel }}
        </el-button>
        <el-button type="primary" :icon="Search" :loading="searching" @click="emit('search')">
          {{ searchLabel }}
        </el-button>
      </slot>
    </div>
  </el-card>
</template>

<style scoped>
.search-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.search-panel__title {
  font-weight: 600;
}

.search-panel__form {
  margin: 0;
}

.search-panel :deep(.filter-panel) {
  align-items: stretch;
}

.search-panel :deep(.filter-item) {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 0;
  width: 100%;
  min-width: 0;
}

.search-panel :deep(.filter-item .el-form-item__label) {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 32px;
  margin: 0;
  padding: 0 10px 0 0;
  line-height: 32px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-regular, #606266);
}

.search-panel :deep(.filter-item .el-form-item__content) {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  min-height: 32px;
  line-height: normal;
}

.search-panel :deep(.filter-item .el-form-item__content > *) {
  width: 100%;
}

.search-panel :deep(.filter-item .el-input),
.search-panel :deep(.filter-item .el-select),
.search-panel :deep(.filter-item .el-date-editor) {
  width: 100%;
}

.search-panel :deep(.filter-item .el-radio-group),
.search-panel :deep(.filter-item .el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px 0;
  min-height: 32px;
}

.search-panel__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
}

@media (max-width: 767px) {
  .search-panel :deep(.filter-item) {
    flex-direction: column;
    align-items: stretch;
  }

  .search-panel :deep(.filter-item .el-form-item__label) {
    justify-content: flex-start;
    width: 100% !important;
    height: 22px;
    margin-bottom: 6px;
    padding: 0;
    line-height: 22px;
    text-align: left;
  }

  .search-panel__actions {
    justify-content: stretch;
  }

  .search-panel__actions :deep(.el-button) {
    flex: 1;
  }
}
</style>
