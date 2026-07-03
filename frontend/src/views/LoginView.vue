<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
})

async function handleLogin() {
  loading.value = true
  try {
    await authStore.login(form)
    ElMessage.success('로그인되었습니다.')
    router.push({ name: 'dashboard' })
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
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
        계정이 없으신가요?
        <router-link to="/register">회원가입</router-link>
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
  width: 400px;
}

.link {
  margin-top: 16px;
  text-align: center;
}
</style>
