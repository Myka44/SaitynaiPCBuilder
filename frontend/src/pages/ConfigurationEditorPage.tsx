import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Check, ExternalLink, Pencil, Plus, Trash2, X } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import { ConfirmModal } from '../components/ConfirmModal'
import { ConfigurationFormModal, type ConfigurationFormValues } from '../components/ConfigurationFormModal'
import { ConfigurationOffersModal } from '../components/ConfigurationOffersModal'
import { ErrorState, LoadingGrid } from '../components/States'
import { useToast } from '../components/Toast'
import type { Component, Configuration, ConfigurationSummary, HalPage } from '../types'
import { categoryLabels } from '../types'

export function ConfigurationEditorPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const [editing, setEditing] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [offerComponent, setOfferComponent] = useState<Component | null>(null)
  const configuration = useQuery({ queryKey: ['configuration', id], enabled: Boolean(id),
    queryFn: async () => (await api.get<Configuration>(`/configurations/${id}`)).data })
  const summary = useQuery({ queryKey: ['configuration-summary', id], enabled: Boolean(id),
    queryFn: async () => (await api.get<ConfigurationSummary>(`/configurations/${id}/summary`)).data })
  const catalog = useQuery({ queryKey: ['components', 'builder'], queryFn: async () =>
    (await api.get<HalPage<Component>>('/components', { params: { size: 100, sort: 'category,asc' } })).data })
  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['configuration', id] })
    await queryClient.invalidateQueries({ queryKey: ['configuration-summary', id] })
    await queryClient.invalidateQueries({ queryKey: ['configurations'] })
  }
  const relation = useMutation({ mutationFn: async ({ componentId, attached }: { componentId: string; attached: boolean }) =>
    attached ? api.delete(`/configurations/${id}/components/${componentId}`) : api.put(`/configurations/${id}/components/${componentId}`),
    onSuccess: async () => { await refresh(); showToast('Komponentų sąrašas atnaujintas.') },
    onError: (error) => showToast(problemMessage(error), 'error') })
  const update = useMutation({ mutationFn: async (values: ConfigurationFormValues) => api.put(`/configurations/${id}`, values),
    onSuccess: async () => { await refresh(); setEditing(false); showToast('Komplektacija išsaugota.') },
    onError: (error) => showToast(problemMessage(error), 'error') })
  const remove = useMutation({ mutationFn: async () => api.delete(`/configurations/${id}`),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['configurations'] }); showToast('Komplektacija ištrinta.'); navigate('/app/configurations') },
    onError: (error) => showToast(problemMessage(error), 'error') })
  if (configuration.isPending || summary.isPending || catalog.isPending) return <section className="page container"><LoadingGrid /></section>
  if (configuration.isError || summary.isError || catalog.isError) return <section className="page container"><ErrorState message={problemMessage(configuration.error || summary.error || catalog.error)} /></section>
  const data = summary.data
  const selected = new Set(data.components.map((row) => row.component.id))
  const components = embeddedItems(catalog.data)
  return <section className="page container builder-page">
    <Link className="back-link" to="/app/configurations"><ArrowLeft /> Mano komplektacijos</Link>
    <div className="section-heading section-heading--actions"><div><span className="kicker">Komplektacijos konstruktorius</span><h1>{configuration.data.name}</h1>
      <p>{configuration.data.description}</p></div><div className="button-group">
      <button className="button button--ghost" type="button" onClick={() => setEditing(true)}><Pencil /> Redaguoti</button>
      <button className="button button--danger-subtle" type="button" onClick={() => setDeleting(true)}><Trash2 /> Ištrinti</button></div></div>
    <div className="builder-summary"><div><span>Pasirinkta komponentų</span><strong>{selected.size}</strong></div>
      <div><span>Mažiausia kaina</span><strong>{Number(data.totalLowestPriceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</strong></div>
      <div><span>Biudžeto likutis</span><strong className={data.remainingBudgetEur < 0 ? 'negative' : ''}>{Number(data.remainingBudgetEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</strong></div></div>
    <div className="builder-layout"><div><div className="subheading"><h2>Pasirinkti komponentai</h2><Link to={`/configurations/${id}`}>Vieša suvestinė <ExternalLink /></Link></div>
      <div className="builder-selected">{data.components.length === 0 ? <p className="muted">Pasirinkite dalis iš katalogo.</p> : data.components.map(({ component, cheapestOffer }) =>
        <article className="builder-item" key={component.id}><div><span>{categoryLabels[component.category]}</span><h3>{component.name}</h3><p>{component.manufacturer} · {component.model}</p></div>
          <div><strong>{cheapestOffer ? Number(cheapestOffer.priceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' }) : 'Nėra kainos'}</strong>
            <button type="button" className="button button--ghost" onClick={() => setOfferComponent(component)}>Pasiūlymai</button>
            <button type="button" className="icon-button" aria-label={`Pašalinti ${component.name}`} onClick={() => relation.mutate({ componentId: component.id, attached: true })}><X /></button></div></article>)}</div></div>
      <aside className="catalog-picker"><div className="subheading"><h2>Katalogas</h2><Link to="/catalog">Atverti visą</Link></div>
        <div className="catalog-picker__list">{components.map((component) => { const attached = selected.has(component.id); return <div className="picker-row" key={component.id}>
          <div><span>{categoryLabels[component.category]}</span><strong>{component.name}</strong><small>{component.manufacturer} {component.model}</small></div>
          <button type="button" className={attached ? 'picker-button picker-button--selected' : 'picker-button'} disabled={relation.isPending}
            onClick={() => relation.mutate({ componentId: component.id, attached })}>{attached ? <Check /> : <Plus />}<span className="sr-only">{attached ? 'Pašalinti' : 'Pridėti'}</span></button>
        </div>})}</div></aside></div>
    {offerComponent && <ConfigurationOffersModal configurationId={id!} componentId={offerComponent.id}
      componentName={offerComponent.name} onClose={() => setOfferComponent(null)} />}
    {editing && <ConfigurationFormModal initial={configuration.data} pending={update.isPending} onClose={() => setEditing(false)}
      onSubmit={async (values) => { await update.mutateAsync(values) }} />}
    {deleting && <ConfirmModal title="Ištrinti komplektaciją?" detail="Šio veiksmo atšaukti negalėsite. Katalogo komponentai liks sistemoje."
      pending={remove.isPending} onClose={() => setDeleting(false)} onConfirm={() => remove.mutate()} />}
  </section>
}

