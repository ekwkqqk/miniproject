import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { getErrorMessage } from '@/api/client'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!refreshToken.value)

  function setSession(tokens, userData) {
    accessToken.value = tokens.accessToken
    refreshToken.value = tokens.refreshToken
    user.value = userData
    localStorage.setItem('accessToken', tokens.accessToken)
    localStorage.setItem('refreshToken', tokens.refreshToken)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function clearSession() {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  async function register(payload) {
    try {
      const { data } = await authApi.register(payload)
      if (data.success) {
        setSession(data.data, data.data.user)
      }
      return data
    } catch (error) {
      throw new Error(getErrorMessage(error))
    }
  }

  async function login(payload) {
    try {
      const { data } = await authApi.login(payload)
      if (data.success) {
        setSession(data.data, data.data.user)
      }
      return data
    } catch (error) {
      throw new Error(getErrorMessage(error))
    }
  }

  async function fetchMe() {
    try {
      const { data } = await authApi.getMe()
      if (data.success) {
        user.value = data.data
        localStorage.setItem('user', JSON.stringify(data.data))
      }
      return data
    } catch (error) {
      clearSession()
      throw new Error(getErrorMessage(error))
    }
  }

  async function logout() {
    const token = refreshToken.value || localStorage.getItem('refreshToken')
    if (token) {
      try {
        await authApi.logout(token)
      } catch {
        // 서버 폐기 실패해도 클라이언트 세션은 제거
      }
    }
    clearSession()
  }

  return {
    accessToken,
    refreshToken,
    user,
    isAuthenticated,
    register,
    login,
    fetchMe,
    logout,
  }
})
