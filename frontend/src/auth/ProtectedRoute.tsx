import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'
import type { Role } from '../types'

export function ProtectedRoute({ role = 'MEMBER' }: { role?: Role }) {
  const { user, ready, hasRole } = useAuth()
  const location = useLocation()
  if (!ready) return <div className="page container"><div className="loading-bar" aria-label="Tikrinama sesija" /></div>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (!hasRole(role)) return <Navigate to="/forbidden" replace />
  return <Outlet />
}

