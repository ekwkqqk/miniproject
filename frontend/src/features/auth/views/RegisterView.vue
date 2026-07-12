<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)

const form = reactive({
  name: '',
  email: '',
  password: '',
})

async function handleRegister() {
  loading.value = true
  try {
    await authStore.register(form)
    ElMessage.success('회원가입이 완료되었습니다.')
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
      <h2>회원가입</h2>
      <el-form label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="이름">
          <el-input v-model="form.name" placeholder="이름" />
        </el-form-item>
        <el-form-item label="이메일">
          <el-input v-model="form.email" type="email" placeholder="email@example.com" />
        </el-form-item>
        <el-form-item label="비밀번호">
          <el-input v-model="form.password" type="password" show-password placeholder="6자 이상" />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" style="width: 100%">
          회원가입
        </el-button>
      </el-form>
      <p class="link">
        이미 계정이 있으신가요?
        <router-link to="/login">로그인</router-link>
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
