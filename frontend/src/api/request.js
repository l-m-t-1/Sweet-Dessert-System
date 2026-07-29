import axios from 'axios'

const request = axios.create({ baseURL: '/api', timeout: 10000 })

request.interceptors.response.use(
  response => {
    const body = response.data
    if (body && typeof body.success === 'boolean') {
      if (body.success) return body.data
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  error => Promise.reject(new Error(
    error.response?.data?.message || '网络连接失败，请稍后重试'
  ))
)

export default request
