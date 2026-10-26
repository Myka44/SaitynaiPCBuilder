import type { HalPage } from '../types'

export function embeddedItems<T>(page: HalPage<T> | undefined): T[] {
  if (!page?._embedded) return []
  const first = Object.values(page._embedded)[0]
  return Array.isArray(first) ? first : []
}

