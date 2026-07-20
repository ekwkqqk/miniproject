<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/features/auth/store'
import { useSettingsStore } from '@/features/settings/store'
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
import { Plus } from '@element-plus/icons-vue'

const authStore = useAuthStore()
const settingsStore = useSettingsStore()
const { canUpdate } = useMenuAuth()
const { isMobile } = useBreakpoint()
const { tCode } = useI18n()

const users = ref([])
const roles = ref([])
const loading = ref(false)
const createVisible = ref(false)
const creating = ref(false)
const editVisible = ref(false)
const saving = ref(false)
const resettingPassword = ref(false)

const createForm = reactive({
  name: '',
  email: '',
  password: '',
  enabled: true,
})

const editForm = reactive({
  id: null,
  name: '',
  email: '',
  enabled: true,
  roleIds: [],
})

const editingSelf = computed(() => editForm.email === authStore.user?.email)

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

function openCreate() {
  createForm.name = ''
  createForm.email = ''
  createForm.password = ''
  createForm.enabled = true
  createVisible.value = true
}

function openEdit(user) {
  if (!canUpdate.value) return
  editForm.id = user.id
  editForm.name = user.name
  editForm.email = user.email
  editForm.enabled = user.enabled
  editForm.roleIds = user.roles?.map((role) => role.id) || []
  editVisible.value = true
}

