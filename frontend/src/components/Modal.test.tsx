import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { Modal } from './Modal'

describe('Modal', () => {
  it('užsidaro Escape klavišu', async () => {
    const onClose = vi.fn()
    render(<Modal title="Bandomasis modalas" onClose={onClose}><p>Turinys</p></Modal>)
    expect(screen.getByRole('dialog')).toHaveAccessibleName('Bandomasis modalas')
    await userEvent.keyboard('{Escape}')
    expect(onClose).toHaveBeenCalledOnce()
  })
})
