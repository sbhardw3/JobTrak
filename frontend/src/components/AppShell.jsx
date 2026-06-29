import { NavLink } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.js'

function AppShell({ eyebrow, title, children }) {
  const { user, logout } = useAuth()

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">{eyebrow}</p>
          <h1>{title}</h1>
        </div>
        <div className="topbar-actions">
          <span>{user?.name}</span>
          <button className="secondary-button" onClick={logout} type="button">
            Log out
          </button>
        </div>
      </header>

      <nav className="app-nav" aria-label="Primary navigation">
        <NavLink to="/dashboard">Dashboard</NavLink>
        <NavLink to="/resumes">Resumes</NavLink>
        <NavLink to="/applications">Applications</NavLink>
        <NavLink to="/ai-analysis">AI Analysis</NavLink>
      </nav>

      {children}
    </main>
  )
}

export default AppShell
