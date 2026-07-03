import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { getErrorMessage } from '@/api/client'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!accessToken.value)

  function setSession(token, userData) {
    accessToken.value = token
    user.value = userData
    localStorage.setItem('accessToken', token)
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function clearSession() {
    accessToken.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
  }

  async function register(payload) {
    try {
      const { data } = await authApi.register(payload)
      if (data.success) {
        setSession(data.data.accessToken, data.data.user)
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
        setSession(data.data.accessToken, data.data.user)
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

  function logout() {
    clearSession()
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    register,
    login,
    fetchMe,
    logout,
  }
})
