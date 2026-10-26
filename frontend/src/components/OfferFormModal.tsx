import { useForm } from 'react-hook-form'
import { Modal } from './Modal'
import type { Offer, StockStatus } from '../types'

export interface OfferFormValues {
  supplierName: string
  priceEur: number
  productUrl: string
  stockStatus: StockStatus
  checkedAt: string
}

function toLocalDateTime(value?: string) {
  const date = value ? new Date(value) : new Date()
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 16)
}

export function OfferFormModal({ initial, pending, onClose, onSubmit }: {
  initial?: Offer
  pending: boolean
  onClose: () => void
  onSubmit: (values: OfferFormValues) => Promise<void>
}) {
  const { register, handleSubmit } = useForm<OfferFormValues>({ defaultValues: initial ? {
    supplierName: initial.supplierName, priceEur: initial.priceEur, productUrl: initial.productUrl,
    stockStatus: initial.stockStatus, checkedAt: toLocalDateTime(initial.checkedAt),
  } : { stockStatus: 'IN_STOCK', checkedAt: toLocalDateTime() } })
  return <Modal title={initial ? 'Redaguoti pasiūlymą' : 'Naujas pasiūlymas'} onClose={onClose}>
    <form className="form" onSubmit={handleSubmit(async (values) => onSubmit({ ...values, checkedAt: new Date(values.checkedAt).toISOString() }))}>
      <label>Mockintas tiekėjas<input {...register('supplierName', { required: true, maxLength: 100 })} /></label>
      <div className="form__row"><label>Kaina, €<input type="number" min="0.01" step="0.01"
        {...register('priceEur', { required: true, valueAsNumber: true, min: 0.01 })} /></label>
        <label>Būsena<select {...register('stockStatus')}><option value="IN_STOCK">Yra sandėlyje</option>
          <option value="LOW_STOCK">Liko nedaug</option><option value="OUT_OF_STOCK">Nėra sandėlyje</option></select></label></div>
      <label>Pasiūlymo HTTPS adresas<input type="url" placeholder="https://example.invalid/produktas"
        {...register('productUrl', { required: true, pattern: /^https:\/\/.+/ })} /></label>
      <label>Patikrinta<input type="datetime-local" {...register('checkedAt', { required: true })} /></label>
      <div className="form__actions"><button type="button" className="button button--ghost" onClick={onClose}>Atšaukti</button>
        <button type="submit" className="button" disabled={pending}>{pending ? 'Saugoma…' : 'Išsaugoti'}</button></div>
    </form>
  </Modal>
}

