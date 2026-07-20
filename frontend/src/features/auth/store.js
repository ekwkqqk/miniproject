import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/features/auth/api'
import { getErrorMessage, getErrorCode } from '@/shared/api/client'
import {
  getAccessToken,
  setAuthSession,
  clearAccessToken,
  subscribeAuthListener,
} from '@/features/auth/tokenHolder'
import { isAccessTokenExpired, refreshSession } from '@/features/auth/refreshSession'
import { useMenuStore } from '@/features/menu/store'
import { useApprovalBadgeStore } from '@/features/approval/badgeStore'

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
  const isSystemAdmin = computed(() =>
    user.value?.roles?.some((role) => role.code === 'SYSTEM_ADMIN') ?? false
  )

  function setSession(data) {
    setAuthSession(data.accessToken, data.user)
    accessToken.value = data.accessToken
    user.value = data.user
  }

  function clearSession() {
    clearAccessToken()
    accessToken.value = ''
    user.value = null
    useMenuStore().clearMenus()
    useApprovalBadgeStore().clear()
  }

  async function restoreSession() {
    // access token이 아직 유효하면 그대로 통과
    if (!isAccessTokenExpired(getAccessToken())) {
      sessionReady.value = true
      useApprovalBadgeStore().startPolling()
      return true
    }

    // access 만료(또는 없음) → refresh cookie로 갱신 시도
    // refresh도 만료/없으면 로그인 필요
    try {
      const data = await refreshSession()
      setSession(data)
      await useMenuStore().fetchMyMenus()
      useApprovalBadgeStore().startPolling()
      sessionReady.value = true
      return true
    } catch {
      clearSession()
      sessionReady.value = true
      return false
    }
  }

  async function register(payload) {
    try {
      const { data } = await authApi.register(payload)
      if (data.success) {
        setSession(data.data)
        await useMenuStore().fetchMyMenus()
        useApprovalBadgeStore().startPolling()
        sessionReady.value = true
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
        await useMenuStore().fetchMyMenus()
        useApprovalBadgeStore().startPolling()
        sessionReady.value = true
      }
      return data
    } catch (error) {
      const err = new Error(getErrorMessage(error))
      err.errorCode = getErrorCode(error)
      throw err
    }
  }

  async function changePassword(payload) {
    try {
      const { data } = await authApi.changePassword(payload)
      return data
    } catch (error) {
      const err = new Error(getErrorMessage(error))
      err.errorCode = getErrorCode(error)
      throw err
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
      // ignore
    }
    clearSession()
    sessionReady.value = true
  }

  return {
    accessToken,
    user,
    sessionReady,
    isAuthenticated,
    isSystemAdmin,
    restoreSession,
    register,
    login,
    changePassword,
    fetchMe,
    logout,
  }
})
