import { useQuery } from '@tanstack/react-query'
import { api, problemMessage } from '../api/client'
import type { Offer } from '../types'
import { stockLabels } from '../types'
import { Modal } from './Modal'
import { ErrorState } from './States'

export function OfferDetailsModal({ offerId, onClose }: { offerId: string; onClose: () => void }) {
  const query = useQuery({ queryKey: ['offer', offerId],
    queryFn: async () => (await api.get<Offer>(`/offers/${offerId}`)).data })
  return <Modal title="Pasiūlymo informacija" onClose={onClose}>
    {query.isPending ? <p role="status">Kraunamas pasiūlymas…</p>
      : query.isError ? <ErrorState message={problemMessage(query.error)} />
        : <><dl className="spec-list">
          <div><dt>Pardavėjas</dt><dd>{query.data.supplierName}</dd></div>
          <div><dt>Kaina</dt><dd>{Number(query.data.priceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</dd></div>
          <div><dt>Būsena</dt><dd>{stockLabels[query.data.stockStatus]}</dd></div>
        </dl><p className="muted">Patikrinta: {new Date(query.data.checkedAt).toLocaleString('lt-LT')}</p>
          <a className="button" href={query.data.productUrl} target="_blank" rel="noreferrer">Atverti pardavėjo puslapį</a></>}
  </Modal>
}
