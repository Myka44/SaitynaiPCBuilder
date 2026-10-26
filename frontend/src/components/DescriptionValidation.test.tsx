import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ComponentFormModal } from './ComponentFormModal'
import { ConfigurationFormModal } from './ConfigurationFormModal'

afterEach(cleanup)
describe.each(['component', 'configuration'] as const)('%s aprašymo validacija', (kind) => {
  it('rodo klaidą ties 1001 simboliu ir leidžia išsaugoti sutrumpinus iki 1000', async () => {
    const submit = vi.fn().mockResolvedValue(undefined)
    render(kind === 'component'
      ? <ComponentFormModal pending={false} onClose={() => undefined} onSubmit={submit} />
      : <ConfigurationFormModal pending={false} onClose={() => undefined} onSubmit={submit} />)
    fireEvent.change(screen.getByLabelText('Pavadinimas'), { target: { value: 'Tinkamas pavadinimas' } })
    if (kind === 'component') {
      fireEvent.change(screen.getByLabelText('Gamintojas'), { target: { value: 'AMD' } })
      fireEvent.change(screen.getByLabelText('Modelis'), { target: { value: '7700' } })
    }
    const description = screen.getByLabelText('Aprašymas')
    fireEvent.change(description, { target: { value: 'a'.repeat(1001) } })
    expect(await screen.findByRole('alert')).toHaveTextContent('Aprašymas negali viršyti 1000 simbolių.')
    expect(screen.getByText('1001 / 1000 simbolių')).toBeInTheDocument()
    expect(description).toHaveAttribute('aria-invalid', 'true')
    await userEvent.click(screen.getByRole('button', { name: 'Išsaugoti' }))
    expect(submit).not.toHaveBeenCalled()
    fireEvent.change(description, { target: { value: 'a'.repeat(1000) } })
    await waitFor(() => expect(screen.queryByRole('alert')).not.toBeInTheDocument())
    await userEvent.click(screen.getByRole('button', { name: 'Išsaugoti' }))
    await waitFor(() => expect(submit.mock.calls[0]?.[0]).toEqual(expect.objectContaining({ description: 'a'.repeat(1000) })))
  })
})
