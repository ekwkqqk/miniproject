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

    <div class="filter-panel search-panel__grid">
      <slot />
      <div v-if="showActions || $slots.actions" class="filter-panel__actions search-panel__actions">
        <slot name="actions">
          <el-button type="primary" :icon="Search" :loading="searching" @click="emit('search')">
            {{ searchLabel }}
          </el-button>
          <el-button :icon="RefreshRight" @click="emit('reset')">
            {{ resetLabel }}
          </el-button>
        </slot>
      </div>
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

.search-panel :deep(.filter-item) {
  margin-bottom: 0;
}

.search-panel :deep(.filter-item .el-form-item__label) {
  padding-bottom: 4px;
}
</style>
