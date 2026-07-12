<script setup>
import { computed, onMounted, ref } from 'vue'
import * as adminApi from '@/api/admin'
import { useMenuAuth } from '@/composables/useMenuAuth'
import { ElMessage, ElMessageBox } from 'element-plus'

const { canUpdate, canDelete } = useMenuAuth()
const menus = ref([])
const roles = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)

const emptyButtons = () => ({
  canRead: true,
  canUpdate: false,
  canDelete: false,
  canUpload: false,
  canDownload: false,
  canOther: false,
})

const form = ref({
  parentId: null,
  name: '',
  url: '',
  sortOrder: 1,
  roleIds: [],
  roleButtons: {},
})

const isFolder = computed(() => !form.value.url?.trim())

function flattenMenus(nodes, depth = 0, result = []) {
  nodes.forEach((node) => {
    result.push({ ...node, depth })
    if (node.children?.length) {
      flattenMenus(node.children, depth + 1, result)
    }
  })
  return result
}

const flatMenus = computed(() => flattenMenus(menus.value))

const parentOptions = computed(() =>
  flatMenus.value.filter((menu) => menu.id !== editingId.value)
)

async function load() {
  loading.value = true
  try {
    const [menuRes, roleRes] = await Promise.all([adminApi.getMenus(), adminApi.getRoles()])
    if (menuRes.data.success) menus.value = menuRes.data.data
    if (roleRes.data.success) roles.value = roleRes.data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = {
    parentId: null,
    name: '',
    url: '',
    sortOrder: 1,
    roleIds: [],
    roleButtons: {},
  }
  dialogVisible.value = true
}

function openEdit(menu) {
  editingId.value = menu.id
  const roleButtons = {}
  menu.roleButtons?.forEach((item) => {
    roleButtons[item.roleId] = { ...item.buttons }
  })
  form.value = {
    parentId: menu.parentId ?? null,
    name: menu.name,
    url: menu.url || '',
    sortOrder: menu.sortOrder,
    roleIds: menu.roles?.map((r) => r.id) || [],
    roleButtons,
  }
  dialogVisible.value = true
}

function onRoleIdsChange(roleIds) {
  const next = { ...form.value.roleButtons }
  roleIds.forEach((roleId) => {
    if (!next[roleId]) next[roleId] = emptyButtons()
  })
  Object.keys(next).forEach((key) => {
    if (!roleIds.includes(Number(key))) delete next[key]
  })
  form.value.roleButtons = next
}

function buildPayload() {
  const folder = !form.value.url?.trim()
  return {
    parentId: form.value.parentId,
    name: form.value.name,
    url: folder ? '' : form.value.url,
    sortOrder: form.value.sortOrder,
    roleIds: folder ? [] : form.value.roleIds,
    roleButtons: folder
      ? []
      : form.value.roleIds.map((roleId) => ({
          roleId,
          ...(form.value.roleButtons[roleId] || emptyButtons()),
        })),
  }
}

async function handleSave() {
  try {
    const payload = buildPayload()
    const { data } = editingId.value
      ? await adminApi.updateMenu(editingId.value, payload)
      : await adminApi.createMenu(payload)
    if (data.success) {
      ElMessage.success(editingId.value ? '메뉴가 수정되었습니다.' : '메뉴가 등록되었습니다.')
      dialogVisible.value = false
      await load()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

async function handleDelete(menu) {
  try {
    await ElMessageBox.confirm(`메뉴 "${menu.name}"을(를) 삭제할까요?`, '확인', { type: 'warning' })
    const { data } = await adminApi.deleteMenu(menu.id)
    if (data.success) {
      ElMessage.success('메뉴가 삭제되었습니다.')
      await load()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message)
    }
  }
}

function roleName(roleId) {
  return roles.value.find((r) => r.id === roleId)?.name || roleId
}

function indentLabel(menu) {
  return `${'— '.repeat(menu.depth || 0)}${menu.name}`
}

onMounted(load)
</script>

<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>메뉴 관리</span>
        <el-button v-if="canUpdate" type="primary" @click="openCreate">메뉴 등록</el-button>
      </div>
    </template>

    <el-table
      v-loading="loading"
      :data="menus"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
      style="width: 100%"
    >
      <el-table-column prop="name" label="메뉴명" />
      <el-table-column label="유형" width="90">
        <template #default="{ row }">
          <el-tag :type="row.folder ? 'info' : 'success'" size="small">
            {{ row.folder ? '폴더' : '화면' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="url" label="URL">
        <template #default="{ row }">
          {{ row.url || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="정렬" width="80" />
      <el-table-column label="Role">
        <template #default="{ row }">
          <template v-if="!row.folder">
            <el-tag v-for="role in row.roles" :key="role.id" style="margin: 2px">{{ role.name }}</el-tag>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="관리" width="160">
        <template #default="{ row }">
          <el-button v-if="canUpdate" type="primary" link @click="openEdit(row)">수정</el-button>
          <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">삭제</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '메뉴 수정' : '메뉴 등록'" width="720px">
      <el-form label-position="top">
        <el-form-item label="상위 메뉴">
          <el-select v-model="form.parentId" clearable placeholder="최상위" style="width: 100%">
            <el-option
              v-for="menu in parentOptions"
              :key="menu.id"
              :label="indentLabel(menu)"
              :value="menu.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="메뉴명">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="URL (폴더는 비워두세요)">
          <el-input v-model="form.url" placeholder="/example 또는 비움(폴더)" />
        </el-form-item>
        <el-form-item label="정렬">
          <el-input-number v-model="form.sortOrder" :min="1" />
        </el-form-item>

        <template v-if="!isFolder">
          <el-form-item label="접근 Role">
            <el-select
              v-model="form.roleIds"
              multiple
              style="width: 100%"
              @change="onRoleIdsChange"
            >
              <el-option
                v-for="role in roles"
                :key="role.id"
                :label="role.name"
                :value="role.id"
              />
            </el-select>
          </el-form-item>

          <div v-for="roleId in form.roleIds" :key="roleId" class="button-box">
            <template v-if="form.roleButtons[roleId]">
              <h4>{{ roleName(roleId) }} 버튼 권한</h4>
              <el-checkbox v-model="form.roleButtons[roleId].canRead">조회</el-checkbox>
              <el-checkbox v-model="form.roleButtons[roleId].canUpdate">수정</el-checkbox>
              <el-checkbox v-model="form.roleButtons[roleId].canDelete">삭제</el-checkbox>
              <el-checkbox v-model="form.roleButtons[roleId].canUpload">업로드</el-checkbox>
              <el-checkbox v-model="form.roleButtons[roleId].canDownload">다운로드</el-checkbox>
              <el-checkbox v-model="form.roleButtons[roleId].canOther">기타</el-checkbox>
            </template>
          </div>
        </template>
        <el-alert
          v-else
          type="info"
          :closable="false"
          title="폴더 메뉴는 URL·Role·버튼 권한이 없습니다. 하위 화면 메뉴에만 권한을 지정합니다."
        />
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

.button-box {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.button-box h4 {
  margin: 0 0 8px;
}
</style>
