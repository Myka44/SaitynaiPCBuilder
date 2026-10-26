import { describe, expect, it } from 'vitest'
import { embeddedItems } from './hal'

describe('embeddedItems', () => {
  it('ištraukia pirmą HAL kolekciją', () => {
    const items = embeddedItems({
      _embedded: { components: [{ id: 'cpu-1' }] },
      _links: { self: { href: '/components' } },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    })
    expect(items).toEqual([{ id: 'cpu-1' }])
  })

  it('tuščiam atsakymui grąžina tuščią masyvą', () => {
    expect(embeddedItems(undefined)).toEqual([])
    expect(embeddedItems({ _links: {}, page: { size: 20, totalElements: 0, totalPages: 0, number: 0 } })).toEqual([])
  })
})
