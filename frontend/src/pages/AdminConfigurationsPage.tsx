import { useQuery } from '@tanstack/react-query'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import { ConfigurationCard } from '../components/ConfigurationCard'
import { EmptyState, ErrorState, LoadingGrid } from '../components/States'
import type { Configuration, HalPage } from '../types'

export function AdminConfigurationsPage() {
  const query = useQuery({ queryKey: ['configurations', 'admin-all'], queryFn: async () =>
    (await api.get<HalPage<Configuration>>('/configurations', { params: { size: 100, sort: 'updatedAt,desc' } })).data })
  const items = embeddedItems(query.data)
  return <section className="page container"><div className="section-heading"><div>
    <h1>Visos komplektacijos</h1></div></div>
    {query.isPending ? <LoadingGrid /> : query.isError ? <ErrorState message={problemMessage(query.error)} /> : items.length === 0 ?
      <EmptyState title="Įrašų nėra" detail="Duomenų bazėje nėra komplektacijų." /> :
      <div className="card-grid">{items.map((item) => <ConfigurationCard configuration={item} edit key={item.id} />)}</div>}
  </section>
}

