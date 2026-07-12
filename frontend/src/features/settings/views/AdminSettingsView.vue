<script setup>
import { onMounted, ref } from 'vue'
import * as settingsApi from '@/features/settings/api'
import * as adminApi from '@/features/admin/api'
import { useSettingsStore } from '@/features/settings/store'
import { useMenuAuth } from '@/features/menu/useMenuAuth'
import { ElMessage } from 'element-plus'

const { canUpdate } = useMenuAuth()
const settingsStore = useSettingsStore()

const loading = ref(false)
const saving = ref(false)
const roles = ref([])
const form = ref({
  themePrimaryColor: '#409EFF',
  passwordChangePeriodDays: 0,
  passwordMinLength: 6,
  defaultRoleCodes: ['USER'],
  allowMultiLogin: true,
})

const presetColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#1a5f4a', '#6c5ce7']

async function load() {
  loading.value = true
  try {
    const [settingsRes, rolesRes] = await Promise.all([
      settingsApi.getSettings(),
      adminApi.getRoles(),
    ])
    if (settingsRes.data.success) {
      form.value = {
        ...settingsRes.data.data,
        defaultRoleCodes: settingsRes.data.data.defaultRoleCodes?.length
          ? [...settingsRes.data.data.defaultRoleCodes]
          : ['USER'],
      }
      settingsStore.setThemeFromAdmin(form.value.themePrimaryColor)
    }
    if (rolesRes.data.success) roles.value = rolesRes.data.data
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!canUpdate.value) return
  if (!form.value.defaultRoleCodes?.length) {
    ElMessage.warning('초기 Role을 하나 이상 선택해주세요.')
    return
  }
  saving.value = true
  try {
    const { data } = await settingsApi.updateSettings(form.value)
    if (data.success) {
      form.value = {
        ...data.data,
        defaultRoleCodes: [...(data.data.defaultRoleCodes || [])],
      }
      settingsStore.setThemeFromAdmin(form.value.themePrimaryColor)
      ElMessage.success('시스템 설정이 저장되었습니다.')
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || error.message)
  } finally {
    saving.value = false
  }
}

function onThemeInput(color) {
  form.value.themePrimaryColor = color
  settingsStore.setThemeFromAdmin(color)
}

onMounted(load)
</script>

<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="header">
        <span>시스템 설정</span>
        <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSave">저장</el-button>
      </div>
    </template>

    <el-form label-position="top" class="settings-form" :disabled="!canUpdate">
      <el-divider content-position="left">테마</el-divider>
      <el-form-item label="테마 색상 (Primary)">
        <div class="theme-row">
          <el-color-picker
            :model-value="form.themePrimaryColor"
            @update:model-value="onThemeInput"
          />
          <el-input
            v-model="form.themePrimaryColor"
            style="width: 140px"
            @change="onThemeInput(form.themePrimaryColor)"
          />
          <div class="presets">
            <button
              v-for="color in presetColors"
              :key="color"
              type="button"
              class="preset"
              :style="{ background: color }"
              :title="color"
              @click="onThemeInput(color)"
            />
          </div>
        </div>
        <div class="hint">Element Plus primary 색상에 반영됩니다.</div>
      </el-form-item>

      <el-divider content-position="left">비밀번호</el-divider>
      <el-form-item label="비밀번호 변경 주기 (일)">
        <el-input-number v-model="form.passwordChangePeriodDays" :min="0" :max="3650" />
        <div class="hint">0이면 주기 만료를 검사하지 않습니다. 설정 시 로그인 시점에 만료를 확인합니다.</div>
      </el-form-item>
      <el-form-item label="비밀번호 최소 길이">
        <el-input-number v-model="form.passwordMinLength" :min="4" :max="128" />
      </el-form-item>

      <el-divider content-position="left">회원가입</el-divider>
      <el-form-item label="초기 Role">
        <el-checkbox-group v-model="form.defaultRoleCodes" class="role-checks">
          <el-checkbox
            v-for="role in roles"
            :key="role.id"
            :label="role.code"
            :value="role.code"
          >
            {{ role.name }} ({{ role.code }})
          </el-checkbox>
        </el-checkbox-group>
        <div class="hint">회원가입 시 선택한 Role이 모두 부여됩니다. 하나 이상 선택해야 합니다.</div>
      </el-form-item>

      <el-divider content-position="left">로그인</el-divider>
      <el-form-item label="멀티 로그인">
        <el-switch
          v-model="form.allowMultiLogin"
          active-text="허용"
          inactive-text="단일 세션만"
        />
        <div class="hint">
          끄면 새 로그인 시 기존 refresh 세션을 모두 폐기합니다. (한 계정 동시 접속 제한)
        </div>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.settings-form {
  max-width: min(720px, 100%);
}

.theme-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.presets {
  display: flex;
  gap: 6px;
}

.preset {
  width: 22px;
  height: 22px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  padding: 0;
}

.role-checks {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.hint {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
