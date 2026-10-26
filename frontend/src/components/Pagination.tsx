import { ChevronLeft, ChevronRight } from 'lucide-react'
import type { PageInfo } from '../types'

export function Pagination({ page, onPage }: { page: PageInfo; onPage: (page: number) => void }) {
  if (page.totalPages <= 1) return null
  return <nav className="pagination" aria-label="Puslapiavimas">
    <button type="button" disabled={page.number === 0} onClick={() => onPage(page.number - 1)}>
      <ChevronLeft aria-hidden="true" /> Ankstesnis
    </button>
    <span>{page.number + 1} iš {page.totalPages}</span>
    <button type="button" disabled={page.number + 1 >= page.totalPages} onClick={() => onPage(page.number + 1)}>
      Kitas <ChevronRight aria-hidden="true" />
    </button>
  </nav>
}

