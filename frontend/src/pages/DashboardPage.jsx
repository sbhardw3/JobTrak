import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getApplications } from '../api/applicationApi.js'
import { getResumes } from '../api/resumeApi.js'
import AppShell from '../components/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.js'

function DashboardPage() {
  const { user } = useAuth()
  const [metrics, setMetrics] = useState({ resumes: 0, applications: 0, active: 0 })
  const [error, setError] = useState('')

  useEffect(() => {
    let isMounted = true

    async function loadMetrics() {
      try {
        const [resumes, applications] = await Promise.all([getResumes(), getApplications()])
        const active = applications.filter(
          (application) => !['REJECTED', 'OFFER', 'GHOSTED'].includes(application.status),
        ).length

        if (isMounted) {
          setMetrics({ resumes: resumes.length, applications: applications.length, active })
        }
      } catch {
        if (isMounted) {
          setError('Dashboard data could not be loaded.')
        }
      }
    }

    loadMetrics()

    return () => {
      isMounted = false
    }
  }, [])

  return (
    <AppShell eyebrow="JobTrak" title="Dashboard">
      <section className="welcome-band">
        <div>
          <h2>Welcome, {user?.name}</h2>
          <p>{user?.email}</p>
        </div>
        <span className="status-pill">Authenticated</span>
      </section>

      {error && <p className="form-error page-error">{error}</p>}

      <section className="dashboard-grid">
        <article>
          <p className="metric-label">Resumes</p>
          <h2>{metrics.resumes}</h2>
          <Link to="/resumes">Manage resumes</Link>
        </article>
        <article>
          <p className="metric-label">Applications</p>
          <h2>{metrics.applications}</h2>
          <Link to="/applications">Manage applications</Link>
        </article>
        <article>
          <p className="metric-label">Active</p>
          <h2>{metrics.active}</h2>
          <Link to="/applications">View tracker</Link>
        </article>
      </section>
    </AppShell>
  )
}

export default DashboardPage
