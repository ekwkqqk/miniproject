<script setup>
import { onMounted, ref } from 'vue'
import * as i18nAdminApi from '@/features/i18n/adminApi'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import { ElMessage } from 'element-plus'
import { confirmDialog } from '@/shared/utils/dialog'

const { canUpdate, canDelete } = useMenuAuth()
const { tCode } = useI18n()
const groups = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({ code: '', name: '', description: '' })

async function load() {
  loading.value = true
  try {
    const { data } = await i18nAdminApi.getGroups()
    if (data.success) groups.value = data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = { code: '', name: '', description: '' }
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = { code: row.code, name: row.name, description: row.description || '' }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const { data } = editingId.value
      ? await i18nAdminApi.updateGroup(editingId.value, form.value)
      : await i18nAdminApi.createGroup(form.value)
    if (data.success) {
      ElMessage.success(editingId.value ? '그룹이 수정되었습니다.' : '그룹이 등록되었습니다.')
      dialogVisible.value = false
      await load()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

async function handleDelete(row) {
  try {
    await confirmDialog(`그룹 "${row.code}"을(를) 삭제할까요? 하위 메시지도 삭제됩니다.`, '확인', {
      type: 'warning',
    })
    const { data } = await i18nAdminApi.deleteGroup(row.id)
    if (data.success) {
      ElMessage.success('그룹이 삭제되었습니다.')
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
  <PageLayout title="메시지 그룹" subtitle="다국어 메시지 그룹 관리" :count="groups.length">
    <template #actions>
      <el-button v-if="canUpdate" type="primary" @click="openCreate">그룹 등록</el-button>
    </template>

    <ContentPanel :loading="loading" :show-header="false">
      <div class="table-scroll">
        <el-table :data="groups" stripe border style="width: 100%">
          <el-table-column prop="code" :label="tCode('table', 'code')" width="160" />
          <el-table-column prop="name" :label="tCode('table', 'name')" />
          <el-table-column prop="description" :label="tCode('table', 'description')" />
          <el-table-column v-if="canUpdate || canDelete" :label="tCode('table', 'manage')" width="160">
            <template #default="{ row }">
              <el-button v-if="canUpdate" type="primary" link @click="openEdit(row)">수정</el-button>
              <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">삭제</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentPanel>

    <ResponsiveDialog v-model="dialogVisible" :title="editingId ? '그룹 수정' : '그룹 등록'" :width="480">
      <el-form label-position="top">
        <el-form-item label="코드">
          <el-input v-model="form.code" :disabled="!!editingId" placeholder="예: common, auth" />
        </el-form-item>
        <el-form-item label="이름">
          <el-input v-model="form.name" placeholder="예: 공통" />
        </el-form-item>
        <el-form-item label="설명">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">취소</el-button>
        <el-button type="primary" @click="handleSave">저장</el-button>
      </template>
    </ResponsiveDialog>
  </PageLayout>
</template>
