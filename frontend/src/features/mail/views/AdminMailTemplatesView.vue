<script setup>
import { onMounted, ref } from 'vue'
import * as mailApi from '@/features/mail/api'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { useI18n } from '@/features/i18n/useI18n'
import PageLayout from '@/shared/components/PageLayout.vue'
import ContentPanel from '@/shared/components/ContentPanel.vue'
import ResponsiveDialog from '@/shared/components/ResponsiveDialog.vue'
import { ElMessage } from 'element-plus'
import { confirmDialog } from '@/shared/utils/dialog'

const { canUpdate, canDelete } = useMenuAuth()
const { tCode } = useI18n()
const templates = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const sendVisible = ref(false)
const editingId = ref(null)
const sendTarget = ref(null)

const emptyForm = () => ({
  code: '',
  name: '',
  description: '',
  fromAddress: '',
  fromName: '',
  toAddresses: '',
  ccAddresses: '',
  bccAddresses: '',
  subject: '',
  body: '',
  html: true,
  enabled: true,
})

const form = ref(emptyForm())
const sendForm = ref({
  to: '',
  paramsJson: '{"name":"홍길동"}',
})

async function load() {
  loading.value = true
  try {
    const { data } = await mailApi.getTemplates()
    if (data.success) templates.value = data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    code: row.code,
    name: row.name,
    description: row.description || '',
    fromAddress: row.fromAddress,
    fromName: row.fromName || '',
    toAddresses: row.toAddresses,
    ccAddresses: row.ccAddresses || '',
    bccAddresses: row.bccAddresses || '',
    subject: row.subject,
    body: row.body,
    html: row.html,
    enabled: row.enabled,
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const payload = { ...form.value }
    const { data } = editingId.value
      ? await mailApi.updateTemplate(editingId.value, payload)
      : await mailApi.createTemplate(payload)
    if (data.success) {
      ElMessage.success(editingId.value ? '템플릿이 수정되었습니다.' : '템플릿이 등록되었습니다.')
      dialogVisible.value = false
      await load()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

async function handleDelete(row) {
  try {
    await confirmDialog(`템플릿 "${row.code}"을(를) 삭제할까요?`, '확인', { type: 'warning' })
    const { data } = await mailApi.deleteTemplate(row.id)
    if (data.success) {
      ElMessage.success('템플릿이 삭제되었습니다.')
      await load()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || error.message)
    }
  }
}

function openSend(row) {
  sendTarget.value = row
  sendForm.value = {
    to: row.toAddresses || '',
    paramsJson: '{"name":"홍길동"}',
  }
  sendVisible.value = true
}

async function handleSend() {
  try {
    let params = {}
    try {
      params = JSON.parse(sendForm.value.paramsJson || '{}')
    } catch {
      ElMessage.error('파라미터 JSON 형식이 올바르지 않습니다.')
      return
    }
    const to = sendForm.value.to
      .split(/[,;]/)
      .map((s) => s.trim())
      .filter(Boolean)
    const { data } = await mailApi.sendMail({
      templateCode: sendTarget.value.code,
      to: to.length ? to : undefined,
      params,
    })
    if (data.success) {
      const dry = data.data?.dryRun ? ' (DRY-RUN: 로그 출력)' : ''
      ElMessage.success(`메일 발송 처리 완료${dry}`)
      sendVisible.value = false
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  }
}

onMounted(load)
</script>

<template>
  <PageLayout title="메일 템플릿" subtitle="발송 템플릿 등록·수정·테스트" :count="templates.length">
    <template #actions>
      <el-button v-if="canUpdate" type="primary" @click="openCreate">템플릿 등록</el-button>
    </template>

    <ContentPanel title="템플릿 목록" :loading="loading">
      <p class="hint">
        제목/본문에 <code>{name}</code> 형태 파라미터를 사용할 수 있습니다.
        수신자·참조는 쉼표로 여러 명을 입력하세요. 로컬에서는 DRY-RUN(로그)으로 동작합니다.
      </p>
      <div class="table-scroll">
        <el-table :data="templates" stripe border style="width: 100%">
          <el-table-column prop="code" :label="tCode('table', 'code')" width="140" />
          <el-table-column prop="name" :label="tCode('table', 'name')" width="160" />
          <el-table-column :label="tCode('table', 'from')" min-width="180">
            <template #default="{ row }">
              {{ row.fromName ? `${row.fromName} <${row.fromAddress}>` : row.fromAddress }}
            </template>
          </el-table-column>
          <el-table-column prop="toAddresses" :label="tCode('table', 'to')" min-width="180" show-overflow-tooltip />
          <el-table-column prop="subject" :label="tCode('table', 'subject')" min-width="200" show-overflow-tooltip />
          <el-table-column :label="tCode('table', 'enabled')" width="80">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? 'Y' : 'N' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="tCode('table', 'manage')" width="220" fixed="right">
            <template #default="{ row }">
              <el-button type="success" link @click="openSend(row)">발송</el-button>
              <el-button v-if="canUpdate" type="primary" link @click="openEdit(row)">수정</el-button>
              <el-button v-if="canDelete" type="danger" link @click="handleDelete(row)">삭제</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </ContentPanel>

    <ResponsiveDialog v-model="dialogVisible" :title="editingId ? '템플릿 수정' : '템플릿 등록'" :width="720">
      <el-form label-position="top">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="코드">
              <el-input v-model="form.code" :disabled="!!editingId" placeholder="예: welcome" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="이름">
              <el-input v-model="form.name" placeholder="예: 가입 환영 메일" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="설명">
          <el-input v-model="form.description" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="14">
            <el-form-item label="발신자 이메일">
              <el-input v-model="form.fromAddress" placeholder="noreply@example.com" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="발신자 이름">
              <el-input v-model="form.fromName" placeholder="miniproject" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="수신자 (To)">
          <el-input v-model="form.toAddresses" placeholder="user1@example.com, user2@example.com" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="참조 (Cc)">
              <el-input v-model="form.ccAddresses" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="숨은참조 (Bcc)">
              <el-input v-model="form.bccAddresses" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="제목">
          <el-input v-model="form.subject" placeholder="[알림] {name}님 환영합니다" />
        </el-form-item>
        <el-form-item label="본문">
          <el-input v-model="form.body" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item label="옵션">
          <el-checkbox v-model="form.html">HTML 본문</el-checkbox>
          <el-checkbox v-model="form.enabled" style="margin-left: 16px">사용</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">취소</el-button>
        <el-button type="primary" @click="handleSave">저장</el-button>
      </template>
    </ResponsiveDialog>

    <ResponsiveDialog v-model="sendVisible" title="메일 발송 테스트" :width="520">
      <el-form v-if="sendTarget" label-position="top">
        <el-form-item label="템플릿">
          <el-input :model-value="`${sendTarget.name} (${sendTarget.code})`" disabled />
        </el-form-item>
        <el-form-item label="수신자 (비우면 템플릿 기본값)">
          <el-input v-model="sendForm.to" />
        </el-form-item>
        <el-form-item label="파라미터 JSON">
          <el-input v-model="sendForm.paramsJson" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendVisible = false">취소</el-button>
        <el-button type="primary" @click="handleSend">발송</el-button>
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
</style>
