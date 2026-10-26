import { useForm, useWatch } from 'react-hook-form'
import { Modal } from './Modal'
import type { Configuration, UsageType, Visibility } from '../types'

export interface ConfigurationFormValues {
  name: string
  description: string
  budgetEur: number
  usageType: UsageType
  visibility: Visibility
}

type ConfigurationFormState = Omit<ConfigurationFormValues, 'visibility'> & { isPublic: boolean }

export function ConfigurationFormModal({ initial, pending, onClose, onSubmit }: {
  initial?: Configuration
  pending: boolean
  onClose: () => void
  onSubmit: (values: ConfigurationFormValues) => Promise<void>
}) {
  const { register, handleSubmit, control, formState: { errors } } = useForm<ConfigurationFormState>({
    mode: 'onChange',
    defaultValues: initial ? {
      name: initial.name,
      description: initial.description,
      budgetEur: initial.budgetEur,
      usageType: initial.usageType,
      isPublic: initial.visibility === 'PUBLIC',
    } : { usageType: 'GAMING', isPublic: false, budgetEur: 1200 },
  })
  const description = useWatch({ control, name: 'description', defaultValue: initial?.description ?? '' })
  return <Modal title={initial ? 'Redaguoti komplektaciją' : 'Nauja komplektacija'} onClose={onClose}>
    <form className="form" onSubmit={handleSubmit(({ isPublic, ...values }) => onSubmit({
      ...values,
      visibility: isPublic ? 'PUBLIC' : 'PRIVATE',
    }))}>
      <label>Pavadinimas<input aria-invalid={!!errors.name}
        aria-describedby={errors.name ? 'configuration-name-error' : undefined}
        {...register('name', {
          required: 'Įveskite pavadinimą.',
          validate: (value) => value.trim().length >= 3 || 'Pavadinimą turi sudaryti bent 3 simboliai, neskaičiuojant tarpų pradžioje ir pabaigoje.',
          maxLength: { value: 100, message: 'Pavadinimas negali viršyti 100 simbolių.' },
        })} />
        {errors.name && <span id="configuration-name-error" className="field-error" role="alert">{errors.name.message}</span>}</label>
      <div><label htmlFor="configuration-description">Aprašymas</label>
        <textarea id="configuration-description" rows={4} aria-invalid={!!errors.description}
          aria-describedby="configuration-description-count configuration-description-error"
          {...register('description', { required: 'Įveskite aprašymą.',
            maxLength: { value: 1000, message: 'Aprašymas negali viršyti 1000 simbolių.' } })} />
        <small id="configuration-description-count" className={`character-count${description.length > 1000 ? ' negative' : ''}`}>
          {description.length} / 1000 simbolių</small>
        {errors.description && <span id="configuration-description-error" className="field-error" role="alert">{errors.description.message}</span>}
      </div>
      <div className="form__row">
        <label>Biudžetas, €<input type="number" min="1" max="100000" step="0.01"
          {...register('budgetEur', { required: true, valueAsNumber: true, min: 1, max: 100000 })} /></label>
        <label>Paskirtis<select {...register('usageType')}>
          <option value="GAMING">Žaidimams</option><option value="WORKSTATION">Darbo stotis</option>
          <option value="OFFICE">Biurui</option><option value="OTHER">Kita</option>
        </select></label>
      </div>
      <label className="checkbox-label"><input type="checkbox" {...register('isPublic')} /> Skelbti viešai</label>
      <div className="form__actions"><button type="button" className="button button--ghost" onClick={onClose}>Atšaukti</button>
        <button type="submit" className="button" disabled={pending}>{pending ? 'Saugoma…' : 'Išsaugoti'}</button></div>
    </form>
  </Modal>
}
