import { useForm, useWatch } from 'react-hook-form'
import { Modal } from './Modal'
import type { Component, ComponentCategory } from '../types'
import { categoryLabels } from '../types'

export interface ComponentFormValues {
  name: string
  category: ComponentCategory
  manufacturer: string
  model: string
  description: string
  powerWatts: number | null
}

export function ComponentFormModal({ initial, pending, onClose, onSubmit }: {
  initial?: Component
  pending: boolean
  onClose: () => void
  onSubmit: (values: ComponentFormValues) => Promise<void>
}) {
  const { register, handleSubmit, control, formState: { errors } } = useForm<ComponentFormValues>({
    mode: 'onChange',
    defaultValues: initial ?? { category: 'CPU', powerWatts: null },
  })
  const description = useWatch({ control, name: 'description', defaultValue: initial?.description ?? '' })
  return <Modal title={initial ? 'Redaguoti komponentą' : 'Naujas komponentas'} onClose={onClose}>
    <form className="form" onSubmit={handleSubmit(onSubmit)}>
      <label>Pavadinimas<input aria-invalid={!!errors.name}
        aria-describedby={errors.name ? 'component-name-error' : undefined}
        {...register('name', {
          required: 'Įveskite pavadinimą.',
          validate: (value) => value.trim().length > 0 || 'Pavadinimas negali būti sudarytas tik iš tarpų.',
          maxLength: { value: 120, message: 'Pavadinimas negali viršyti 120 simbolių.' },
        })} />
        {errors.name && <span id="component-name-error" className="field-error" role="alert">{errors.name.message}</span>}</label>
      <div className="form__row"><label>Kategorija<select {...register('category')}>
        {Object.entries(categoryLabels).map(([value, label]) => <option value={value} key={value}>{label}</option>)}
      </select></label><label>Galia, W<input type="number" min="0" max="2000"
        {...register('powerWatts', { setValueAs: (value) => value === '' ? null : Number(value) })} /></label></div>
      <div className="form__row"><label>Gamintojas<input {...register('manufacturer', { required: true, maxLength: 80 })} /></label>
        <label>Modelis<input {...register('model', { required: true, maxLength: 100 })} /></label></div>
      <div><label htmlFor="component-description">Aprašymas</label>
        <textarea id="component-description" rows={4} aria-invalid={!!errors.description}
          aria-describedby="component-description-count component-description-error"
          {...register('description', { required: 'Įveskite aprašymą.',
            maxLength: { value: 1000, message: 'Aprašymas negali viršyti 1000 simbolių.' } })} />
        <small id="component-description-count" className={`character-count${description.length > 1000 ? ' negative' : ''}`}>
          {description.length} / 1000 simbolių</small>
        {errors.description && <span id="component-description-error" className="field-error" role="alert">{errors.description.message}</span>}
      </div>
      <div className="form__actions"><button type="button" className="button button--ghost" onClick={onClose}>Atšaukti</button>
        <button type="submit" className="button" disabled={pending}>{pending ? 'Saugoma…' : 'Išsaugoti'}</button></div>
    </form>
  </Modal>
}

