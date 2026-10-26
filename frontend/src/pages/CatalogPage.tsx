import { useState, type FormEvent } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowRight, Cpu, Filter, Pencil, Plus, Search, Trash2, X } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import { useAuth } from '../auth/AuthContext'
import { ComponentFormModal, type ComponentFormValues } from '../components/ComponentFormModal'
import { ConfirmModal } from '../components/ConfirmModal'
import { EmptyState, ErrorState } from '../components/States'
import { Pagination } from '../components/Pagination'
import { useToast } from '../components/Toast'
import type { Component, HalPage } from '../types'
import { categoryLabels } from '../types'

const filterNames = ['q', 'category', 'supplier', 'minPrice', 'maxPrice'] as const
const money = (value: number) => Number(value).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })

export function CatalogPage() {
  const [params, setParams] = useSearchParams()
  const [editing, setEditing] = useState<Component | null | 'new'>(null)
  const [deleting, setDeleting] = useState<Component | null>(null)
  const [filterError, setFilterError] = useState('')
  const queryClient = useQueryClient()
  const { hasRole } = useAuth()
  const { showToast } = useToast()
  const canEdit = hasRole('EDITOR')
  const rawPage = Number(params.get('page') ?? 0)
  const page = Number.isInteger(rawPage) && rawPage >= 0 ? rawPage : 0
  const sort = params.get('sort') ?? 'name,asc'
  const filters = Object.fromEntries(filterNames.map((name) => [name, params.get(name) || undefined]))
  const query = useQuery({
    queryKey: ['components', filters, page, sort],
    queryFn: async () => (await api.get<HalPage<Component>>('/components', {
      params: { ...filters, page, size: 5, sort },
    })).data,
  })
  const suppliers = useQuery({
    queryKey: ['component-suppliers'],
    queryFn: async () => (await api.get<string[]>('/components/suppliers')).data,
  })
  const save = useMutation({
    mutationFn: async (values: ComponentFormValues) => editing === 'new'
      ? (await api.post<Component>('/components', values)).data
      : (await api.put<Component>(`/components/${(editing as Component).id}`, values)).data,
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['components'] }); setEditing(null); showToast('Komponentas išsaugotas.') },
    onError: (error) => showToast(problemMessage(error), 'error'),
  })
  const remove = useMutation({
    mutationFn: async () => api.delete(`/components/${deleting!.id}`),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['components'] }); setDeleting(null); showToast('Komponentas ištrintas.') },
    onError: (error) => showToast(problemMessage(error), 'error'),
  })
  const changeParam = (name: string, value: string) => setParams((current) => {
    const next = new URLSearchParams(current)
    if (value) next.set(name, value)
    else next.delete(name)
    if (name !== 'page') next.delete('page')
    return next
  })
  const applyFilters = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const values = new FormData(event.currentTarget)
    const min = String(values.get('minPrice') ?? '')
    const max = String(values.get('maxPrice') ?? '')
    if (min && max && Number(min) > Number(max)) {
      setFilterError('Minimali kaina negali viršyti maksimalios.')
      return
    }
    setFilterError('')
    setParams((current) => {
      const next = new URLSearchParams(current)
      for (const name of filterNames) {
        const value = String(values.get(name) ?? '').trim()
        if (value) next.set(name, value)
        else next.delete(name)
      }
      next.delete('page')
      return next
    })
  }
  const items = embeddedItems(query.data)
  const activeFilters = filterNames.filter((name) => filters[name])
  const supplierOptions = [...new Set([...(suppliers.data ?? []), ...(filters.supplier ? [filters.supplier] : [])])]
  const filterLabel = (name: typeof filterNames[number]) => name === 'category'
    ? categoryLabels[filters.category as keyof typeof categoryLabels] ?? filters.category
    : name === 'minPrice' ? `Nuo ${filters[name]} €` : name === 'maxPrice' ? `Iki ${filters[name]} €` : filters[name]

  return <section className="page container catalog-page">
    <div className="section-heading section-heading--actions"><div><h1>Komponentų katalogas</h1></div>
      {canEdit && <button className="button" type="button" onClick={() => setEditing('new')}><Plus /> Naujas komponentas</button>}
    </div>
    <div className="catalog-layout">
      <aside className="catalog-sidebar" aria-label="Katalogo filtrai">
        <div className="catalog-sidebar__heading"><h2><Filter aria-hidden="true" /> Filtrai</h2>
          <button type="button" className="catalog-reset" onClick={() => { setFilterError(''); setParams({}) }}>Išvalyti</button></div>
        <form key={params.toString()} className="catalog-filter-form" onSubmit={applyFilters}>
          <div className="catalog-filter-group"><label htmlFor="catalog-search">Paieška</label>
            <div className="catalog-search"><Search aria-hidden="true" /><input id="catalog-search" name="q" type="search"
              defaultValue={filters.q ?? ''} placeholder="Pavadinimas ar modelis" /></div></div>
          <fieldset className="catalog-filter-group"><legend>Komponento tipas</legend>
            <label className="catalog-category"><input type="radio" name="category" value="" defaultChecked={!filters.category} />Visi komponentai</label>
            {Object.entries(categoryLabels).map(([value, label]) => <label className="catalog-category" key={value}>
              <input type="radio" name="category" value={value} defaultChecked={filters.category === value} />{label}</label>)}
          </fieldset>
          <fieldset className="catalog-filter-group"><legend>Kaina, €</legend>
            <div className="catalog-price-inputs"><label>Nuo<input type="number" name="minPrice" min="0" step="0.01"
              placeholder="0" defaultValue={filters.minPrice ?? ''} /></label><span aria-hidden="true">–</span>
              <label>Iki<input type="number" name="maxPrice" min="0" step="0.01" placeholder="Neribojama" defaultValue={filters.maxPrice ?? ''} /></label></div>
            {filterError && <span className="field-error" role="alert">{filterError}</span>}
          </fieldset>
          <div className="catalog-filter-group"><label htmlFor="catalog-supplier">Pardavėjas</label>
            <select id="catalog-supplier" name="supplier" defaultValue={filters.supplier ?? ''}>
              <option value="">Visi pardavėjai</option>{supplierOptions.map((supplier) => <option key={supplier}>{supplier}</option>)}</select>
            {suppliers.isError && <span className="field-error" role="alert">Nepavyko įkelti pardavėjų.
              <button type="button" className="catalog-reset" onClick={() => void suppliers.refetch()}>Bandyti dar kartą</button></span>}
          </div>
          <button type="submit" className="button button--wide">Taikyti filtrus</button>
          <p className="catalog-filter-note">Kainos pagal pasiūlymus sandėlyje. Rodoma mažiausia pasirinkto pardavėjo ir kainos filtrus atitinkanti kaina.</p>
        </form>
      </aside>
      <div className="catalog-results">
        <div className="catalog-toolbar"><div><strong>Komponentai</strong><span className="muted" aria-live="polite">
          {query.data ? `Rasta: ${query.data.page.totalElements}` : 'Kraunama…'}</span></div>
          <label>Rikiuoti pagal<select value={sort} onChange={(event) => changeParam('sort', event.target.value)}>
            <option value="name,asc">Pavadinimą: A–Ž</option><option value="name,desc">Pavadinimą: Ž–A</option>
            <option value="manufacturer,asc">Gamintoją</option><option value="category,asc">Komponento tipą</option></select></label></div>
        {activeFilters.length > 0 && <div className="catalog-active-filters" aria-label="Taikomi filtrai">
          {activeFilters.map((name) => <button key={name} type="button" onClick={() => changeParam(name, '')}
            aria-label={`Pašalinti filtrą: ${filterLabel(name)}`}>{filterLabel(name)}<X aria-hidden="true" /></button>)}
        </div>}
        {query.isPending ? <div className="catalog-loading" role="status" aria-label="Kraunamas katalogas">
          {[0, 1, 2, 3, 4].map((row) => <div className="catalog-loading__row skeleton" key={row}><div className="skeleton__line" /></div>)}
        </div> : query.isError ? <ErrorState message={problemMessage(query.error)} /> : items.length === 0 ?
          <EmptyState title="Komponentų nerasta" detail="Pakeiskite paieškos filtrus arba juos išvalykite." /> : <>
            <div className="catalog-table-scroll" role="region" aria-label="Komponentų rezultatų lentelė" tabIndex={0}>
              <table className="catalog-table"><caption className="sr-only">Komponentai ir mažiausios filtrus atitinkančių pasiūlymų kainos</caption>
                <thead><tr><th scope="col">Komponentas</th><th scope="col">Tipas</th><th scope="col">Gamintojas</th>
                  <th scope="col">Galia</th><th scope="col">Pardavėjas</th><th scope="col" className="catalog-table__price">Kaina nuo</th>
                  <th scope="col"><span className="sr-only">Veiksmai</span></th></tr></thead>
                <tbody>{items.map((component) => <tr key={component.id}>
                  <td><div className="catalog-product"><span className={`catalog-product__icon component-card__visual--${component.category.toLowerCase()}`}><Cpu aria-hidden="true" /></span>
                    <div><Link className="catalog-product__name" to={`/catalog/${component.id}`}>{component.name}</Link><span className="catalog-product__model">{component.model}</span></div></div></td>
                  <td>{categoryLabels[component.category]}</td><td>{component.manufacturer}</td><td className="catalog-table__power">{component.powerWatts == null ? '—' : `${component.powerWatts} W`}</td>
                  <td>{component.cheapestOffer ? <><span className="catalog-vendor">{component.cheapestOffer.supplierName}</span><small className="catalog-offer-count">Pasiūlymų: {component.offerCount ?? 0}</small></> : <span className="muted">Nėra pasiūlymų</span>}</td>
                  <td className="catalog-table__price">{component.cheapestOffer ? <strong>{money(component.cheapestOffer.priceEur)}</strong> : '—'}</td>
                  <td><div className="catalog-row-actions"><Link className="button button--ghost" to={`/catalog/${component.id}`}>Peržiūrėti<ArrowRight aria-hidden="true" /></Link>
                    {canEdit && <><button type="button" className="icon-button" aria-label={`Redaguoti ${component.name}`} onClick={() => setEditing(component)}><Pencil /></button>
                      <button type="button" className="icon-button icon-button--danger" aria-label={`Trinti ${component.name}`} onClick={() => setDeleting(component)}><Trash2 /></button></>}
                  </div></td>
                </tr>)}</tbody>
              </table>
            </div>
            <Pagination page={query.data!.page} onPage={(number) => changeParam('page', String(number))} />
          </>}
      </div>
    </div>
    {editing && <ComponentFormModal initial={editing === 'new' ? undefined : editing} pending={save.isPending}
      onClose={() => setEditing(null)} onSubmit={async (values) => { await save.mutateAsync(values) }} />}
    {deleting && <ConfirmModal title="Ištrinti komponentą?" detail="Naudojamo komponento ištrinti negalima."
      pending={remove.isPending} onClose={() => setDeleting(null)} onConfirm={() => remove.mutate()} />}
  </section>
}
