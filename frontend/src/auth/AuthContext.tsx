/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { api, refreshAccessToken, setAccessToken, setSessionListener } from '../api/client'
import type { AuthResponse, Role, User } from '../types'

interface AuthContextValue {
  user: User | null
  ready: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
  hasRole: (role: Role) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)
const roleWeight: Record<Role, number> = { MEMBER: 1, EDITOR: 2, ADMIN: 3 }

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [ready, setReady] = useState(false)
  const queryClient = useQueryClient()

  const applySession = useCallback((session: AuthResponse | null) => {
    setAccessToken(session?.accessToken ?? null)
    setUser(session?.user ?? null)
  }, [])

  useEffect(() => {
    setSessionListener(applySession)
    refreshAccessToken().then(async () => {
      const { data } = await api.get<User>('/auth/me')
      setUser(data)
    }).catch(() => applySession(null)).finally(() => setReady(true))
    return () => setSessionListener(null)
  }, [applySession])

  const authenticate = async (path: 'login' | 'register', email: string, password: string) => {
    const { data } = await api.post<AuthResponse>(`/auth/${path}`, { email, password })
    applySession(data)
    await queryClient.invalidateQueries()
  }

  const logout = async () => {
    try { await api.post('/auth/logout') } finally {
      applySession(null)
      queryClient.clear()
    }
  }

  const value = useMemo<AuthContextValue>(() => ({
    user,
    ready,
    login: (email, password) => authenticate('login', email, password),
    register: (email, password) => authenticate('register', email, password),
    logout,
    hasRole: (role) => Boolean(user && roleWeight[user.role] >= roleWeight[role]),
  // authenticate is intentionally scoped to this provider render.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }), [user, ready])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth turi būti naudojamas AuthProvider viduje')
  return value
}
