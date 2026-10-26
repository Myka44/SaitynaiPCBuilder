import { AlertTriangle, Inbox } from 'lucide-react'

export function LoadingGrid() {
  return <div className="card-grid" aria-label="Kraunama">
    {[0, 1, 2].map((item) => <div className="card skeleton" key={item} aria-hidden="true">
      <div className="skeleton__line skeleton__line--short" />
      <div className="skeleton__line" />
      <div className="skeleton__line" />
    </div>)}
  </div>
}

export function EmptyState({ title, detail }: { title: string; detail: string }) {
  return <div className="empty-state"><Inbox aria-hidden="true" /><h2>{title}</h2><p>{detail}</p></div>
}

export function ErrorState({ message }: { message: string }) {
  return <div className="error-state" role="alert"><AlertTriangle aria-hidden="true" /><div><strong>Nepavyko įkelti</strong><p>{message}</p></div></div>
}

