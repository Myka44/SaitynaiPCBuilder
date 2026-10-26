import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { cleanup, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { api } from '../api/client'
import { ComponentDetailPage } from './ComponentDetailPage'
import { ConfigurationEditorPage } from './ConfigurationEditorPage'

vi.mock('../api/client', () => ({ api: { get: vi.fn() }, problemMessage: () => 'Nepavyko įkelti' }))
vi.mock('../auth/AuthContext', () => ({ useAuth: () => ({ hasRole: () => false }) }))
vi.mock('../components/Toast', () => ({ useToast: () => ({ showToast: vi.fn() }) }))

const component = { id: 'cpu-1', name: 'Ryzen 7', model: '7700', manufacturer: 'AMD', category: 'CPU', powerWatts: 65, description: 'Procesorius' }
const offer = { id: 'offer-1', componentId: 'cpu-1', supplierName: 'BalticPC Demo', priceEur: 319.99,
  stockStatus: 'IN_STOCK', checkedAt: '2026-10-26T10:00:00Z', productUrl: 'https://example.invalid/demo' }
const configuration = { id: 'config-1', name: 'Komplektacijos duomenys', description: 'Aprašymas', visibility: 'PRIVATE', budgetEur: 1200, usageType: 'GAMING' }
const hal = (items: unknown[], page = 0) => ({ _embedded: { items }, _links: {}, page: { number: page, size: 20, totalElements: 21, totalPages: 2 } })

beforeEach(() => {
  vi.mocked(api.get).mockReset()
  vi.mocked(api.get).mockImplementation(async (url, options) => {
    if (url === '/components/cpu-1') return { data: component }
    if (url === '/offers/offer-1') return { data: { ...offer, priceEur: 325 } }
    if (url === '/components/cpu-1/offers') return { data: hal([offer]) }
    if (url === '/components') return { data: hal([component]) }
    if (url === '/configurations/config-1') return { data: configuration }
    if (url === '/configurations/config-1/summary') return { data: {
      configuration: { ...configuration, name: 'Suvestinės duomenys' }, components: [{ component, cheapestOffer: offer }],
      totalLowestPriceEur: 319.99, remainingBudgetEur: 880.01, withinBudget: true,
    } }
    if (url === '/configurations/config-1/components/cpu-1/offers') {
      const page = (options?.params as { page?: number } | undefined)?.page ?? 0
      return { data: hal([{ ...offer, supplierName: page === 0 ? 'BalticPC Demo' : 'TechTurgus Demo' }], page) }
    }
    throw new Error(`Unexpected endpoint: ${url}`)
  })
})
afterEach(cleanup)

function renderPage(route: 'component' | 'configuration') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(<QueryClientProvider client={client}><MemoryRouter initialEntries={[
    route === 'component' ? '/catalog/cpu-1' : '/app/configurations/config-1/edit',
  ]}><Routes><Route path="/catalog/:id" element={<ComponentDetailPage />} />
    <Route path="/app/configurations/:id/edit" element={<ConfigurationEditorPage />} /></Routes></MemoryRouter></QueryClientProvider>)
}

describe('Lab3 skaitymo veiksmai', () => {
  it('rodo iliustraciją ir nuskaito atskiro pasiūlymo informaciją atidarius jo langą', async () => {
    renderPage('component')
    expect(await screen.findByRole('img', { name: 'Kompiuterio komponentų iliustracija' }))
      .toHaveAttribute('src', '/images/component-illustration.svg')
    await userEvent.click(screen.getByRole('button', { name: 'Peržiūrėti BalticPC Demo pasiūlymą' }))
    const modal = await screen.findByRole('dialog', { name: 'Pasiūlymo informacija' })
    expect(await within(modal).findByText(/325,00/)).toBeInTheDocument()
    expect(api.get).toHaveBeenCalledWith('/offers/offer-1')
  })

  it('naudoja komplektacijos GET ir trijų objektų hierarchinį pasiūlymų metodą su puslapiavimu', async () => {
    renderPage('configuration')
    expect(await screen.findByRole('heading', { name: 'Komplektacijos duomenys' })).toBeInTheDocument()
    expect(api.get).toHaveBeenCalledWith('/configurations/config-1')
    await userEvent.click(screen.getByRole('button', { name: 'Pasiūlymai' }))
    const modal = await screen.findByRole('dialog', { name: 'Komponento pasiūlymai' })
    expect(await within(modal).findByText('BalticPC Demo')).toBeInTheDocument()
    expect(api.get).toHaveBeenCalledWith('/configurations/config-1/components/cpu-1/offers', {
      params: { page: 0, size: 20, sort: 'priceEur,asc' },
    })
    await userEvent.click(within(modal).getByRole('button', { name: 'Kitas' }))
    await waitFor(() => expect(within(modal).getByText('TechTurgus Demo')).toBeInTheDocument())
    expect(api.get).toHaveBeenCalledWith('/configurations/config-1/components/cpu-1/offers', {
      params: { page: 1, size: 20, sort: 'priceEur,asc' },
    })
  })
})
