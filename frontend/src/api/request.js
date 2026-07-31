import axios from 'axios'
import { clearSession, readSession } from '../auth/session'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

request.interceptors.request.use(config => {
  const token = readSession()?.token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

request.interceptors.response.use(
  response => {
    const body = response.data
    if (body && typeof body.success === 'boolean') {
      if (body.success) return body.data
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  error => {
    if (error.response?.status === 401) {
      clearSession()
      if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
        window.location.assign('/login')
      }
    }
    return Promise.reject(new Error(
      error.response?.data?.message || '网络连接失败，请稍后重试'
    ))
  }
)

export default request
