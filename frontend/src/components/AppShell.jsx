import { NavLink } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth.js'

function AppShell({ eyebrow, title, children }) {
  const { user, logout } = useAuth()

  return (
    <main className="app-shell">
      <header className="site-header">
        <div className="site-header-inner">
          <NavLink className="brand-link" to="/dashboard" aria-label="JobTrak dashboard">
            <span className="brand-mark">JT</span>
            <div>
              <strong>JobTrak</strong>
              <small>Application command center</small>
            </div>
          </NavLink>

          <nav className="app-nav top-nav" aria-label="Primary navigation">
            <NavLink to="/dashboard">
              <span className="nav-icon">D</span>
              Dashboard
            </NavLink>
            <NavLink to="/resumes">
              <span className="nav-icon">R</span>
              Resumes
            </NavLink>
            <NavLink to="/applications">
              <span className="nav-icon">T</span>
              Tracker
            </NavLink>
            <NavLink to="/ai-analysis">
              <span className="nav-icon">AI</span>
              AI Analysis
            </NavLink>
          </nav>

          <details className="account-menu">
            <summary aria-label="Account menu">
              <span className="account-avatar">{getInitials(user?.name || user?.email || 'User')}</span>
            </summary>
            <div className="account-panel">
              <small>Signed in as</small>
              <strong>{user?.name}</strong>
              <span>{user?.email}</span>
              <button onClick={logout} type="button">
                Log out
              </button>
            </div>
          </details>
        </div>
      </header>

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

function getInitials(value) {
  return value
    .split(/[ @.]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
}

export default AppShell
