import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { api } from '../api/client'
import { HomePage } from './HomePage'

vi.mock('../api/client', () => ({
  api: {
    get: vi.fn(async () => ({ data: {
      _embedded: { configurations: [] },
      _links: {},
      page: { size: 6, number: 0, totalElements: 0, totalPages: 0 },
    } })),
  },
  problemMessage: () => 'Nepavyko įkelti',
}))

function LocationProbe() {
  const location = useLocation()
  return <output aria-label="Paieškos parametrai">{location.search}</output>
}

afterEach(cleanup)

function renderHome(path = '/') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<QueryClientProvider client={client}>
    <MemoryRouter initialEntries={[path]}><HomePage /><LocationProbe /></MemoryRouter>
  </QueryClientProvider>)
}

describe('HomePage', () => {
  it('rodo kompaktišką antraštę ir paiešką be hero paveikslo', async () => {
    const { container } = renderHome()
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Kompiuterių komplektacijos')
    expect(container.querySelector('img')).not.toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ieškoti' })).toHaveClass('button')
    expect(await screen.findByText('Komplektacijų nerasta')).toBeInTheDocument()
  })

  it('paieška atnaujina URL ir grąžina į pirmą rezultatų puslapį', async () => {
    renderHome('/?page=2')
    const user = userEvent.setup()
    await user.type(screen.getByRole('textbox', { name: 'Ieškoti viešų komplektacijų' }), 'Ryzen')
    await user.click(screen.getByRole('button', { name: 'Ieškoti' }))
    await waitFor(() => expect(screen.getByLabelText('Paieškos parametrai')).toHaveTextContent('?q=Ryzen'))
    expect(screen.getByLabelText('Paieškos parametrai')).not.toHaveTextContent('page=')
    await waitFor(() => expect(api.get).toHaveBeenCalledWith('/configurations', {
      params: { visibility: 'PUBLIC', q: 'Ryzen', page: 0, size: 6, sort: 'updatedAt,desc' },
    }))
  })
})
