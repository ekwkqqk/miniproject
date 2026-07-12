<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/features/auth/store'
import * as adminApi from '@/features/admin/api'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const { canUpdate } = useMenuAuth()
const users = ref([])
const roles = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const [userRes, roleRes] = await Promise.all([adminApi.getUsers(), adminApi.getRoles()])
    if (userRes.data.success) users.value = userRes.data.data
    if (roleRes.data.success) roles.value = roleRes.data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

async function handleRolesChange(user, roleIds) {
  try {
    const { data } = await adminApi.updateUserRoles(user.id, roleIds)
    if (data.success) {
      user.roles = data.data.roles
      ElMessage.success('사용자 Role이 변경되었습니다.')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
    await load()
  }
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <span>사용자 Role 관리</span>
    </template>

    <el-table v-loading="loading" :data="users" style="width: 100%">
      <el-table-column prop="name" label="이름" width="140" />
      <el-table-column prop="email" label="이메일" />
      <el-table-column label="Role">
        <template #default="{ row }">
          <el-select
            :model-value="row.roles?.map((r) => r.id) || []"
            multiple
            style="width: 100%"
            :disabled="!canUpdate || row.email === authStore.user?.email"
            @change="(value) => handleRolesChange(row, value)"
          >
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="가입일" width="180" />
    </el-table>
  </el-card>
</template>
