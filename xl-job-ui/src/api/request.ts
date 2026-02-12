import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 可以在这里添加 token 等认证信息
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers['X-Token'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 根据后端返回的 code 判断请求是否成功
    if (res.code !== 200) {
      message.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const errorMsg = error.response?.data?.msg || error.message || '网络错误'
    message.error(errorMsg)
    return Promise.reject(error)
  }
)

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  content: T
}

export interface PageResponse<T> {
  dataList: T[]
  recordsTotal: number
  recordsFiltered: number
}

export default request
