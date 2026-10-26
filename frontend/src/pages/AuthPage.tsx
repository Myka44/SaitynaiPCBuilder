import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { LockKeyhole, Mail } from 'lucide-react'
import { problemMessage } from '../api/client'
import { useAuth } from '../auth/AuthContext'

interface Values { email: string; password: string }

export function AuthPage({ mode }: { mode: 'login' | 'register' }) {
  const { user, login, register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState('')
  const [pending, setPending] = useState(false)
  const { register: field, handleSubmit, formState: { errors } } = useForm<Values>({
    defaultValues: { email: mode === 'login' ? 'narys@example.lt' : '', password: mode === 'login' ? 'Password1' : '' },
  })
  if (user) return <Navigate to="/app/configurations" replace />
  const submit = async (values: Values) => {
    setPending(true); setError('')
    try {
      await (mode === 'login' ? login(values.email, values.password) : register(values.email, values.password))
      const destination = (location.state as { from?: string } | null)?.from || '/app/configurations'
      navigate(destination, { replace: true })
    } catch (reason) { setError(problemMessage(reason, 'Prisijungti nepavyko.')) } finally { setPending(false) }
  }
  return <section className="auth-page"><div className="auth-card"><div className="auth-card__intro">
    {mode === 'register' && <span className="kicker">Nauja paskyra</span>}
    <h1>{mode === 'login' ? 'Prisijungimas' : 'Registracija'}</h1>
    {mode === 'register' && <p>Registruojama paskyra visada gauna MEMBER rolę.</p>}</div>
    <form className="form" onSubmit={handleSubmit(submit)}>
      {error && <div className="inline-error" role="alert">{error}</div>}
      <label>El. paštas<div className="input-with-icon"><Mail /><input type="email" autoComplete="email"
        {...field('email', { required: 'Įveskite el. paštą' })} /></div>{errors.email && <span className="field-error">{errors.email.message}</span>}</label>
      <label>Slaptažodis<div className="input-with-icon"><LockKeyhole /><input type="password"
        autoComplete={mode === 'login' ? 'current-password' : 'new-password'} {...field('password', mode === 'register' ? {
          required: true, minLength: 8, pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/,
        } : { required: true })} /></div></label>
      <button className="button button--wide" type="submit" disabled={pending}>{pending ? 'Prašome palaukti…' : mode === 'login' ? 'Prisijungti' : 'Registruotis'}</button>
    </form>
    <p className="auth-switch">{mode === 'login' ? 'Dar neturite paskyros?' : 'Jau turite paskyrą?'} <Link to={mode === 'login' ? '/register' : '/login'}>{mode === 'login' ? 'Registruotis' : 'Prisijungti'}</Link></p>
  </div></section>
}
