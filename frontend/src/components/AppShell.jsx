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

          <div className="top-user">
            <div className="top-user-copy">
              <small>Signed in as</small>
              <span>{user?.name}</span>
            </div>
            <button className="secondary-button logout-button" onClick={logout} type="button">
              Log out
            </button>
          </div>
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

export default AppShell
