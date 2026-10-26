import { useState } from 'react'
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Boxes, Cpu, LogIn, LogOut, Menu, ShieldCheck, UserRound, X } from 'lucide-react'
import { useAuth } from '../auth/AuthContext'

export function Layout() {
  const [open, setOpen] = useState(false)
  const { user, logout, hasRole } = useAuth()
  const navigate = useNavigate()
  const close = () => setOpen(false)
  const signOut = async () => { await logout(); close(); navigate('/') }

  return <div className="app-shell">
    <header className="site-header">
      <div className="container site-header__inner">
        <Link className="brand" to="/" onClick={close} aria-label="PC Komplektatorius pradžia">
          <span className="brand__mark"><Cpu aria-hidden="true" /></span>
          <span>PC Komplektatorius</span>
        </Link>
        <button className="menu-button" type="button" onClick={() => setOpen((value) => !value)}
                aria-expanded={open} aria-controls="main-navigation" aria-label="Atverti meniu">
          {open ? <X /> : <Menu />}
        </button>
        <nav id="main-navigation" className={`site-nav ${open ? 'site-nav--open' : ''}`} aria-label="Pagrindinis meniu">
          <NavLink to="/catalog" onClick={close}><Cpu aria-hidden="true" /> Katalogas</NavLink>
          {user && <NavLink to="/app/configurations" onClick={close}><Boxes aria-hidden="true" /> Mano komplektacijos</NavLink>}
          {hasRole('ADMIN') && <NavLink to="/admin/configurations" onClick={close}><ShieldCheck aria-hidden="true" /> Administravimas</NavLink>}
          {user ? <>
            <span className="user-chip"><UserRound aria-hidden="true" /> {user.email}<small>{user.role}</small></span>
            <button className="nav-button" type="button" onClick={signOut}><LogOut aria-hidden="true" /> Atsijungti</button>
          </> : <NavLink className="nav-cta" to="/login" onClick={close}><LogIn aria-hidden="true" /> Prisijungti</NavLink>}
        </nav>
      </div>
    </header>
    <main><Outlet /></main>
    <footer className="site-footer">
      <div className="container"><p>PC Komplektatorius</p><p>Visi tiekėjai ir kainos yra demonstraciniai.</p></div>
    </footer>
  </div>
}

