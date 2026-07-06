<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import * as adminApi from '@/api/admin'
import { getRoleLabel, ROLES } from '@/utils/roles'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const users = ref([])
const loading = ref(false)

const roleOptions = [
  { value: ROLES.USER, label: getRoleLabel(ROLES.USER) },
  { value: ROLES.SPECIAL_USER, label: getRoleLabel(ROLES.SPECIAL_USER) },
  { value: ROLES.SYSTEM_ADMIN, label: getRoleLabel(ROLES.SYSTEM_ADMIN) },
]

async function loadUsers() {
  loading.value = true
  try {
    const { data } = await adminApi.getUsers()
    if (data.success) {
      users.value = data.data
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

async function handleRoleChange(user, role) {
  try {
    const { data } = await adminApi.updateUserRole(user.id, role)
    if (data.success) {
      user.role = data.data.role
      user.roleLabel = data.data.roleLabel
      ElMessage.success('권한 등급이 변경되었습니다.')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
    await loadUsers()
  }
}

onMounted(loadUsers)
</script>

<template>
  <el-card>
    <template #header>
      <span>사용자 권한 관리</span>
    </template>

    <el-table v-loading="loading" :data="users" style="width: 100%">
      <el-table-column prop="name" label="이름" />
      <el-table-column prop="email" label="이메일" />
      <el-table-column label="권한 등급" width="200">
        <template #default="{ row }">
          <el-select
            :model-value="row.role"
            :disabled="row.email === authStore.user?.email"
            @change="(value) => handleRoleChange(row, value)"
          >
            <el-option
              v-for="option in roleOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="가입일" />
    </el-table>
  </el-card>
</template>
