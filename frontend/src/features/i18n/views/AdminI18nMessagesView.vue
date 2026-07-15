<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import * as i18nAdminApi from '@/features/i18n/adminApi'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import SearchPanel from '@/shared/components/SearchPanel.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const { canUpdate, canDelete } = useMenuAuth()
const { tCode } = useI18n()
const locales = ref([])
const groups = ref([])
const messages = ref([])
const loading = ref(false)
const filterGroup = ref('')
const dialogVisible = ref(false)
const editingId = ref(null)
const form = ref({
  groupCode: '',
  code: '',
  description: '',
  texts: {},
})

const previewParams = ref('{"name":"홍길동","0":"10","1":"3"}')
const previewLocale = ref('ko')
const previewText = computed(() => {
  if (!dialogVisible.value) return ''
  const template = form.value.texts[previewLocale.value] || ''
  let params = {}
  try {
    params = JSON.parse(previewParams.value || '{}')
  } catch {
    return '(파라미터 JSON 오류)'
  }
  return template.replace(/\{([^{}]+)}/g, (m, key) =>
    Object.prototype.hasOwnProperty.call(params, key) && params[key] != null ? String(params[key]) : m
  )
})

async function loadMeta() {
  const [localeRes, groupRes] = await Promise.all([
    i18nAdminApi.getLocales(),
    i18nAdminApi.getGroups(),
  ])
  if (localeRes.data.success) locales.value = localeRes.data.data
  if (groupRes.data.success) groups.value = groupRes.data.data
  if (!previewLocale.value && locales.value.length) {
    previewLocale.value = locales.value[0].code
  }
}

async function loadMessages() {
  loading.value = true
  try {
    const { data } = await i18nAdminApi.getMessages(filterGroup.value || undefined)
    if (data.success) messages.value = data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function emptyTexts() {
  const texts = {}
  for (const locale of locales.value) {
    texts[locale.code] = ''
  }
  return texts
}

function openCreate() {
  editingId.value = null
  form.value = {
    groupCode: filterGroup.value || (groups.value[0]?.code ?? ''),
    code: '',
    description: '',
    texts: emptyTexts(),
  }
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  const texts = emptyTexts()
  Object.assign(texts, row.texts || {})
  form.value = {
    groupCode: row.groupCode,
    code: row.code,
    description: row.description || '',
    texts,
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const payload = { ...form.value }
    const { data } = editingId.value
      ? await i18nAdminApi.updateMessage(editingId.value, payload)
      : await i18nAdminApi.createMessage(payload)
    if (data.success) {
      ElMessage.success(editingId.value ? '메시지가 수정되었습니다.' : '메시지가 등록되었습니다.')
      dialogVisible.value = false
      await loadMessages()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`메시지 "${row.groupCode}.${row.code}"을(를) 삭제할까요?`, '확인', {
      type: 'warning',
    })
    const { data } = await i18nAdminApi.deleteMessage(row.id)
    if (data.success) {
      ElMessage.success('메시지가 삭제되었습니다.')
      await loadMessages()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message)
    }
  }
}

function textSummary(row) {
  return locales.value
    .map((l) => `${l.code}: ${(row.texts?.[l.code] || '').slice(0, 40)}`)
    .join(' / ')
}

function handleReset() {
  filterGroup.value = ''
}

watch(filterGroup, loadMessages)

onMounted(async () => {
  await loadMeta()
  await loadMessages()
})
</script>

<template>
  <PageLayout title="메시지 관리" subtitle="다국어 메시지 코드·번역 관리" :count="messages.length">
    <template #actions>
      <el-button v-if="canUpdate" type="primary" @click="openCreate">메시지 등록</el-button>
    </template>

    <SearchPanel title="검색 조건" @search="loadMessages" @reset="handleReset">
      <el-form-item label="그룹" class="filter-item">
        <el-select v-model="filterGroup" clearable placeholder="전체 그룹" style="width: 100%">
          <el-option v-for="g in groups" :key="g.code" :label="`${g.name} (${g.code})`" :value="g.code" />
        </el-select>
      </el-form-item>
    </SearchPanel>

    <ContentPanel title="메시지 목록" :loading="loading">
      <p class="hint">
        파라미터는 <code>{name}</code> 또는 <code>{0}</code>, <code>{1}</code> 형식으로 작성합니다.
      </p>
      <div class="table-scroll">
        <el-table :data="messages" stripe border style="width: 100%">
          <el-table-column prop="groupCode" :label="tCode('table', 'group')" width="120" />
          <el-table-column prop="code" :label="tCode('table', 'code')" width="160" />
          <el-table-column prop="description" :label="tCode('table', 'description')" width="160" />
          <el-table-column :label="tCode('table', 'translation')" min-width="280">
            <template #default="{ row }">
              <span class="summary">{{ textSummary(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="canUpdate || canDelete" :label="tCode('table', 'manage')" width="160">
            <template #default="{ row }">
              <el-button v-if="canUpdate" type="primary" link @click="openEdit(row)">수정</el-button>
              <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">삭제</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentPanel>

    <ResponsiveDialog v-model="dialogVisible" :title="editingId ? '메시지 수정' : '메시지 등록'" :width="640">
      <el-form label-position="top">
        <el-form-item label="그룹">
          <el-select v-model="form.groupCode" :disabled="!!editingId" style="width: 100%">
            <el-option v-for="g in groups" :key="g.code" :label="`${g.name} (${g.code})`" :value="g.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="메시지 코드">
          <el-input v-model="form.code" :disabled="!!editingId" placeholder="예: welcome" />
        </el-form-item>
        <el-form-item label="설명">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-form-item v-for="locale in locales" :key="locale.code" :label="`${locale.name} (${locale.code})`">
          <el-input
            v-model="form.texts[locale.code]"
            type="textarea"
            :rows="2"
            placeholder="예: 안녕하세요, {name}님!"
          />
        </el-form-item>
        <el-divider>미리보기</el-divider>
        <el-form-item label="미리보기 로케일">
          <el-select v-model="previewLocale" style="width: 160px">
            <el-option v-for="l in locales" :key="l.code" :label="l.code" :value="l.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="파라미터 JSON">
          <el-input v-model="previewParams" type="textarea" :rows="2" />
        </el-form-item>
        <el-alert :title="previewText || '(텍스트 없음)'" type="info" :closable="false" />
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">취소</el-button>
        <el-button type="primary" @click="handleSave">저장</el-button>
      </template>
    </ResponsiveDialog>
  </PageLayout>
</template>

<style scoped>
.hint {
  margin: 0 0 12px;
  color: #666;
  font-size: 13px;
}

.summary {
  font-size: 12px;
  color: #555;
}
</style>
