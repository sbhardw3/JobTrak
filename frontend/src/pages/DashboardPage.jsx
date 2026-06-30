import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getAnalyses } from '../api/aiApi.js'
import { APPLICATION_STATUSES, getApplications } from '../api/applicationApi.js'
import { getResumes } from '../api/resumeApi.js'
import AppShell from '../components/AppShell.jsx'
import { useAuth } from '../hooks/useAuth.js'

function DashboardPage() {
  const { user } = useAuth()
  const [metrics, setMetrics] = useState({ resumes: 0, applications: 0, active: 0, analyses: 0 })
  const [recentApplications, setRecentApplications] = useState([])
  const [pipelineCounts, setPipelineCounts] = useState([])
  const [latestAnalysis, setLatestAnalysis] = useState(null)
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
          setPipelineCounts(
            APPLICATION_STATUSES.map((status) => ({
              status,
              count: applications.filter((application) => application.status === status).length,
            })),
          )
          setRecentApplications(applications.slice(0, 4))
          setLatestAnalysis(analyses[0] || null)
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

      <section className="dashboard-command">
        <section>
          <div className="dashboard-grid">
            <article className="metric-card metric-card-teal">
              <p className="metric-label">Resumes</p>
              <h2>{metrics.resumes}</h2>
              <p>Saved versions ready for tailoring.</p>
              <Link to="/resumes">Manage</Link>
            </article>
            <article className="metric-card metric-card-blue">
              <p className="metric-label">Applications</p>
              <h2>{metrics.applications}</h2>
              <p>Tracked company-role pairs.</p>
              <Link to="/applications">Open tracker</Link>
            </article>
            <article className="metric-card metric-card-amber">
              <p className="metric-label">Active</p>
              <h2>{metrics.active}</h2>
              <p>Still moving in the pipeline.</p>
              <Link to="/applications">Review</Link>
            </article>
            <article className="metric-card metric-card-violet">
              <p className="metric-label">AI analyses</p>
              <h2>{metrics.analyses}</h2>
              <p>Saved resume-job reports.</p>
              <Link to="/ai-analysis">Analyze</Link>
            </article>
          </div>

          <section className="dashboard-panel">
            <div className="section-heading">
              <div>
                <p className="metric-label">Recent applications</p>
                <h2>Latest movement</h2>
              </div>
              <Link className="secondary-link-button compact-button" to="/applications">
                View all
              </Link>
            </div>
            {recentApplications.length === 0 ? (
              <p className="empty-state">No applications tracked yet.</p>
            ) : (
              <div className="dashboard-recent-list">
                {recentApplications.map((application) => (
                  <article className="dashboard-recent-item" key={application.id}>
                    <span className="company-avatar">{getCompanyInitials(application.company)}</span>
                    <div>
                      <strong>{application.company}</strong>
                      <small>{application.jobTitle}</small>
                    </div>
                    <span className={`status-tag status-${application.status.toLowerCase()}`}>
                      {formatStatus(application.status)}
                    </span>
                  </article>
                ))}
              </div>
            )}
          </section>
        </section>

        <aside className="dashboard-side">
          <section className="dashboard-panel">
            <p className="metric-label">Pipeline</p>
            <div className="pipeline-mini">
              {pipelineCounts.map((item) => (
                <div className="pipeline-mini-row" key={item.status}>
                  <span>{formatStatus(item.status)}</span>
                  <strong>{item.count}</strong>
                </div>
              ))}
            </div>
          </section>

          <section className="dashboard-panel">
            <p className="metric-label">Latest AI</p>
            {latestAnalysis ? (
              <div className="latest-analysis-card">
                <strong>{latestAnalysis.matchScore}% match</strong>
                <span>{latestAnalysis.source}</span>
                <small>{formatDate(latestAnalysis.createdAt)}</small>
                <Link to="/ai-analysis">Open analysis</Link>
              </div>
            ) : (
              <p className="empty-state">No analysis yet.</p>
            )}
          </section>

          <section className="dashboard-panel">
            <p className="metric-label">Next actions</p>
            <div className="quick-action-list">
              <Link to="/applications">Add an application</Link>
              <Link to="/resumes">Upload a resume</Link>
              <Link to="/ai-analysis">Run resume analysis</Link>
            </div>
          </section>
        </aside>
      </section>
    </AppShell>
  )
}

function formatStatus(status) {
  if (status === 'OA') {
    return 'OA'
  }

  return status
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
}

function getCompanyInitials(company) {
  return company
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word.charAt(0).toUpperCase())
    .join('')
}

function formatDate(value) {
  if (!value) {
    return 'recent'
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value))
}

export default DashboardPage
