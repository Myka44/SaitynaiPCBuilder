/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { CheckCircle2, XCircle, X } from 'lucide-react'

type ToastKind = 'success' | 'error'
interface ToastItem { id: number; message: string; kind: ToastKind }
interface ToastContextValue { showToast: (message: string, kind?: ToastKind) => void }
const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])
  const value = useMemo(() => ({
    showToast(message: string, kind: ToastKind = 'success') {
      const id = Date.now() + Math.random()
      setItems((current) => [...current, { id, message, kind }])
      window.setTimeout(() => setItems((current) => current.filter((item) => item.id !== id)), 4200)
    },
  }), [])
  const remove = (id: number) => setItems((current) => current.filter((item) => item.id !== id))
  return <ToastContext.Provider value={value}>
    {children}
    <div className="toast-stack" aria-live="polite">
      {items.map((item) => <div className={`toast toast--${item.kind}`} key={item.id}>
        {item.kind === 'success' ? <CheckCircle2 aria-hidden="true" /> : <XCircle aria-hidden="true" />}
        <span>{item.message}</span>
        <button type="button" onClick={() => remove(item.id)} aria-label="Uždaryti pranešimą"><X /></button>
      </div>)}
    </div>
  </ToastContext.Provider>
}

export function useToast() {
  const value = useContext(ToastContext)
  if (!value) throw new Error('useToast turi būti naudojamas ToastProvider viduje')
  return value
}
