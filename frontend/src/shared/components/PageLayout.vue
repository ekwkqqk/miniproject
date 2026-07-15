<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  /** 총 건수 등. null이면 표시 안 함 */
  count: { type: [Number, String], default: null },
  countLabel: { type: String, default: '총 {n}건' },
})

function formatCount(count, label) {
  const text = String(label || '총 {n}건')
  return text.includes('{n}') ? text.replace('{n}', String(count)) : `${text} ${count}`
}
</script>

<template>
  <div class="page-shell page-layout">
    <header class="page-toolbar page-layout__toolbar">
      <div class="page-layout__heading">
        <h1 class="page-toolbar__title">{{ title }}</h1>
        <p v-if="subtitle" class="page-layout__subtitle">{{ subtitle }}</p>
      </div>
      <div class="page-toolbar__actions page-layout__actions">
        <span v-if="count !== null && count !== undefined" class="page-layout__count">
          {{ formatCount(count, countLabel) }}
        </span>
        <slot name="actions" />
      </div>
    </header>

    <div class="page-layout__body">
      <slot />
    </div>
  </div>
</template>
