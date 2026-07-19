<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store'
import { useSettingsStore } from '@/features/settings/store'
import { useI18n } from '@/features/i18n/useI18n'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const settingsStore = useSettingsStore()
const { tCode } = useI18n()
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
})

onMounted(() => {
  if (route.query.email) {
    form.email = String(route.query.email)
  }
  if (route.query.changed === '1') {
    ElMessage.success('비밀번호가 변경되었습니다. 새 비밀번호로 로그인해 주세요.')
    router.replace({ name: 'login', query: { email: form.email || undefined } })
  }
})

function resolvePostLoginTarget() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return redirect
  }
  return { name: 'dashboard' }
}

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success('로그인되었습니다.')
    router.push(resolvePostLoginTarget())
  } catch (error) {
    if (error.errorCode === 'PASSWORD_EXPIRED') {
      ElMessage.warning(error.message)
      router.push({
        name: 'change-password',
        query: { email: form.email, reason: 'expired' },
      })
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
    <el-button
      class="theme-toggle"
      circle
      :aria-label="settingsStore.isDark ? '라이트 모드' : '다크 모드'"
      :title="settingsStore.isDark ? '라이트 모드' : '다크 모드'"
      @click="settingsStore.toggleDarkMode()"
    >
      <el-icon :size="18">
        <Sunny v-if="settingsStore.isDark" />
        <Moon v-else />
      </el-icon>
    </el-button>
    <el-card class="auth-card">
      <h2>로그인</h2>
      <el-form label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="이메일">
          <el-input v-model="form.email" type="email" placeholder="email@example.com" />
        </el-form-item>
        <el-form-item label="비밀번호">
          <el-input v-model="form.password" type="password" show-password placeholder="비밀번호" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          로그인
        </el-button>
      </el-form>
      <p class="link">
        비밀번호를 변경하시겠어요?
        <router-link to="/change-password">{{ tCode('common', 'changePassword') }}</router-link>
      </p>
      <p class="link">
        계정이 없으신가요?
        <router-link to="/register">회원가입</router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-page-bg, #f5f7fa);
}

.theme-toggle {
  position: absolute;
  top: 16px;
  right: 16px;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

.link {
  margin-top: 12px;
  text-align: center;
}
</style>
