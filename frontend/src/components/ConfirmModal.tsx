import { AlertTriangle } from 'lucide-react'
import { Modal } from './Modal'

export function ConfirmModal({ title, detail, pending, onClose, onConfirm }: {
  title: string; detail: string; pending: boolean; onClose: () => void; onConfirm: () => void
}) {
  return <Modal title={title} onClose={onClose}>
    <div className="confirm"><AlertTriangle aria-hidden="true" /><p>{detail}</p></div>
    <div className="form__actions"><button type="button" className="button button--ghost" onClick={onClose}>Atšaukti</button>
      <button type="button" className="button button--danger" onClick={onConfirm} disabled={pending}>
        {pending ? 'Trinama…' : 'Ištrinti'}</button></div>
  </Modal>
}

