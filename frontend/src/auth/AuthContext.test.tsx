import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api, refreshAccessToken } from '../api/client'
import { AuthProvider, useAuth } from './AuthContext'

vi.mock('../api/client', () => ({ api: { get: vi.fn() }, refreshAccessToken: vi.fn(),
  setAccessToken: vi.fn(), setSessionListener: vi.fn() }))
function Probe() {
  const { ready, user } = useAuth()
  return <p>{ready ? user?.email ?? 'Svečias' : 'Kraunama'}</p>
}
function renderAuth() {
  return render(<QueryClientProvider client={new QueryClient()}><AuthProvider><Probe /></AuthProvider></QueryClientProvider>)
}
beforeEach(() => vi.resetAllMocks())
afterEach(cleanup)
describe('AuthContext sesijos atkūrimas', () => {
  it('po refresh gauna naudotoją per auth/me', async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue({ accessToken: 'test', tokenType: 'Bearer', expiresIn: 900,
      user: { id: 'user-1', email: 'old@example.lt', role: 'MEMBER' } })
    vi.mocked(api.get).mockResolvedValue({ data: { id: 'user-1', email: 'current@example.lt', role: 'MEMBER' } })
    renderAuth()
    expect(await screen.findByText('current@example.lt')).toBeInTheDocument()
    expect(api.get).toHaveBeenCalledWith('/auth/me')
  })
  it('nepavykus refresh palieka svečio prieigą', async () => {
    vi.mocked(refreshAccessToken).mockRejectedValue(new Error('Expired'))
    renderAuth()
    expect(await screen.findByText('Svečias')).toBeInTheDocument()
    expect(api.get).not.toHaveBeenCalled()
  })
})
