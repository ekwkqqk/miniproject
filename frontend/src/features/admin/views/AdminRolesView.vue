<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/features/auth/store'
import * as adminApi from '@/features/admin/api'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { useBreakpoint } from '@/shared/composables/useBreakpoint'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import { formatDateTime } from '@/shared/utils/date'
import { ElMessage } from 'element-plus'
import { confirmDialog } from '@/shared/utils/dialog'

const authStore = useAuthStore()
const { canUpdate, canDelete } = useMenuAuth()
const { isMobile } = useBreakpoint()
const { tCode } = useI18n()
const roles = ref([])
const users = ref([])
const selectedRoleId = ref(null)
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)
const userPickerVisible = ref(false)
const userSearchKeyword = ref('')
const selectedUserIds = ref([])
const addingUsers = ref(false)
const form = ref({ code: '', name: '', description: '' })

const selectedRole = computed(() =>
  roles.value.find((role) => role.id === selectedRoleId.value) || null
)

const selectedRoleUsers = computed(() => {
  if (!selectedRole.value) return []
  return users.value.filter((user) =>
    user.roles?.some((role) => role.id === selectedRole.value.id)
  )
})

const availableUsers = computed(() => {
  if (!selectedRole.value) return []
  const keyword = userSearchKeyword.value.trim().toLowerCase()

  return users.value.filter((user) => {
    const alreadyAssigned = user.roles?.some((role) => role.id === selectedRole.value.id)
    const isCurrentUser = user.email === authStore.user?.email
    const matchesKeyword = !keyword
      || user.name?.toLowerCase().includes(keyword)
      || user.email?.toLowerCase().includes(keyword)
    return !alreadyAssigned && !isCurrentUser && matchesKeyword
  })
})

