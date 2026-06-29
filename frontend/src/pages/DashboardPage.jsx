import { useAuth } from '../hooks/useAuth.js'

function DashboardPage() {
  const { user, logout } = useAuth()

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">JobTrak</p>
          <h1>Dashboard</h1>
        </div>
        <button className="secondary-button" onClick={logout} type="button">
          Log out
        </button>
      </header>

      <section className="welcome-band">
        <div>
          <h2>Welcome, {user?.name}</h2>
          <p>{user?.email}</p>
        </div>
        <span className="status-pill">Authenticated</span>
      </section>

      <section className="dashboard-grid">
        <article>
          <p className="metric-label">Phase 5 Part 1</p>
          <h2>Auth connected</h2>
          <p>Signup, login, JWT storage, session restore, and protected routing are wired.</p>
        </article>
        <article>
          <p className="metric-label">Next</p>
          <h2>Resume UI</h2>
          <p>Part 2 will connect resume and application CRUD screens to the backend.</p>
        </article>
        <article>
          <p className="metric-label">Later</p>
          <h2>AI workflow</h2>
          <p>Part 3 will connect the AI analysis endpoint and show saved analysis history.</p>
        </article>
      </section>
    </main>
  )
}

export default DashboardPage
