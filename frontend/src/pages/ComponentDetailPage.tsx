import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, ExternalLink, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import { useAuth } from '../auth/AuthContext'
import { ConfirmModal } from '../components/ConfirmModal'
import { OfferFormModal, type OfferFormValues } from '../components/OfferFormModal'
import { OfferDetailsModal } from '../components/OfferDetailsModal'
import { ErrorState, LoadingGrid } from '../components/States'
import { useToast } from '../components/Toast'
import type { Component, HalPage, Offer } from '../types'
import { categoryLabels, stockLabels } from '../types'

export function ComponentDetailPage() {
  const { id } = useParams()
  const { hasRole } = useAuth()
  const { showToast } = useToast()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState<Offer | null | 'new'>(null)
  const [deleting, setDeleting] = useState<Offer | null>(null)
  const [previewOffer, setPreviewOffer] = useState<string | null>(null)
  const component = useQuery({ queryKey: ['component', id], enabled: Boolean(id),
    queryFn: async () => (await api.get<Component>(`/components/${id}`)).data })
  const offers = useQuery({ queryKey: ['offers', id], enabled: Boolean(id), queryFn: async () =>
    (await api.get<HalPage<Offer>>(`/components/${id}/offers`, { params: { size: 100, sort: 'priceEur,asc' } })).data })
  const save = useMutation({ mutationFn: async (values: OfferFormValues) => editing === 'new'
    ? api.post(`/components/${id}/offers`, values) : api.put(`/offers/${(editing as Offer).id}`, values),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['offers', id] });
      await queryClient.invalidateQueries({ queryKey: ['offer'] }); setEditing(null); showToast('Pasiūlymas išsaugotas.') },
    onError: (error) => showToast(problemMessage(error), 'error') })
  const remove = useMutation({ mutationFn: async () => api.delete(`/offers/${deleting!.id}`),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['offers', id] }); setDeleting(null); showToast('Pasiūlymas ištrintas.') },
    onError: (error) => showToast(problemMessage(error), 'error') })
  if (component.isPending || offers.isPending) return <section className="page container"><LoadingGrid /></section>
  if (component.isError || offers.isError) return <section className="page container"><ErrorState message={problemMessage(component.error || offers.error)} /></section>
  const item = component.data
  const offerItems = embeddedItems(offers.data)
  return <section className="page container detail-page">
    <Link className="back-link" to="/catalog"><ArrowLeft /> Komponentų katalogas</Link>
    <div className="component-detail"><div className="component-detail__visual">
      <img className="component-illustration" src="/images/component-illustration.svg" alt="Kompiuterio komponentų iliustracija" width="400" height="300" />
    </div>
      <div><span className="kicker">{categoryLabels[item.category]}</span><h1>{item.name}</h1><p className="lead">{item.description}</p>
        <dl className="spec-list"><div><dt>Gamintojas</dt><dd>{item.manufacturer}</dd></div><div><dt>Modelis</dt><dd>{item.model}</dd></div>
          <div><dt>Galia</dt><dd>{item.powerWatts == null ? 'Nenurodyta' : `${item.powerWatts} W`}</dd></div></dl></div></div>
    <div className="section-heading section-heading--actions"><div><h2>Mockinti pasiūlymai</h2><p>Kainos skirtos tik sistemos demonstracijai.</p></div>
      {hasRole('EDITOR') && <button className="button" type="button" onClick={() => setEditing('new')}><Plus /> Naujas pasiūlymas</button>}</div>
    <div className="offer-table" role="table"><div className="offer-table__head" role="row"><span>Tiekėjas</span><span>Būsena</span><span>Patikrinta</span><span>Kaina</span><span /></div>
      {offerItems.map((offer) => <div className="offer-table__row" role="row" key={offer.id}>
        <button type="button" className="text-link offer-detail-link" onClick={() => setPreviewOffer(offer.id)}
          aria-label={`Peržiūrėti ${offer.supplierName} pasiūlymą`}>{offer.supplierName}</button>
        <span className={`stock stock--${offer.stockStatus.toLowerCase()}`}>{stockLabels[offer.stockStatus]}</span>
        <span>{new Date(offer.checkedAt).toLocaleString('lt-LT')}</span><strong>{Number(offer.priceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</strong>
        <div><a className="icon-button" href={offer.productUrl} target="_blank" rel="noreferrer" aria-label="Atverti pasiūlymą"><ExternalLink /></a>
          {hasRole('EDITOR') && <><button className="icon-button" type="button" onClick={() => setEditing(offer)} aria-label="Redaguoti"><Pencil /></button>
            <button className="icon-button icon-button--danger" type="button" onClick={() => setDeleting(offer)} aria-label="Ištrinti"><Trash2 /></button></>}</div></div>)}</div>
    {previewOffer && <OfferDetailsModal offerId={previewOffer} onClose={() => setPreviewOffer(null)} />}
    {editing && <OfferFormModal initial={editing === 'new' ? undefined : editing} pending={save.isPending} onClose={() => setEditing(null)}
      onSubmit={async (values) => { await save.mutateAsync(values) }} />}
    {deleting && <ConfirmModal title="Ištrinti pasiūlymą?" detail={`${deleting.supplierName} pasiūlymas bus pašalintas.`}
      pending={remove.isPending} onClose={() => setDeleting(null)} onConfirm={() => remove.mutate()} />}
  </section>
}

