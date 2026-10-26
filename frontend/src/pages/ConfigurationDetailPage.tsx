import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, ExternalLink, WalletCards } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { api, problemMessage } from '../api/client'
import { ErrorState, LoadingGrid } from '../components/States'
import type { ConfigurationSummary } from '../types'
import { categoryLabels, stockLabels, usageLabels } from '../types'

export function ConfigurationDetailPage() {
  const { id } = useParams()
  const query = useQuery({ queryKey: ['configuration-summary', id], enabled: Boolean(id),
    queryFn: async () => (await api.get<ConfigurationSummary>(`/configurations/${id}/summary`)).data })
  if (query.isPending) return <section className="page container"><LoadingGrid /></section>
  if (query.isError) return <section className="page container"><ErrorState message={problemMessage(query.error)} /></section>
  const data = query.data
  return <section className="page container detail-page">
    <Link className="back-link" to="/"><ArrowLeft /> Grįžti į komplektacijas</Link>
    <div className="detail-hero"><div><span className="kicker">{usageLabels[data.configuration.usageType]}</span>
      <h1>{data.configuration.name}</h1><p>{data.configuration.description}</p></div>
      <div className={`budget-panel ${data.withinBudget ? 'budget-panel--ok' : ''}`}><WalletCards />
        <span>Mažiausia bendra kaina</span><strong>{Number(data.totalLowestPriceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</strong>
        <small>Biudžetas {Number(data.configuration.budgetEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</small></div></div>
    <div className="component-list">{data.components.map(({ component, cheapestOffer }) => <article className="component-row" key={component.id}>
      <div className="component-row__category">{categoryLabels[component.category]}</div>
      <div><h2><Link to={`/catalog/${component.id}`}>{component.name}</Link></h2><p>{component.manufacturer} · {component.model}</p></div>
      {cheapestOffer ? <div className="component-row__offer"><span>{stockLabels[cheapestOffer.stockStatus]}</span>
        <strong>{Number(cheapestOffer.priceEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</strong>
        <a href={cheapestOffer.productUrl} target="_blank" rel="noreferrer">{cheapestOffer.supplierName}<ExternalLink /></a></div> :
        <span className="muted">Pasiūlymų nėra</span>}
    </article>)}</div>
  </section>
}

