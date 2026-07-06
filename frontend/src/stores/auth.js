import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { getErrorMessage } from '@/api/client'
import {
  getAccessToken,
  setAuthSession,
  clearAccessToken,
  subscribeAuthListener,
} from '@/api/tokenHolder'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(getAccessToken())
  const user = ref(null)
  const sessionReady = ref(false)

  subscribeAuthListener(({ accessToken: token, user: userData }) => {
    accessToken.value = token
    if (userData !== undefined) {
      user.value = userData
    }
  })

  const isAuthenticated = computed(() => !!accessToken.value)

  function setSession(data) {
    setAuthSession(data.accessToken, data.user)
    accessToken.value = data.accessToken
    user.value = data.user
  }

  function clearSession() {
    clearAccessToken()
    accessToken.value = ''
    user.value = null
  }

  async function restoreSession() {
    if (sessionReady.value) {
      return isAuthenticated.value
    }

    try {
      const { data } = await authApi.refresh()
      if (data.success) {
        setSession(data.data)
      }
    } catch {
      clearSession()
    } finally {
      sessionReady.value = true
    }

    return isAuthenticated.value
  }

  async function register(payload) {
    try {
      const { data } = await authApi.register(payload)
      if (data.success) {
        setSession(data.data)
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
        setSession(data.data)
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
      }
      return data
    } catch (error) {
      clearSession()
      throw new Error(getErrorMessage(error))
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch {
      // 서버 폐기 실패해도 클라이언트 세션은 제거
    }
    clearSession()
    sessionReady.value = true
  }

  return {
    accessToken,
    user,
    sessionReady,
    isAuthenticated,
    restoreSession,
    register,
    login,
    fetchMe,
    logout,
  }
})
