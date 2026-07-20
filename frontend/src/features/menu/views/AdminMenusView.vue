<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import * as adminApi from '@/features/admin/api'
import * as i18nAdminApi from '@/features/i18n/adminApi'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { useMenuStore } from '@/features/menu/store'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import { ElMessage } from 'element-plus'
import { confirmDialog } from '@/shared/utils/dialog'

const { canUpdate, canDelete } = useMenuAuth()
const menuStore = useMenuStore()
const { tCode } = useI18n()
const menus = ref([])
const roles = ref([])
const i18nMessages = ref([])
const loading = ref(false)
const saving = ref(false)
const i18nPickerVisible = ref(false)
const i18nSearch = ref('')
const rolePickerVisible = ref(false)
const roleSearch = ref('')
const draftRoleIds = ref([])
const treeRef = ref(null)

const selectedKey = ref(null)
const mode = ref('idle') // idle | edit | create

const emptyButtons = () => ({
  canRead: true,
  canUpdate: false,
  canDelete: false,
  canUpload: false,
  canDownload: false,
  canOther: false,
})

const emptyForm = () => ({
  parentId: null,
  name: '',
  nameI18nKey: '',
  url: '',
  enabled: true,
  roleIds: [],
  roleButtons: {},
})

const form = ref(emptyForm())

const isFolder = computed(() => !form.value.url?.trim())
const showDetail = computed(() => mode.value === 'edit' || mode.value === 'create')
const detailTitle = computed(() => {
  if (mode.value === 'create') return '새 메뉴 등록'
  if (mode.value === 'edit') return `메뉴 수정 — ${form.value.name || ''}`
  return '메뉴 정보'
})

const i18nOptions = computed(() =>
  i18nMessages.value.map((m) => {
    const key = `${m.groupCode}.${m.code}`
    const preview = m.texts?.ko || m.texts?.en || m.description || ''
    return {
      key,
      groupCode: m.groupCode,
      code: m.code,
      description: m.description || '',
      preview,
      label: preview ? `${key} — ${preview}` : key,
    }
  })
)

const filteredI18nOptions = computed(() => {
  const q = i18nSearch.value.trim().toLowerCase()
  if (!q) return i18nOptions.value
  return i18nOptions.value.filter((o) =>
    [o.key, o.groupCode, o.code, o.description, o.preview]
      .join(' ')
      .toLowerCase()
      .includes(q)
  )
})

const selectedI18nLabel = computed(() => {
  if (!form.value.nameI18nKey) return ''
  const option = i18nOptions.value.find((o) => o.key === form.value.nameI18nKey)
  return option?.label || form.value.nameI18nKey
})

const selectedRolesLabel = computed(() => {
  if (!form.value.roleIds.length) return ''
  return form.value.roleIds
    .map((id) => roles.value.find((r) => r.id === id)?.name || id)
    .join(', ')
})

const filteredRoles = computed(() => {
  const q = roleSearch.value.trim().toLowerCase()
  if (!q) return roles.value
  return roles.value.filter((r) =>
    [r.code, r.name, r.description || ''].join(' ').toLowerCase().includes(q)
  )
})

const rolePermissionRows = computed(() =>
  form.value.roleIds.map((roleId) => ({
    roleId,
    roleName: roles.value.find((r) => r.id === roleId)?.name || roleId,
  }))
)

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

const parentOptions = computed(() => {
  const excludeId = mode.value === 'edit' ? selectedKey.value : null
  return flatMenus.value.filter((menu) => menu.id !== excludeId && menu.folder)
})

const treeProps = {
  children: 'children',
  label: 'name',
}

function findMenuById(nodes, id) {
  for (const node of nodes) {
    if (node.id === id) return node
    if (node.children?.length) {
      const found = findMenuById(node.children, id)
      if (found) return found
    }
  }
  return null
}

