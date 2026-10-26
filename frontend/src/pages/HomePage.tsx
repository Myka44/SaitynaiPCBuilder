import { useQuery } from '@tanstack/react-query'
import { Search } from 'lucide-react'
import { useSearchParams } from 'react-router-dom'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import { ConfigurationCard } from '../components/ConfigurationCard'
import { EmptyState, ErrorState, LoadingGrid } from '../components/States'
import { Pagination } from '../components/Pagination'
import type { Configuration, HalPage } from '../types'

export function HomePage() {
  const [params, setParams] = useSearchParams()
  const q = params.get('q') ?? ''
  const page = Number(params.get('page') ?? 0)
  const query = useQuery({
    queryKey: ['configurations', 'public', q, page],
    queryFn: async () => (await api.get<HalPage<Configuration>>('/configurations', {
      params: { visibility: 'PUBLIC', q: q || undefined, page, size: 6, sort: 'updatedAt,desc' },
    })).data,
  })
  const items = embeddedItems(query.data)
  return <>
    <section className="hero">
      <div className="container hero__content">
        <div className="hero__intro">
          <span className="hero__eyebrow">Komponentai · Pasiūlymai · Komplektacijos</span>
          <h1>Kompiuterių komplektacijos</h1>
        </div>
        <form className="search" onSubmit={(event) => {
          event.preventDefault()
          const value = new FormData(event.currentTarget).get('q')?.toString() ?? ''
          setParams(value ? { q: value } : {})
        }}><div className="search__field"><Search aria-hidden="true" /><input name="q" defaultValue={q} aria-label="Ieškoti viešų komplektacijų"
              placeholder="Ieškoti komplektacijų" /></div><button className="button" type="submit">Ieškoti</button></form>
      </div>
    </section>
    <section className="page container">
      <div className="section-heading"><div><h2>Viešos komplektacijos</h2></div>
        {query.data && <span className="result-count">{query.data.page.totalElements} rezultatai</span>}</div>
      {query.isPending ? <LoadingGrid /> : query.isError ? <ErrorState message={problemMessage(query.error)} /> :
        items.length === 0 ? <EmptyState title="Komplektacijų nerasta" detail="Pakeiskite paiešką arba sukurkite pirmą viešą komplektaciją." /> :
          <><div className="card-grid">{items.map((item) => <ConfigurationCard configuration={item} key={item.id} />)}</div>
            <Pagination page={query.data!.page} onPage={(next) => setParams((current) => { current.set('page', String(next)); return current })} /></>}
    </section>
  </>
}

