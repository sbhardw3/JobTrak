import { NavLink } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.js'

function AppShell({ eyebrow, title, children }) {
  const { user, logout } = useAuth()

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <span className="brand-mark">JT</span>
          <div>
            <strong>JobTrak</strong>
            <small>AI job search hub</small>
          </div>
        </div>

        <nav className="app-nav" aria-label="Primary navigation">
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/resumes">Resumes</NavLink>
          <NavLink to="/applications">Applications</NavLink>
          <NavLink to="/ai-analysis">AI Analysis</NavLink>
        </nav>

        <div className="sidebar-user">
          <span>{user?.name}</span>
          <small>{user?.email}</small>
          <button className="secondary-button" onClick={logout} type="button">
            Log out
          </button>
        </div>
      </aside>

      <section className="main-content">
        <header className="topbar">
          <div>
            <p className="eyebrow">{eyebrow}</p>
            <h1>{title}</h1>
          </div>
        </header>

        {children}
      </section>
    </main>
  )
}

export default AppShell
