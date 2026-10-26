import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Pagination } from './Pagination'

describe('Pagination', () => {
  it('keičia puslapį ir išjungia ankstesnį pirmame puslapyje', async () => {
    const onPage = vi.fn()
    render(<Pagination page={{ number: 0, size: 8, totalElements: 20, totalPages: 3 }} onPage={onPage} />)
    expect(screen.getByRole('button', { name: /ankstesnis/i })).toBeDisabled()
    await userEvent.click(screen.getByRole('button', { name: /kitas/i }))
    expect(onPage).toHaveBeenCalledWith(1)
  })

  it('vieno puslapio atveju nieko nerodo', () => {
    const { container } = render(<Pagination page={{ number: 0, size: 8, totalElements: 2, totalPages: 1 }} onPage={() => undefined} />)
    expect(container).toBeEmptyDOMElement()
  })
})
