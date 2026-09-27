import axios from 'axios'

const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

export const API_BASE_URL = configuredBaseUrl.replace(/\/+$/, '')

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20000,
  headers: {
    Accept: 'application/json',
  },
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isAxiosError(error)) {
      const serverMessage = error.response?.data?.message
      const friendlyError = new Error(
        serverMessage || error.message || '请求后端服务失败',
      )
      friendlyError.status = error.response?.status
      friendlyError.cause = error
      return Promise.reject(friendlyError)
    }

    return Promise.reject(error)
  },
)

export function resolveApiUrl(path) {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`

  if (/^https?:\/\//i.test(normalizedPath)) {
    return normalizedPath
  }

  return apiClient.getUri({
    url: normalizedPath,
    baseURL: API_BASE_URL,
  })
}