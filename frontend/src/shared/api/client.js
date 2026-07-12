import axios from 'axios'
import router from '@/router'
import { getAccessToken, setAuthSession, clearAccessToken } from '@/features/auth/tokenHolder'

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
}

function goToErrorPage(name) {
  if (router.currentRoute.value.name !== name) {
    router.push({ name })
  }
}

client.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    if (status === 403) {
      goToErrorPage('forbidden')
      return Promise.reject(error)
    }

    if (status !== 401 || !originalRequest || originalRequest._retry) {
      return Promise.reject(error)
    }

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
      const { data } = await axios.post('/api/auth/refresh', null, { withCredentials: true })
      const newAccessToken = data.data.accessToken

      setAuthSession(newAccessToken, data.data.user)
      processQueue(null, newAccessToken)
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return client(originalRequest)
    } catch (refreshError) {
      processQueue(refreshError, null)
      clearAccessToken()
      if (router.currentRoute.value.name !== 'login') {
        router.push({ name: 'login' })
      }
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  },
)

export function getErrorMessage(error) {
  return error.response?.data?.message || error.message || '요청 처리 중 오류가 발생했습니다.'
}

export default client
