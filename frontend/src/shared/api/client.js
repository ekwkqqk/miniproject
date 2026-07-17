import axios from 'axios'
import router from '@/router'
import { getAccessToken, clearAccessToken } from '@/features/auth/tokenHolder'
import { refreshSession } from '@/features/auth/refreshSession'

const client = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

let isRefreshing = false
let failedQueue = []

function processQueue(error, token = null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) {
      reject(error)
    } else {
      resolve(token)
    }
  })
  failedQueue = []
}

function isAuthRequest(url) {
  return url?.includes('/auth/login')
    || url?.includes('/auth/register')
    || url?.includes('/auth/refresh')
    || url?.includes('/auth/logout')
    || url?.includes('/auth/change-password')
}

function goToErrorPage(name) {
  if (router.currentRoute.value.name !== name) {
    router.push({ name })
  }
}

function goToLogin() {
  clearAccessToken()
  const current = router.currentRoute.value
  if (current.name === 'login') return

  const query = {}
  if (current.meta?.requiresAuth && current.fullPath) {
    query.redirect = current.fullPath
  }
  router.push({ name: 'login', query })
}

client.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // FormData는 boundary가 필요하므로 기본 JSON Content-Type을 제거한다
  if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
    if (typeof config.headers?.delete === 'function') {
      config.headers.delete('Content-Type')
    } else if (config.headers) {
      delete config.headers['Content-Type']
    }
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    // 실제 권한 부족만 403 페이지로. 토큰 만료는 백엔드가 401로 내려준다.
    if (status === 403) {
      goToErrorPage('forbidden')
      return Promise.reject(error)
    }

    if (status !== 401 || !originalRequest || originalRequest._retry) {
      if (status === 401 && !isAuthRequest(originalRequest?.url)) {
        goToLogin()
      }
      return Promise.reject(error)
    }

    // 로그인/회원가입 실패(잘못된 비밀번호 등)는 로그인 강제 이동하지 않음
    if (isAuthRequest(originalRequest.url)) {
      return Promise.reject(error)
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject })
      }).then((token) => {
        originalRequest.headers.Authorization = `Bearer ${token}`
        return client(originalRequest)
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    try {
      const { accessToken } = await refreshSession()
      processQueue(null, accessToken)
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return client(originalRequest)
    } catch (refreshError) {
      processQueue(refreshError, null)
      goToLogin()
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export function getErrorMessage(error) {
  return error.response?.data?.message || error.message || '요청 처리 중 오류가 발생했습니다.'
}

export function getErrorCode(error) {
  return error.response?.data?.errorCode || null
}

export default client