async function load(preferSelectId) {
  loading.value = true
  try {
    const [menuRes, roleRes, msgRes] = await Promise.all([
      adminApi.getMenus(),
      adminApi.getRoles(),
      i18nAdminApi.getMessages(),
    ])
    if (menuRes.data.success) menus.value = menuRes.data.data
    if (roleRes.data.success) roles.value = roleRes.data.data
    if (msgRes.data.success) i18nMessages.value = msgRes.data.data

    await nextTick()
    const targetId = preferSelectId ?? (mode.value === 'edit' ? selectedKey.value : null)
    if (targetId && findMenuById(menus.value, targetId)) {
      selectMenu(findMenuById(menus.value, targetId))
      treeRef.value?.setCurrentKey(targetId)
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function selectMenu(menu) {
  if (!menu) return
  mode.value = 'edit'
  selectedKey.value = menu.id
  const roleButtons = {}
  menu.roleButtons?.forEach((item) => {
    roleButtons[item.roleId] = { ...item.buttons }
  })
  form.value = {
    parentId: menu.parentId ?? null,
    name: menu.name,
    nameI18nKey: menu.nameI18nKey || '',
    url: menu.url || '',
    enabled: menu.enabled !== false,
    roleIds: menu.roles?.map((r) => r.id) || [],
    roleButtons,
  }
}

function onTreeNodeClick(data) {
  selectMenu(data)
}

function startCreate(parentId = null) {
  if (!canUpdate.value) return
  mode.value = 'create'
  selectedKey.value = null
  treeRef.value?.setCurrentKey(null)
  form.value = {
    ...emptyForm(),
    parentId,
  }
}

function startCreateRoot() {
  startCreate(null)
}

function startCreateChild() {
  if (mode.value !== 'edit' || !selectedKey.value) {
    ElMessage.warning('하위를 추가할 상위 메뉴를 먼저 선택하세요.')
    return
  }
  const current = findMenuById(menus.value, selectedKey.value)
  if (current && !current.folder) {
    ElMessage.warning('화면 메뉴 아래에는 하위를 추가할 수 없습니다. 폴더를 선택하세요.')
    return
  }
  startCreate(selectedKey.value)
}

function cancelCreate() {
  mode.value = 'idle'
  form.value = emptyForm()
  selectedKey.value = null
}

function openI18nPicker() {
  i18nSearch.value = ''
  i18nPickerVisible.value = true
}

function clearI18nKey() {
  form.value.nameI18nKey = ''
}

function selectI18nKey(row) {
  form.value.nameI18nKey = row.key
  if (row.preview) {
    form.value.name = row.preview
  }
  i18nPickerVisible.value = false
}

function openRolePicker() {
  draftRoleIds.value = [...form.value.roleIds]
  roleSearch.value = ''
  rolePickerVisible.value = true
}

function clearRoles() {
  form.value.roleIds = []
  form.value.roleButtons = {}
}

function isDraftRoleChecked(roleId) {
  return draftRoleIds.value.includes(roleId)
}

function toggleDraftRole(roleId, checked) {
  if (checked) {
    if (!draftRoleIds.value.includes(roleId)) {
      draftRoleIds.value = [...draftRoleIds.value, roleId]
    }
  } else {
    draftRoleIds.value = draftRoleIds.value.filter((id) => id !== roleId)
  }
}

function applyRolePicker() {
  const nextIds = [...draftRoleIds.value]
  const nextButtons = { ...form.value.roleButtons }
  nextIds.forEach((roleId) => {
    if (!nextButtons[roleId]) nextButtons[roleId] = emptyButtons()
  })
  Object.keys(nextButtons).forEach((key) => {
    if (!nextIds.includes(Number(key))) delete nextButtons[key]
  })
  form.value.roleIds = nextIds
  form.value.roleButtons = nextButtons
  rolePickerVisible.value = false
}

function buildPayload() {
  const folder = !form.value.url?.trim()
  return {
    parentId: form.value.parentId,
    name: form.value.name,
    nameI18nKey: form.value.nameI18nKey || null,
    url: folder ? '' : form.value.url,
    enabled: form.value.enabled,
    roleIds: folder ? [] : form.value.roleIds,
    roleButtons: folder
      ? []
      : form.value.roleIds.map((roleId) => ({
          roleId,
          ...(form.value.roleButtons[roleId] || emptyButtons()),
        })),
  }
}

async function refreshSidebarMenus() {
  try {
    await menuStore.fetchMyMenus()
  } catch {
    // ignore
  }
}

async function handleSave() {
  if (!canUpdate.value) return
  saving.value = true
  try {
    const payload = buildPayload()
    const isCreate = mode.value === 'create'
    const { data } = isCreate
      ? await adminApi.createMenu(payload)
      : await adminApi.updateMenu(selectedKey.value, payload)
    if (data.success) {
      ElMessage.success(isCreate ? '메뉴가 등록되었습니다.' : '메뉴가 수정되었습니다.')
      const savedId = data.data?.id
      mode.value = 'edit'
      await load(savedId)
      await refreshSidebarMenus()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!canDelete.value || mode.value !== 'edit' || !selectedKey.value) return
  const name = form.value.name
  try {
    await confirmDialog(`메뉴 "${name}"을(를) 삭제할까요?`, '확인', { type: 'warning' })
    const { data } = await adminApi.deleteMenu(selectedKey.value)
    if (data.success) {
      ElMessage.success('메뉴가 삭제되었습니다.')
      mode.value = 'idle'
      selectedKey.value = null
      form.value = emptyForm()
      await load()
      await refreshSidebarMenus()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message)
    }
  }
}

function allowDrag() {
  return canUpdate.value
}

function allowDrop(draggingNode, dropNode, type) {
  if (!canUpdate.value) return false
  if (type === 'inner') {
    return !!dropNode.data.folder
  }
  // before/after: 루트이거나 부모가 폴더면 OK (형제로 배치)
  return true
}

async function onNodeDrop(draggingNode, dropNode, dropType) {
  if (!canUpdate.value) return

  let parentId = null
  let siblings = []

  if (dropType === 'inner') {
    parentId = dropNode.data.id
    siblings = dropNode.data.children || []
  } else {
    const parentNode = dropNode.parent
    if (!parentNode || parentNode.level === 0) {
      parentId = null
      siblings = menus.value
    } else {
      parentId = parentNode.data.id
      siblings = parentNode.data.children || []
    }
  }

  const orderedSiblingIds = siblings.map((item) => item.id)
  try {
    const { data } = await adminApi.reorderMenus({
      menuId: draggingNode.data.id,
      parentId,
      orderedSiblingIds,
    })
    if (data.success) {
      menus.value = data.data || []
      await nextTick()
      treeRef.value?.setCurrentKey(draggingNode.data.id)
      if (mode.value === 'edit' && selectedKey.value === draggingNode.data.id) {
        const updated = findMenuById(menus.value, draggingNode.data.id)
        if (updated) selectMenu(updated)
      }
      await refreshSidebarMenus()
      ElMessage.success('메뉴 순서가 변경되었습니다.')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
    await load(selectedKey.value)
  }
}

function indentLabel(menu) {
  return `${'— '.repeat(menu.depth || 0)}${menu.name}`
}

onMounted(load)
</script>

<template>
  <PageLayout title="메뉴 관리" subtitle="메뉴 트리 구성 및 Role·버튼 권한">
    <div v-loading="loading" class="menu-admin">
      <aside class="tree-panel">
        <div class="tree-toolbar">
          <span class="panel-title">메뉴 트리</span>
          <div class="tree-actions">
            <el-button v-if="canUpdate" size="small" @click="startCreateRoot">최상위 추가</el-button>
            <el-button
              v-if="canUpdate"
              size="small"
              :disabled="mode !== 'edit'"
              @click="startCreateChild"
            >
              하위 추가
            </el-button>
          </div>
        </div>
        <p v-if="canUpdate" class="tree-hint">드래그 앤 드롭으로 순서·상위 메뉴를 변경할 수 있습니다.</p>

        <el-tree
          ref="treeRef"
          :data="menus"
          :props="treeProps"
          node-key="id"
          highlight-current
          default-expand-all
          :expand-on-click-node="false"
          :draggable="canUpdate"
          :allow-drag="allowDrag"
          :allow-drop="allowDrop"
          class="menu-tree"
          @node-click="onTreeNodeClick"
          @node-drop="onNodeDrop"
        >
          <template #default="{ data }">
            <span class="tree-node" :class="{ 'tree-node--disabled': !data.enabled }">
              <el-tag :type="data.folder ? 'info' : 'success'" size="small" effect="plain">
                {{ data.folder ? '폴더' : '화면' }}
              </el-tag>
              <span class="tree-node-name">{{ data.name }}</span>
              <el-tag v-if="!data.enabled" type="danger" size="small" effect="plain">
                미사용
              </el-tag>
            </span>
          </template>
        </el-tree>

        <p v-if="!menus.length && !loading" class="empty-tree">등록된 메뉴가 없습니다.</p>
      </aside>

      <section class="detail-panel">
        <div class="detail-toolbar">
          <span class="panel-title">{{ detailTitle }}</span>
          <div v-if="showDetail" class="detail-actions">
            <el-button v-if="mode === 'create'" @click="cancelCreate">취소</el-button>
            <el-button
              v-if="mode === 'edit' && canDelete"
              type="danger"
              plain
              @click="handleDelete"
            >
              삭제
            </el-button>
            <el-button
              v-if="canUpdate"
              type="primary"
              :loading="saving"
              @click="handleSave"
            >
              저장
            </el-button>
          </div>
        </div>

        <div v-if="!showDetail" class="detail-empty">
          <p>왼쪽 트리에서 메뉴를 선택하거나, 새 메뉴를 추가하세요.</p>
        </div>

        <el-form v-else label-position="top" class="detail-form">
          <el-form-item label="상위 메뉴">
            <el-select v-model="form.parentId" clearable placeholder="최상위" style="width: 100%" :disabled="!canUpdate">
              <el-option
                v-for="menu in parentOptions"
                :key="menu.id"
                :label="indentLabel(menu)"
                :value="menu.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="다국어 메뉴명">
            <div class="picker-field">
              <el-input
                :model-value="selectedI18nLabel"
                readonly
                placeholder="다국어 메시지를 검색해 선택하세요"
              />
              <el-button :disabled="!canUpdate" @click="openI18nPicker">찾기</el-button>
              <el-button :disabled="!canUpdate || !form.nameI18nKey" @click="clearI18nKey">지우기</el-button>
            </div>
          </el-form-item>

          <el-form-item label="기본 메뉴명 (fallback)">
            <el-input
              v-model="form.name"
              :disabled="!canUpdate"
              placeholder="다국어가 없을 때 표시할 이름"
            />
          </el-form-item>

          <el-form-item label="URL (폴더는 비워두세요)">
            <el-input
              v-model="form.url"
              :disabled="!canUpdate"
              placeholder="/example 또는 비움(폴더)"
            />
          </el-form-item>

          <el-form-item label="사용 여부">
            <el-switch
              v-model="form.enabled"
              :disabled="!canUpdate"
              inline-prompt
              active-text="사용"
              inactive-text="미사용"
            />
            <span class="field-hint">
              미사용 메뉴와 그 하위 메뉴는 사용자 사이드바에 표시되지 않습니다.
            </span>
          </el-form-item>

          <template v-if="!isFolder">
            <el-form-item label="접근 Role">
              <div class="picker-field">
                <el-input
                  :model-value="selectedRolesLabel"
                  readonly
                  placeholder="Role을 검색해 선택하세요"
                />
                <el-button :disabled="!canUpdate" @click="openRolePicker">찾기</el-button>
                <el-button :disabled="!canUpdate || !form.roleIds.length" @click="clearRoles">지우기</el-button>
              </div>
            </el-form-item>

            <el-form-item v-if="rolePermissionRows.length" label="버튼 권한">
              <el-table :data="rolePermissionRows" border size="small" style="width: 100%">
                <el-table-column prop="roleName" :label="tCode('table', 'role')" min-width="140" />
                <el-table-column :label="tCode('table', 'canRead')" width="72" align="center">
                  <template #default="{ row }">
                    <el-checkbox
                      v-if="form.roleButtons[row.roleId]"
                      v-model="form.roleButtons[row.roleId].canRead"
                      :disabled="!canUpdate"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="tCode('table', 'canUpdate')" width="72" align="center">
                  <template #default="{ row }">
                    <el-checkbox
                      v-if="form.roleButtons[row.roleId]"
                      v-model="form.roleButtons[row.roleId].canUpdate"
                      :disabled="!canUpdate"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="tCode('table', 'canDelete')" width="72" align="center">
                  <template #default="{ row }">
                    <el-checkbox
                      v-if="form.roleButtons[row.roleId]"
                      v-model="form.roleButtons[row.roleId].canDelete"
                      :disabled="!canUpdate"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="tCode('table', 'canUpload')" width="80" align="center">
                  <template #default="{ row }">
                    <el-checkbox
                      v-if="form.roleButtons[row.roleId]"
                      v-model="form.roleButtons[row.roleId].canUpload"
                      :disabled="!canUpdate"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="tCode('table', 'canDownload')" width="88" align="center">
                  <template #default="{ row }">
                    <el-checkbox
                      v-if="form.roleButtons[row.roleId]"
                      v-model="form.roleButtons[row.roleId].canDownload"
                      :disabled="!canUpdate"
                    />
                  </template>
                </el-table-column>
                <el-table-column :label="tCode('table', 'canOther')" width="72" align="center">
                  <template #default="{ row }">
                    <el-checkbox
                      v-if="form.roleButtons[row.roleId]"
                      v-model="form.roleButtons[row.roleId].canOther"
                      :disabled="!canUpdate"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </template>
          <el-alert
            v-else
            type="info"
            :closable="false"
            title="폴더 메뉴는 URL·Role·버튼 권한이 없습니다. 하위 화면 메뉴에만 권한을 지정합니다."
          />
        </el-form>
      </section>

      <ResponsiveDialog
        v-model="i18nPickerVisible"
        title="다국어 메시지 찾기"
        :width="720"
      >
        <el-input
          v-model="i18nSearch"
          clearable
          placeholder="키, 그룹, 코드, 번역 내용으로 검색"
          style="margin-bottom: 12px"
        />
        <el-table
          :data="filteredI18nOptions"
          height="360"
          highlight-current-row
          style="width: 100%"
          @row-click="selectI18nKey"
        >
          <el-table-column prop="groupCode" :label="tCode('table', 'group')" width="120" />
          <el-table-column prop="code" :label="tCode('table', 'code')" width="140" />
          <el-table-column prop="key" :label="tCode('table', 'key')" width="180" />
          <el-table-column prop="preview" :label="tCode('table', 'preview')" min-width="180" show-overflow-tooltip />
          <el-table-column :label="tCode('table', 'select')" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="selectI18nKey(row)">선택</el-button>
            </template>
          </el-table-column>
        </el-table>
        <template #footer>
          <el-button @click="i18nPickerVisible = false">닫기</el-button>
        </template>
      </ResponsiveDialog>

      <ResponsiveDialog
        v-model="rolePickerVisible"
        title="접근 Role 선택"
        :width="640"
      >
        <el-input
          v-model="roleSearch"
          clearable
          placeholder="코드, 이름, 설명으로 검색"
          style="margin-bottom: 12px"
        />
        <el-table :data="filteredRoles" height="360" style="width: 100%">
          <el-table-column label="" width="56" align="center">
            <template #default="{ row }">
              <el-checkbox
                :model-value="isDraftRoleChecked(row.id)"
                @change="(checked) => toggleDraftRole(row.id, checked)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="code" :label="tCode('table', 'code')" width="140" />
          <el-table-column prop="name" :label="tCode('table', 'name')" min-width="140" />
          <el-table-column prop="description" :label="tCode('table', 'description')" min-width="160" show-overflow-tooltip />
        </el-table>
        <template #footer>
          <el-button @click="rolePickerVisible = false">취소</el-button>
          <el-button type="primary" @click="applyRolePicker">적용</el-button>
        </template>
      </ResponsiveDialog>
    </div>
  </PageLayout>
</template>

<style scoped>
.menu-admin {
  display: grid;
  grid-template-columns: minmax(260px, 320px) 1fr;
  gap: 16px;
  min-height: calc(100vh - 140px);
  align-items: stretch;
}

.tree-panel,
.detail-panel {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  min-height: 520px;
}

.tree-toolbar,
.detail-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.panel-title {
  font-weight: 600;
  color: #303133;
}

.tree-actions,
.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}

.tree-hint {
  margin: 0;
  padding: 8px 16px 0;
  font-size: 12px;
  color: #909399;
}

.menu-tree {
  padding: 12px;
  flex: 1;
  overflow: auto;
  background: transparent;
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding-right: 8px;
}

.tree-node-name {
  font-size: 14px;
}

.tree-node--disabled .tree-node-name {
  color: #909399;
  text-decoration: line-through;
}

.field-hint {
  display: block;
  width: 100%;
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
  line-height: 1.4;
}

.empty-tree,
.detail-empty {
  padding: 24px 16px;
  color: #909399;
  font-size: 14px;
}

.detail-form {
  padding: 16px;
  overflow: auto;
  flex: 1;
}

.picker-field {
  display: flex;
  gap: 8px;
  width: 100%;
}

.picker-field .el-input {
  flex: 1;
}

@media (max-width: 900px) {
  .menu-admin {
    grid-template-columns: 1fr;
  }

  .tree-panel {
    min-height: 280px;
  }
}
</style>
