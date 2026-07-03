import axios from 'axios'
import router from '@/router'

const client = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      if (router.currentRoute.value.meta.requiresAuth) {
        router.push({ name: 'login' })
      }
    }
    return Promise.reject(error)
  },
)

export function getErrorMessage(error) {
  return error.response?.data?.message || error.message || '요청 처리 중 오류가 발생했습니다.'
}

export default client
