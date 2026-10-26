import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios'
import type { ApiProblem, AuthResponse } from '../types'

const baseURL = import.meta.env.VITE_API_BASE_URL || '/api/v1'

export const api = axios.create({ baseURL, withCredentials: true })

let accessToken: string | null = null
let refreshPromise: Promise<AuthResponse> | null = null
let sessionListener: ((session: AuthResponse | null) => void) | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function setSessionListener(listener: ((session: AuthResponse | null) => void) | null) {
  sessionListener = listener
}

export async function refreshAccessToken(): Promise<AuthResponse> {
  if (!refreshPromise) {
    refreshPromise = axios.post<AuthResponse>(`${baseURL}/auth/refresh`, {}, { withCredentials: true })
      .then(({ data }) => {
        setAccessToken(data.accessToken)
        sessionListener?.(data)
        return data
      })
      .catch((error) => {
        setAccessToken(null)
        sessionListener?.(null)
        throw error
      })
      .finally(() => { refreshPromise = null })
  }
  return refreshPromise
}

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`
  return config
})

api.interceptors.response.use(undefined, async (error: AxiosError) => {
  const request = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined
  const isAuthCall = request?.url?.includes('/auth/')
  if (error.response?.status === 401 && request && !request._retry && !isAuthCall) {
    request._retry = true
    const session = await refreshAccessToken()
    request.headers.Authorization = `Bearer ${session.accessToken}`
    return api.request(request)
  }
  throw error
})

export function problemMessage(error: unknown, fallback = 'Veiksmo atlikti nepavyko.') {
  if (axios.isAxiosError<ApiProblem>(error)) {
    return error.response?.data?.detail || error.response?.data?.title || fallback
  }
  return fallback
}

