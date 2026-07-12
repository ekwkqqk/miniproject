<script setup>
import { onMounted, ref } from 'vue'
import * as adminApi from '@/api/admin'
import { useMenuAuth } from '@/composables/useMenuAuth'
import { ElMessage, ElMessageBox } from 'element-plus'

const { canUpdate, canDelete } = useMenuAuth()
const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = ref({ code: '', name: '', description: '' })

async function loadRoles() {
  loading.value = true
  try {
    const { data } = await adminApi.getRoles()
    if (data.success) roles.value = data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { code: '', name: '', description: '' }
  dialogVisible.value = true
}

async function handleCreate() {
  try {
    const { data } = await adminApi.createRole(form.value)
    if (data.success) {
      ElMessage.success('Role이 생성되었습니다.')
      dialogVisible.value = false
      await loadRoles()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

async function handleDelete(role) {
  try {
    await ElMessageBox.confirm(`Role "${role.name}"을(를) 삭제할까요?`, '확인', { type: 'warning' })
    const { data } = await adminApi.deleteRole(role.id)
    if (data.success) {
      ElMessage.success('Role이 삭제되었습니다.')
      await loadRoles()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message)
    }
  }
}

onMounted(loadRoles)
</script>

<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>Role 관리</span>
        <el-button v-if="canUpdate" type="primary" @click="openCreate">Role 생성</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="roles" style="width: 100%">
      <el-table-column prop="code" label="코드" width="160" />
      <el-table-column prop="name" label="이름" />
      <el-table-column prop="description" label="설명" />
      <el-table-column v-if="canDelete" label="관리" width="120">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleDelete(row)">
            삭제
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="Role 생성" width="480px">
      <el-form label-position="top">
        <el-form-item label="코드">
          <el-input v-model="form.code" placeholder="예: OPERATOR" />
        </el-form-item>
        <el-form-item label="이름">
          <el-input v-model="form.name" placeholder="예: 운영자" />
        </el-form-item>
        <el-form-item label="설명">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">취소</el-button>
        <el-button type="primary" @click="handleCreate">저장</el-button>
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