async function handleEditSave() {
  if (!editForm.name.trim()) {
    ElMessage.warning('이름을 입력해주세요.')
    return
  }
  if (!editForm.roleIds.length) {
    ElMessage.warning('Role을 하나 이상 선택해주세요.')
    return
  }

  saving.value = true
  try {
    const { data } = await adminApi.updateUser(editForm.id, {
      name: editForm.name.trim(),
      enabled: editForm.enabled,
      roleIds: editForm.roleIds,
    })
    if (data.success) {
      const index = users.value.findIndex((user) => user.id === editForm.id)
      if (index >= 0) users.value[index] = data.data
      if (editingSelf.value) {
        await authStore.fetchMe()
      }
      ElMessage.success('사용자 정보가 수정되었습니다.')
      editVisible.value = false
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    saving.value = false
  }
}

async function handleResetPassword() {
  if (!editForm.id) return
  try {
    await confirmDialog(
      '설정된 기본 비밀번호로 초기화합니다. 기존 세션은 만료됩니다. 계속할까요?',
      '비밀번호 초기화',
      { type: 'warning', confirmButtonText: '초기화', cancelButtonText: '취소' },
    )
  } catch {
    return
  }

  resettingPassword.value = true
  try {
    const { data } = await adminApi.resetUserPassword(editForm.id)
    if (data.success) {
      ElMessage.success(data.message || '비밀번호가 초기화되었습니다.')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    resettingPassword.value = false
  }
}

async function handleCreate() {
  if (!createForm.name.trim()) {
    ElMessage.warning('이름을 입력해주세요.')
    return
  }
  if (!createForm.email.trim()) {
    ElMessage.warning('이메일을 입력해주세요.')
    return
  }
  if (!createForm.password) {
    ElMessage.warning('비밀번호를 입력해주세요.')
    return
  }
  if (createForm.password.length < settingsStore.passwordMinLength) {
    ElMessage.warning(`비밀번호는 ${settingsStore.passwordMinLength}자 이상이어야 합니다.`)
    return
  }

  creating.value = true
  try {
    const { data } = await adminApi.createUser({
      name: createForm.name.trim(),
      email: createForm.email.trim(),
      password: createForm.password,
      enabled: createForm.enabled,
    })
    if (data.success) {
      ElMessage.success('사용자가 추가되었습니다.')
      createVisible.value = false
      await load()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    creating.value = false
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

async function handleEnabledChange(user, enabled) {
  const previous = user.enabled
  user.enabled = enabled
  try {
    const { data } = await adminApi.updateUserEnabled(user.id, enabled)
    if (data.success) {
      user.enabled = data.data.enabled
      ElMessage.success(enabled ? '계정이 활성화되었습니다.' : '계정이 비활성화되었습니다.')
    }
  } catch (error) {
    user.enabled = previous
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

onMounted(() => {
  settingsStore.loadPublicSettings()
  load()
})
</script>

<template>
  <PageLayout
    title="사용자 관리"
    subtitle="사용자 조회·추가 및 계정 활성/비활성 관리"
    :count="users.length"
    count-label="총 {n}명"
  >
    <template #actions>
      <el-button
        v-if="canUpdate"
        type="primary"
        :icon="Plus"
        @click="openCreate"
      >
        사용자 추가
      </el-button>
    </template>

    <ContentPanel title="사용자 목록" :loading="loading" :show-header="false">
      <div v-if="isMobile" class="mobile-list">
        <article v-for="row in users" :key="row.id" class="mobile-card">
          <div class="mobile-card__head">
            <div>
              <button
                v-if="canUpdate"
                type="button"
                class="name-button"
                @click="openEdit(row)"
              >
                {{ row.name }}
              </button>
              <strong v-else>{{ row.name }}</strong>
              <div class="email">{{ row.email }}</div>
            </div>
            <el-switch
              :model-value="row.enabled"
              :disabled="!canUpdate || row.email === authStore.user?.email"
              inline-prompt
              active-text="활성"
              inactive-text="중지"
              @change="(value) => handleEnabledChange(row, value)"
            />
          </div>
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
          <div class="meta">가입일 {{ formatDateTime(row.createdAt) }}</div>
        </article>
        <el-empty v-if="!users.length && !loading" description="사용자가 없습니다." />
      </div>

      <div v-else class="table-scroll">
        <el-table :data="users" stripe border style="width: 100%">
          <el-table-column prop="id" :label="tCode('table', 'id')" width="80" />
          <el-table-column prop="name" :label="tCode('table', 'name')" width="140">
            <template #default="{ row }">
              <el-button v-if="canUpdate" link type="primary" @click="openEdit(row)">
                {{ row.name }}
              </el-button>
              <span v-else>{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="email" :label="tCode('table', 'email')" min-width="200" />
          <el-table-column :label="tCode('table', 'status')" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
                {{ row.enabled ? '활성' : '비활성' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="tCode('table', 'activeToggle')" width="140" align="center">
            <template #default="{ row }">
              <el-switch
                :model-value="row.enabled"
                :disabled="!canUpdate || row.email === authStore.user?.email"
                @change="(value) => handleEnabledChange(row, value)"
              />
            </template>
          </el-table-column>
          <el-table-column :label="tCode('table', 'role')" min-width="260">
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
          <el-table-column :label="tCode('table', 'createdAt')" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createdAt) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentPanel>

    <ResponsiveDialog v-model="createVisible" title="사용자 추가" :width="480">
      <el-form label-position="top" @submit.prevent="handleCreate">
        <el-form-item label="이름" required>
          <el-input v-model="createForm.name" placeholder="이름" maxlength="100" />
        </el-form-item>
        <el-form-item label="이메일" required>
          <el-input v-model="createForm.email" type="email" placeholder="email@example.com" />
        </el-form-item>
        <el-form-item :label="`비밀번호 (${settingsStore.passwordMinLength}자 이상)`" required>
          <el-input
            v-model="createForm.password"
            type="password"
            show-password
            :placeholder="`${settingsStore.passwordMinLength}자 이상`"
          />
        </el-form-item>
        <el-form-item label="활성화 여부">
          <el-switch
            v-model="createForm.enabled"
            inline-prompt
            active-text="활성"
            inactive-text="비활성"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">취소</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">추가</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog v-model="editVisible" title="사용자 수정" :width="520">
      <el-form label-position="top" @submit.prevent="handleEditSave">
        <el-form-item label="이름" required>
          <el-input v-model="editForm.name" placeholder="이름" maxlength="100" />
        </el-form-item>
        <el-form-item label="이메일">
          <el-input v-model="editForm.email" readonly />
        </el-form-item>
        <el-form-item label="활성화 여부">
          <el-switch
            v-model="editForm.enabled"
            :disabled="editingSelf"
            inline-prompt
            active-text="활성"
            inactive-text="비활성"
          />
          <span v-if="editingSelf" class="form-hint">본인 계정의 상태는 변경할 수 없습니다.</span>
        </el-form-item>
        <el-form-item label="Role" required>
          <el-select
            v-model="editForm.roleIds"
            multiple
            style="width: 100%"
            :disabled="editingSelf"
            placeholder="Role을 선택하세요"
          >
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="`${role.name} (${role.code})`"
              :value="role.id"
            />
          </el-select>
          <span v-if="editingSelf" class="form-hint">본인의 Role은 변경할 수 없습니다.</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="edit-footer">
          <el-button
            v-if="canUpdate"
            type="warning"
            plain
            :loading="resettingPassword"
            :disabled="saving"
            @click="handleResetPassword"
          >
            비밀번호 초기화
          </el-button>
          <div class="edit-footer__right">
            <el-button :disabled="saving || resettingPassword" @click="editVisible = false">취소</el-button>
            <el-button type="primary" :loading="saving" :disabled="resettingPassword" @click="handleEditSave">저장</el-button>
          </div>
        </div>
      </template>
    </ResponsiveDialog>
  </PageLayout>
</template>

<style scoped>
.name-button {
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--el-color-primary);
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.email {
  margin-top: 2px;
  color: #909399;
  font-size: 12px;
}

.meta {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.form-hint {
  display: block;
  width: 100%;
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.edit-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  width: 100%;
}

.edit-footer__right {
  display: flex;
  gap: 8px;
  margin-left: auto;
}
</style>
