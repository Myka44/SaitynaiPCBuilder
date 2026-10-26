import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, useLocation } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from '../api/client'
import { CatalogPage } from './CatalogPage'

vi.mock('../api/client', () => ({ api: { get: vi.fn() }, problemMessage: () => 'Nepavyko įkelti' }))
vi.mock('../auth/AuthContext', () => ({ useAuth: () => ({ hasRole: () => false }) }))
vi.mock('../components/Toast', () => ({ useToast: () => ({ showToast: vi.fn() }) }))

function LocationProbe() {
  return <output aria-label="URL">{useLocation().search}</output>
}
function renderCatalog(path = '/catalog') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={[path]}>
    <CatalogPage /><LocationProbe />
  </MemoryRouter></QueryClientProvider>)
}
beforeEach(() => {
  vi.mocked(api.get).mockReset()
  vi.mocked(api.get).mockImplementation(async (url) => ({ data: url === '/components/suppliers'
    ? ['BalticPC Demo', 'TechTurgus Demo']
    : { _embedded: { components: [{ id: 'cpu-1', name: 'Ryzen 7', category: 'CPU', manufacturer: 'AMD', model: '7700',
      powerWatts: 65, description: 'Procesorius', cheapestOffer: { priceEur: 319.99, supplierName: 'BalticPC Demo' }, offerCount: 2 }] },
      _links: {}, page: { number: 0, size: 20, totalElements: 1, totalPages: 1 } } }))
})
afterEach(cleanup)

describe('CatalogPage', () => {
  it('rodo filtrų juostą ir komponentų lentelę su kaina bei pardavėju', async () => {
    renderCatalog()
    expect(screen.getByRole('complementary', { name: 'Katalogo filtrai' })).toBeInTheDocument()
    const table = await screen.findByRole('table')
    expect(within(table).getByRole('columnheader', { name: 'Kaina nuo' })).toBeInTheDocument()
    expect(within(table).getByText('BalticPC Demo')).toBeInTheDocument()
    expect(within(table).getByText(/319,99/)).toBeInTheDocument()
    expect(within(table).getByRole('link', { name: 'Ryzen 7' })).toHaveAttribute('href', '/catalog/cpu-1')
    expect(screen.queryByRole('button', { name: /Naujas komponentas/ })).not.toBeInTheDocument()
  })

  it('siunčia kainos, tipo ir pardavėjo filtrus į API ir atstato puslapiavimą', async () => {
    renderCatalog('/catalog?page=3')
    const user = userEvent.setup()
    await screen.findByRole('option', { name: 'TechTurgus Demo' })
    await user.click(screen.getByRole('radio', { name: 'Procesorius' }))
    await user.selectOptions(screen.getByLabelText('Pardavėjas'), 'TechTurgus Demo')
    fireEvent.change(screen.getByLabelText('Nuo'), { target: { value: '100' } })
    fireEvent.change(screen.getByLabelText('Iki'), { target: { value: '400' } })
    await user.click(screen.getByRole('button', { name: 'Taikyti filtrus' }))
    await waitFor(() => expect(api.get).toHaveBeenCalledWith('/components', { params: {
      q: undefined, category: 'CPU', supplier: 'TechTurgus Demo', minPrice: '100', maxPrice: '400', page: 0, size: 5, sort: 'name,asc',
    } }))
    expect(screen.getByLabelText('URL')).not.toHaveTextContent('page=')
    await user.click(screen.getByRole('button', { name: 'Išvalyti' }))
    expect(screen.getByLabelText('URL')).toHaveTextContent('')
    expect(screen.getByLabelText('Nuo')).toHaveValue(null)
  })

  it('paaiškina netinkamą kainos intervalą', async () => {
    renderCatalog()
    fireEvent.change(screen.getByLabelText('Nuo'), { target: { value: '500' } })
    fireEvent.change(screen.getByLabelText('Iki'), { target: { value: '100' } })
    await userEvent.click(screen.getByRole('button', { name: 'Taikyti filtrus' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Minimali kaina negali viršyti maksimalios.')
  })
})