async function load(preferredRoleId = selectedRoleId.value) {
  loading.value = true
  try {
    const [roleRes, userRes] = await Promise.all([
      adminApi.getRoles(),
      adminApi.getUsers(),
    ])
    if (roleRes.data.success) roles.value = roleRes.data.data
    if (userRes.data.success) users.value = userRes.data.data

    const preferredExists = roles.value.some((role) => role.id === preferredRoleId)
    selectedRoleId.value = preferredExists
      ? preferredRoleId
      : (roles.value[0]?.id ?? null)
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

function openUserPicker() {
  if (!selectedRole.value) return
  userSearchKeyword.value = ''
  selectedUserIds.value = []
  userPickerVisible.value = true
}

function handleUserSelectionChange(selection) {
  selectedUserIds.value = selection.map((user) => user.id)
}

async function handleAddUsers() {
  if (!selectedRole.value || !selectedUserIds.value.length) {
    ElMessage.warning('추가할 사용자를 선택해주세요.')
    return
  }

  const roleId = selectedRole.value.id
  const targets = users.value.filter((user) => selectedUserIds.value.includes(user.id))
  addingUsers.value = true

  try {
    const results = await Promise.allSettled(
      targets.map((user) => {
        const roleIds = new Set(user.roles?.map((role) => role.id) || [])
        roleIds.add(roleId)
        return adminApi.updateUserRoles(user.id, [...roleIds])
      })
    )
    const successCount = results.filter((result) => result.status === 'fulfilled').length
    const failureCount = results.length - successCount

    if (successCount) {
      ElMessage.success(`${successCount}명의 사용자를 추가했습니다.`)
      userPickerVisible.value = false
    }
    if (failureCount) {
      const firstError = results.find((result) => result.status === 'rejected')?.reason
      ElMessage.error(
        `${failureCount}명 추가에 실패했습니다. ${
          firstError?.response?.data?.message || firstError?.message || ''
        }`.trim()
      )
    }
    await load(roleId)
  } finally {
    addingUsers.value = false
  }
}

async function handleCreate() {
  creating.value = true
  try {
    const { data } = await adminApi.createRole(form.value)
    if (data.success) {
      ElMessage.success('Role이 생성되었습니다.')
      dialogVisible.value = false
      await load(data.data.id)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    creating.value = false
  }
}

async function handleDelete(role) {
  try {
    await confirmDialog(`Role "${role.name}"을(를) 삭제할까요?`, '확인', { type: 'warning' })
    const { data } = await adminApi.deleteRole(role.id)
    if (data.success) {
      ElMessage.success('Role이 삭제되었습니다.')
      await load(role.id === selectedRoleId.value ? null : selectedRoleId.value)
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
  <PageLayout title="Role 관리" subtitle="Role 상세 정보와 소속 사용자를 관리합니다." :count="roles.length">
    <template #actions>
      <el-button v-if="canUpdate" type="primary" @click="openCreate">Role 생성</el-button>
    </template>

    <div v-loading="loading" class="role-admin">
      <ContentPanel title="Role 목록" :count="roles.length" class="role-list-panel">
        <div v-if="roles.length" class="role-list">
          <button
            v-for="role in roles"
            :key="role.id"
            type="button"
            class="role-list-item"
            :class="{ 'is-active': role.id === selectedRoleId }"
            @click="selectedRoleId = role.id"
          >
            <span class="role-list-item__name">{{ role.name }}</span>
            <span class="role-list-item__code">{{ role.code }}</span>
          </button>
        </div>
        <el-empty v-else-if="!loading" description="등록된 Role이 없습니다." />
      </ContentPanel>

      <div class="role-detail-column">
        <ContentPanel title="Role 내용" class="role-detail-panel">
          <template v-if="selectedRole">
            <div class="detail-head">
              <div>
                <h3>{{ selectedRole.name }}</h3>
                <el-tag effect="plain">{{ selectedRole.code }}</el-tag>
              </div>
              <el-button
                v-if="canDelete"
                type="danger"
                plain
                @click="handleDelete(selectedRole)"
              >
                삭제
              </el-button>
            </div>

            <el-descriptions :column="isMobile ? 1 : 2" border class="role-descriptions">
              <el-descriptions-item label="ID">{{ selectedRole.id }}</el-descriptions-item>
              <el-descriptions-item :label="tCode('table', 'code')">
                {{ selectedRole.code }}
              </el-descriptions-item>
              <el-descriptions-item :label="tCode('table', 'name')">
                {{ selectedRole.name }}
              </el-descriptions-item>
              <el-descriptions-item :label="tCode('table', 'description')" :span="isMobile ? 1 : 2">
                {{ selectedRole.description || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </template>
          <el-empty v-else description="왼쪽 목록에서 Role을 선택하세요." />
        </ContentPanel>

        <ContentPanel
          title="Role 사용자"
          :count="selectedRole ? selectedRoleUsers.length : null"
          count-label="총 {n}명"
          class="role-users-panel"
        >
          <template #header-actions>
            <el-button
              v-if="canUpdate && selectedRole"
              type="primary"
              size="small"
              @click="openUserPicker"
            >
              사용자 추가
            </el-button>
          </template>

          <template v-if="selectedRole">
            <div v-if="isMobile" class="mobile-user-list">
              <article v-for="user in selectedRoleUsers" :key="user.id" class="mobile-user-card">
                <div>
                  <strong>{{ user.name }}</strong>
                  <div class="user-email">{{ user.email }}</div>
                </div>
                <el-tag :type="user.enabled ? 'success' : 'info'" size="small">
                  {{ user.enabled ? '활성' : '비활성' }}
                </el-tag>
              </article>
            </div>

            <div v-else class="table-scroll">
              <el-table :data="selectedRoleUsers" stripe border style="width: 100%">
                <el-table-column prop="id" :label="tCode('table', 'id')" width="80" />
                <el-table-column prop="name" :label="tCode('table', 'name')" min-width="140" />
                <el-table-column prop="email" :label="tCode('table', 'email')" min-width="220" />
                <el-table-column :label="tCode('table', 'status')" width="100" align="center">
                  <template #default="{ row }">
                    <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                      {{ row.enabled ? '활성' : '비활성' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="tCode('table', 'createdAt')" width="180">
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty
              v-if="!selectedRoleUsers.length && !loading"
              description="이 Role을 가진 사용자가 없습니다."
            />
          </template>
          <el-empty v-else description="Role을 선택하면 사용자 목록이 표시됩니다." />
        </ContentPanel>
      </div>
    </div>

    <ResponsiveDialog v-model="dialogVisible" title="Role 생성" :width="480">
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
        <el-button type="primary" :loading="creating" @click="handleCreate">저장</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog
      v-model="userPickerVisible"
      title="사용자 조회"
      :width="760"
      destroy-on-close
    >
      <div class="user-picker">
        <el-input
          v-model="userSearchKeyword"
          clearable
          placeholder="이름 또는 이메일로 검색"
        />

        <div class="user-picker__summary">
          <span>{{ selectedRole?.name }} Role에 추가할 사용자를 선택하세요.</span>
          <el-tag type="info" effect="plain">{{ selectedUserIds.length }}명 선택</el-tag>
        </div>

        <div class="table-scroll">
          <el-table
            :data="availableUsers"
            row-key="id"
            stripe
            border
            max-height="420"
            style="width: 100%"
            @selection-change="handleUserSelectionChange"
          >
            <el-table-column type="selection" width="50" :reserve-selection="true" />
            <el-table-column prop="name" :label="tCode('table', 'name')" min-width="130" />
            <el-table-column prop="email" :label="tCode('table', 'email')" min-width="220" />
            <el-table-column :label="tCode('table', 'status')" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                  {{ row.enabled ? '활성' : '비활성' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty
          v-if="!availableUsers.length"
          description="추가할 수 있는 사용자가 없습니다."
        />
      </div>

      <template #footer>
        <el-button :disabled="addingUsers" @click="userPickerVisible = false">취소</el-button>
        <el-button
          type="primary"
          :loading="addingUsers"
          :disabled="!selectedUserIds.length"
          @click="handleAddUsers"
        >
          확인
        </el-button>
      </template>
    </ResponsiveDialog>
  </PageLayout>
</template>

<style scoped>
.role-admin {
  display: grid;
  grid-template-columns: minmax(220px, 280px) minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.role-list-panel,
.role-detail-column {
  margin-top: 0;
  align-self: start;
}

.role-list-panel :deep(.el-card__header),
.role-detail-panel :deep(.el-card__header) {
  min-height: 56px;
  box-sizing: border-box;
}

.role-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.role-list-item {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  color: #303133;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s, background-color 0.2s;
}

.role-list-item:hover {
  border-color: var(--el-color-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.role-list-item.is-active {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  box-shadow: inset 3px 0 0 var(--el-color-primary);
}

.role-list-item__name,
.role-list-item__code {
  display: block;
}

.role-list-item__name {
  font-weight: 600;
}

.role-list-item__code {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.role-detail-column {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 16px;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.detail-head h3 {
  margin: 0 0 8px;
  font-size: 20px;
}

.role-descriptions {
  width: 100%;
}

.mobile-user-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-user-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
}

.user-email {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  overflow-wrap: anywhere;
}

.user-picker {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-picker__summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #606266;
  font-size: 13px;
}

@media (max-width: 767px) {
  .role-admin {
    grid-template-columns: 1fr;
  }

  .user-picker__summary {
    align-items: flex-start;
  }
}
</style>
