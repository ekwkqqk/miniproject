<script setup>
import { onMounted, ref } from 'vue'
import * as i18nAdminApi from '@/api/i18nAdmin'
import { useMenuAuth } from '@/composables/useMenuAuth'
import { ElMessage, ElMessageBox } from 'element-plus'

const { canUpdate, canDelete } = useMenuAuth()
const locales = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({ code: '', name: '', enabled: true, sortOrder: 0 })

async function load() {
  loading.value = true
  try {
    const { data } = await i18nAdminApi.getLocales()
    if (data.success) locales.value = data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { code: '', name: '', enabled: true, sortOrder: 0 }
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    code: row.code,
    name: row.name,
    enabled: row.enabled,
    sortOrder: row.sortOrder,
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const payload = { ...form.value }
    const { data } = editingId.value
      ? await i18nAdminApi.updateLocale(editingId.value, payload)
      : await i18nAdminApi.createLocale(payload)
    if (data.success) {
      ElMessage.success(editingId.value ? '로케일이 수정되었습니다.' : '로케일이 등록되었습니다.')
      dialogVisible.value = false
      await load()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`로케일 "${row.code}"을(를) 삭제할까요? 관련 번역도 삭제됩니다.`, '확인', {
      type: 'warning',
    })
    const { data } = await i18nAdminApi.deleteLocale(row.id)
    if (data.success) {
      ElMessage.success('로케일이 삭제되었습니다.')
      await load()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message)
    }
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>로케일 관리</span>
        <el-button v-if="canUpdate" type="primary" @click="openCreate">로케일 등록</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="locales" style="width: 100%">
      <el-table-column prop="code" label="코드" width="120" />
      <el-table-column prop="name" label="이름" />
      <el-table-column label="사용" width="100">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? 'Y' : 'N' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="정렬" width="80" />
      <el-table-column v-if="canUpdate || canDelete" label="관리" width="160">
        <template #default="{ row }">
          <el-button v-if="canUpdate" type="primary" link @click="openEdit(row)">수정</el-button>
          <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">삭제</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '로케일 수정' : '로케일 등록'" width="480px">
      <el-form label-position="top">
        <el-form-item label="코드">
          <el-input v-model="form.code" :disabled="!!editingId" placeholder="예: ko, en, ja" />
        </el-form-item>
        <el-form-item label="이름">
          <el-input v-model="form.name" placeholder="예: 한국어" />
        </el-form-item>
        <el-form-item label="정렬">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="사용">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">취소</el-button>
        <el-button type="primary" @click="handleSave">저장</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
