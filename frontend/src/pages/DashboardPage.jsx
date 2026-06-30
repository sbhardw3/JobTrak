import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAnalyses } from '../api/aiApi.js'
import { getApplications } from '../api/applicationApi.js'
import { getResumes } from '../api/resumeApi.js'
import AppShell from '../components/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.js'

function DashboardPage() {
  const { user } = useAuth()
  const [metrics, setMetrics] = useState({ resumes: 0, applications: 0, active: 0, analyses: 0 })
  const [error, setError] = useState('')

  useEffect(() => {
    let isMounted = true

    async function loadMetrics() {
      try {
        const [resumes, applications, analyses] = await Promise.all([
          getResumes(),
          getApplications(),
          getAnalyses(),
        ])
        const active = applications.filter(
          (application) => !['REJECTED', 'OFFER', 'GHOSTED'].includes(application.status),
        ).length

        if (isMounted) {
          setMetrics({
            resumes: resumes.length,
            applications: applications.length,
            active,
            analyses: analyses.length,
          })
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
      <section className="hero-band">
        <div>
          <p className="eyebrow">Command center</p>
          <h2>Welcome back, {user?.name}</h2>
          <p>Track every application, tune your resume, and prepare stronger outreach from one workspace.</p>
        </div>
        <div className="hero-actions">
          <Link className="primary-link-button" to="/applications">
            Track a job
          </Link>
          <Link className="secondary-link-button" to="/ai-analysis">
            Run AI analysis
          </Link>
        </div>
      </section>

      {error && <p className="form-error page-error">{error}</p>}

      <section className="dashboard-grid">
        <article className="metric-card metric-card-teal">
          <p className="metric-label">Resumes</p>
          <h2>{metrics.resumes}</h2>
          <p>Saved resume versions ready for tailoring.</p>
          <Link to="/resumes">Manage resumes</Link>
        </article>
        <article className="metric-card metric-card-blue">
          <p className="metric-label">Applications</p>
          <h2>{metrics.applications}</h2>
          <p>Jobs you are saving, applying to, or tracking.</p>
          <Link to="/applications">Manage applications</Link>
        </article>
        <article className="metric-card metric-card-amber">
          <p className="metric-label">Active</p>
          <h2>{metrics.active}</h2>
          <p>Open opportunities still moving through your pipeline.</p>
          <Link to="/applications">View tracker</Link>
        </article>
        <article className="metric-card metric-card-violet">
          <p className="metric-label">AI analyses</p>
          <h2>{metrics.analyses}</h2>
          <p>Resume-to-job matches with improvement suggestions.</p>
          <Link to="/ai-analysis">Run analysis</Link>
        </article>
      </section>
    </AppShell>
  )
}

export default DashboardPage
