<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store'
import { useSettingsStore } from '@/features/settings/store'
import { useI18n } from '@/features/i18n/useI18n'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const settingsStore = useSettingsStore()
const { tCode } = useI18n()
const loading = ref(false)

const expired = computed(() => route.query.reason === 'expired')
const minLength = computed(() => settingsStore.passwordMinLength)

const form = reactive({
  email: '',
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: '',
})

const errors = reactive({
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: '',
})

const passwordsMismatch = computed(() => {
  if (!form.newPasswordConfirm) return false
  return form.newPassword !== form.newPasswordConfirm
})

watch(
  () => form.currentPassword,
  () => {
    errors.currentPassword = ''
  },
)

watch(
  () => [form.newPassword, form.newPasswordConfirm],
  () => {
    errors.newPassword = ''
    if (passwordsMismatch.value) {
      errors.newPasswordConfirm = '새 비밀번호가 일치하지 않습니다.'
    } else {
      errors.newPasswordConfirm = ''
    }
  },
)

onMounted(() => {
  settingsStore.loadPublicSettings()
  form.email = String(route.query.email || authStore.user?.email || '')
})

async function handleSubmit() {
  errors.currentPassword = ''
  errors.newPassword = ''
  errors.newPasswordConfirm = ''

  if (form.newPassword !== form.newPasswordConfirm) {
    errors.newPasswordConfirm = '새 비밀번호가 일치하지 않습니다.'
    return
  }
  if (form.newPassword.length < minLength.value) {
    errors.newPassword = `새 비밀번호는 ${minLength.value}자 이상이어야 합니다.`
    return
  }

  loading.value = true
  try {
    await authStore.changePassword({
      email: form.email,
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
      newPasswordConfirm: form.newPasswordConfirm,
    })
    await authStore.logout()
    router.push({
      name: 'login',
      query: { email: form.email, changed: '1' },
    })
  } catch (error) {
    if (error.errorCode === 'INVALID_CURRENT_PASSWORD') {
      errors.currentPassword = '현재 비밀번호가 틀렸습니다.'
      return
    }
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h2>{{ tCode('common', 'changePassword') }}</h2>
      <el-alert
        v-if="expired"
        type="warning"
        :closable="false"
        show-icon
        title="비밀번호 사용 기간이 만료되었습니다."
        description="현재 비밀번호로 새 비밀번호를 설정한 뒤 다시 로그인해 주세요."
        class="alert"
      />
      <el-form label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="이메일">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="email@example.com"
            :disabled="!!authStore.user"
          />
        </el-form-item>
        <el-form-item label="현재 비밀번호" :error="errors.currentPassword">
          <el-input
            v-model="form.currentPassword"
            type="password"
            show-password
            placeholder="현재 비밀번호"
            :class="{ 'is-error-input': !!errors.currentPassword }"
          />
        </el-form-item>
        <el-form-item :label="`새 비밀번호 (${minLength}자 이상)`" :error="errors.newPassword">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            :placeholder="`${minLength}자 이상`"
          />
        </el-form-item>
        <el-form-item label="새 비밀번호 확인" :error="errors.newPasswordConfirm">
          <el-input
            v-model="form.newPasswordConfirm"
            type="password"
            show-password
            placeholder="새 비밀번호 재입력"
            :class="{ 'is-error-input': !!errors.newPasswordConfirm }"
          />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          {{ tCode('common', 'changePassword') }}
        </el-button>
      </el-form>
      <p class="link">
        <router-link to="/login">로그인으로 돌아가기</router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

.alert {
  margin-bottom: 16px;
}

.link {
  margin-top: 16px;
  text-align: center;
}

:deep(.el-form-item__error) {
  color: #f56c6c;
  font-size: 12px;
  line-height: 1.4;
  padding-top: 4px;
}
</style>
