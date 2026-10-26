import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import type { HalPage, Offer } from '../types'
import { stockLabels } from '../types'
import { Modal } from './Modal'
import { Pagination } from './Pagination'
import { EmptyState, ErrorState } from './States'

export function ConfigurationOffersModal({ configurationId, componentId, componentName, onClose }: {
  configurationId: string; componentId: string; componentName: string; onClose: () => void
}) {
  const [page, setPage] = useState(0)
  const query = useQuery({ queryKey: ['configuration-component-offers', configurationId, componentId, page],
    queryFn: async () => (await api.get<HalPage<Offer>>(
      `/configurations/${configurationId}/components/${componentId}/offers`,
      { params: { page, size: 20, sort: 'priceEur,asc' } })).data })
  const offers = embeddedItems(query.data)
  return <Modal title="Komponento pasiūlymai" onClose={onClose}>
    <p>{componentName}</p>
    {query.isPending ? <p role="status">Kraunami pasiūlymai…</p>
      : query.isError ? <ErrorState message={problemMessage(query.error)} />
        : offers.length === 0 ? <EmptyState title="Pasiūlymų nėra" detail="Šiam komponentui pasiūlymai dar nesukurti." />
          : <><ul className="configuration-offers">{offers.map((offer) => <li key={offer.id}>
            <div><a className="text-link" href={offer.productUrl} target="_blank" rel="noreferrer">{offer.supplierName}</a>
              <span className="muted">{stockLabels[offer.stockStatus]}</span></div>
            <strong>{Number(offer.priceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</strong>
          </li>)}</ul><Pagination page={query.data!.page} onPage={setPage} /></>}
  </Modal>
}
