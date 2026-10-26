import { Ban, SearchX } from 'lucide-react'
import { Link } from 'react-router-dom'

export function StatusPage({ forbidden = false }: { forbidden?: boolean }) {
  return <section className="page container status-page">{forbidden ? <Ban /> : <SearchX />}
    <span className="kicker">{forbidden ? '403' : '404'}</span><h1>{forbidden ? 'Nepakanka teisių' : 'Puslapis nerastas'}</h1>
    <p>{forbidden ? 'Jūsų rolė neleidžia atverti šio puslapio.' : 'Patikrinkite adresą arba grįžkite į pradžią.'}</p>
    <Link className="button" to="/">Grįžti į pradžią</Link></section>
}

