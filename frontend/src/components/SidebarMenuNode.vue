<script setup>
defineProps({
  menu: {
    type: Object,
    required: true,
  },
})
</script>

<template>
  <el-sub-menu v-if="menu.folder || menu.children?.length" :index="`folder-${menu.id}`">
    <template #title>
      <span>{{ menu.name }}</span>
    </template>
    <SidebarMenuNode
      v-for="child in menu.children"
      :key="child.id"
      :menu="child"
    />
  </el-sub-menu>
  <el-menu-item v-else-if="menu.url" :index="menu.url">
    <span>{{ menu.name }}</span>
  </el-menu-item>
</template>
