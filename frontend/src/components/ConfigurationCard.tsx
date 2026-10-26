import { Link } from 'react-router-dom'
import { ArrowRight, Boxes, BriefcaseBusiness, Eye, EyeOff, Gamepad2, MonitorCog } from 'lucide-react'
import type { Configuration, UsageType } from '../types'
import { usageLabels } from '../types'

const usageIcon: Record<UsageType, typeof Gamepad2> = {
  GAMING: Gamepad2,
  WORKSTATION: MonitorCog,
  OFFICE: BriefcaseBusiness,
  OTHER: Boxes,
}

export function ConfigurationCard({ configuration, edit = false }: { configuration: Configuration; edit?: boolean }) {
  const Icon = usageIcon[configuration.usageType]
  return <article className="card configuration-card">
    <div className="card__eyebrow"><Icon aria-hidden="true" /> {usageLabels[configuration.usageType]}</div>
    <h2>{configuration.name}</h2>
    <p>{configuration.description}</p>
    <dl className="configuration-card__stats">
      <div><dt>Biudžetas</dt><dd>{Number(configuration.budgetEur).toLocaleString('lt-LT', { style: 'currency', currency: 'EUR' })}</dd></div>
      <div><dt>Komponentai</dt><dd>{configuration.componentCount}</dd></div>
    </dl>
    <div className="card__footer">
      <span className="visibility">{configuration.visibility === 'PUBLIC' ? <Eye /> : <EyeOff />}
        {configuration.visibility === 'PUBLIC' ? 'Vieša' : 'Privati'}</span>
      <Link className="text-link" to={edit ? `/app/configurations/${configuration.id}/edit` : `/configurations/${configuration.id}`}>
        {edit ? 'Tvarkyti' : 'Peržiūrėti'} <ArrowRight aria-hidden="true" />
      </Link>
    </div>
  </article>
}

