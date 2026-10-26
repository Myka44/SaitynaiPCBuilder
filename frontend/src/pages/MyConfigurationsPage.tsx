import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { api, problemMessage } from '../api/client'
import { embeddedItems } from '../api/hal'
import { ConfigurationCard } from '../components/ConfigurationCard'
import { ConfigurationFormModal, type ConfigurationFormValues } from '../components/ConfigurationFormModal'
import { EmptyState, ErrorState, LoadingGrid } from '../components/States'
import { useToast } from '../components/Toast'
import type { Configuration, HalPage } from '../types'

export function MyConfigurationsPage() {
  const [creating, setCreating] = useState(false)
  const queryClient = useQueryClient()
  const { showToast } = useToast()
  const query = useQuery({ queryKey: ['configurations', 'mine'], queryFn: async () =>
    (await api.get<HalPage<Configuration>>('/configurations', { params: { owner: 'me', size: 100, sort: 'updatedAt,desc' } })).data })
  const create = useMutation({ mutationFn: async (values: ConfigurationFormValues) => api.post('/configurations', values),
    onSuccess: async () => { await queryClient.invalidateQueries({ queryKey: ['configurations'] }); setCreating(false); showToast('Komplektacija sukurta.') },
    onError: (error) => showToast(problemMessage(error), 'error') })
  const items = embeddedItems(query.data)
  return <section className="page container">
    <div className="section-heading section-heading--actions"><div><h1>Mano komplektacijos</h1></div>
      <button className="button" type="button" onClick={() => setCreating(true)}><Plus /> Nauja komplektacija</button></div>
    {query.isPending ? <LoadingGrid /> : query.isError ? <ErrorState message={problemMessage(query.error)} /> : items.length === 0 ?
      <EmptyState title="Dar nėra komplektacijų" detail="Sukurkite pirmą komplektaciją ir pridėkite komponentus iš katalogo." /> :
      <div className="card-grid">{items.map((item) => <ConfigurationCard configuration={item} edit key={item.id} />)}</div>}
    {creating && <ConfigurationFormModal pending={create.isPending} onClose={() => setCreating(false)}
      onSubmit={async (values) => { await create.mutateAsync(values) }} />}
  </section>
}

