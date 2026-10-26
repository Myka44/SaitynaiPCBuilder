import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { ConfigurationFormModal } from './ConfigurationFormModal'

describe('ConfigurationFormModal', () => {
  it('validuoja ir paverčia viešumo žymą į API reikšmę', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    const user = userEvent.setup()
    render(<ConfigurationFormModal pending={false} onClose={() => undefined} onSubmit={onSubmit} />)

    await user.type(screen.getByLabelText(/pavadinimas/i), 'Žaidimų kompiuteris')
    await user.type(screen.getByLabelText(/aprašymas/i), 'Subalansuota demonstracinė komplektacija')
    await user.click(screen.getByLabelText(/skelbti viešai/i))
    await user.click(screen.getByRole('button', { name: /išsaugoti/i }))

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({
      name: 'Žaidimų kompiuteris',
      visibility: 'PUBLIC',
      budgetEur: 1200,
    })))
  })
})
