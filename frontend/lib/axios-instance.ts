import axios, {
  AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios'
import { clearAuth, setAccessToken } from '@/lib/auth'

type RefreshResponse = { accessToken?: string }
type RetryableRequest = InternalAxiosRequestConfig & { _retry?: boolean }

const API_URL = process.env.NEXT_PUBLIC_API_URL
let refreshPromise: Promise<string> | null = null

export const AXIOS_INSTANCE = axios.create({
  baseURL: API_URL,
  withCredentials: true,
})

AXIOS_INSTANCE.interceptors.request.use((config) => {
  const token =
    typeof window !== 'undefined' ? localStorage.getItem('token') : null
  if (token) {
    config.headers = config.headers ?? {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function refreshAccessToken() {
  if (typeof window === 'undefined') {
    return Promise.reject(
      new Error('Token refresh is only available in the browser')
    )
  }

  if (!refreshPromise) {
    refreshPromise = axios
      .get<RefreshResponse>(`${API_URL ?? ''}/auth/refresh`, {
        withCredentials: true,
      })
      .then(({ data }) => {
        if (!data.accessToken)
          throw new Error('Refresh response has no access token')
        setAccessToken(data.accessToken)
        return data.accessToken
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

AXIOS_INSTANCE.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const request = error.config as RetryableRequest | undefined
    const isUnauthorized = error.response?.status === 401
    const isAuthRequest =
      request?.url?.includes('/auth/login') ||
      request?.url?.includes('/auth/refresh') ||
      request?.url?.includes('/auth/logout')

    if (!request || !isUnauthorized || isAuthRequest || request._retry) {
      return Promise.reject(error)
    }

    request._retry = true

    try {
      const accessToken = await refreshAccessToken()
      request.headers.Authorization = `Bearer ${accessToken}`
      return AXIOS_INSTANCE(request)
    } catch (refreshError) {
      clearAuth()
      return Promise.reject(refreshError)
    }
  }
)

export const customInstance = <T>(
  config: AxiosRequestConfig,
  options?: AxiosRequestConfig
): Promise<T> => {
  return AXIOS_INSTANCE({ ...config, ...options }).then(({ data }) => data)
}

export default customInstance
