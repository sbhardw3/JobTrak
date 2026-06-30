import { useEffect, useState } from 'react'
import {
  APPLICATION_STATUSES,
  createApplication,
  deleteApplication,
  getApplications,
  updateApplication,
  updateApplicationStatus,
} from '../api/applicationApi.js'
import AppShell from '../components/AppShell.jsx'

const emptyForm = {
  company: '',
  jobTitle: '',
  jobDescription: '',
  jobUrl: '',
  notes: '',
  status: 'SAVED',
}

function ApplicationsPage() {
  const [applications, setApplications] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [activeStatus, setActiveStatus] = useState('ALL')
  const [formOpen, setFormOpen] = useState(false)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadApplications()
  }, [])

  async function loadApplications() {
    setLoading(true)
    setError('')

    try {
      setApplications(await getApplications())
    } catch {
      setError('Applications could not be loaded.')
    } finally {
      setLoading(false)
    }
  }

  function handleChange(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      if (editingId) {
        await updateApplication(editingId, form)
      } else {
        await createApplication(form)
      }
      setForm(emptyForm)
      setEditingId(null)
      setFormOpen(false)
      await loadApplications()
    } catch {
      setError('Application could not be saved.')
    } finally {
      setSubmitting(false)
    }
  }

  function startEdit(application) {
    setEditingId(application.id)
    setFormOpen(true)
    setForm({
      company: application.company,
      jobTitle: application.jobTitle,
      jobDescription: application.jobDescription || '',
      jobUrl: application.jobUrl || '',
      notes: application.notes || '',
      status: application.status,
    })
  }

  function cancelEdit() {
    setEditingId(null)
    setForm(emptyForm)
    setFormOpen(false)
  }

  async function handleDelete(id) {
    setError('')

    try {
      await deleteApplication(id)
      await loadApplications()
      if (editingId === id) {
        cancelEdit()
      }
    } catch {
      setError('Application could not be deleted.')
    }
  }

  async function handleStatusChange(id, status) {
    setError('')

    try {
      const updated = await updateApplicationStatus(id, status)
      setApplications((current) =>
        current.map((application) => (application.id === id ? updated : application)),
      )
    } catch {
      setError('Status could not be updated.')
    }
  }

  const pipelineCounts = APPLICATION_STATUSES.map((status) => ({
    status,
    count: applications.filter((application) => application.status === status).length,
  }))
  const filteredApplications =
    activeStatus === 'ALL'
      ? applications
      : applications.filter((application) => application.status === activeStatus)
  const activeStatusLabel = activeStatus === 'ALL' ? 'All applications' : formatStatus(activeStatus)

  return (
    <AppShell eyebrow="Application pipeline" title="Job Tracker">
      <section className="tracker-page">
        <section className="tracker-hero">
          <div>
            <p className="metric-label">Tracked</p>
            <h2>{applications.length} applications</h2>
            <p>Filter the pipeline by status, update stages quickly, and keep every company-role pair visible.</p>
          </div>
          <button className="primary-button" onClick={() => setActiveStatus('ALL')} type="button">
            View all
          </button>
          <button className="secondary-button" onClick={() => setFormOpen((current) => !current)} type="button">
            {formOpen ? 'Close form' : 'Add application'}
          </button>
        </section>

        {(formOpen || editingId) && (
          <form className="data-form tracker-form" onSubmit={handleSubmit}>
            <div className="section-heading">
              <div>
                <p className="metric-label">{editingId ? 'Editing' : 'New application'}</p>
                <h2>{editingId ? 'Update application' : 'Save application'}</h2>
              </div>
              <button className="secondary-button compact-button" onClick={cancelEdit} type="button">
                Close
              </button>
            </div>

            <div className="form-grid">
              <label>
                Company
                <input name="company" onChange={handleChange} required type="text" value={form.company} />
              </label>

              <label>
                Job title
                <input name="jobTitle" onChange={handleChange} required type="text" value={form.jobTitle} />
              </label>
            </div>

            <div className="form-grid">
              <label>
                Job URL
                <input name="jobUrl" onChange={handleChange} type="url" value={form.jobUrl} />
              </label>

              <label>
                Status
                <select name="status" onChange={handleChange} value={form.status}>
                  {APPLICATION_STATUSES.map((status) => (
                    <option key={status} value={status}>
                      {formatStatus(status)}
                    </option>
                  ))}
                </select>
              </label>
            </div>

            <div className="form-grid">
              <label>
                Job description
                <textarea
                  name="jobDescription"
                  onChange={handleChange}
                  rows="8"
                  value={form.jobDescription}
                />
              </label>

              <label>
                Notes
                <textarea name="notes" onChange={handleChange} rows="4" value={form.notes} />
              </label>
            </div>

            {error && <p className="form-error">{error}</p>}

            <div className="form-actions">
              <button className="primary-button" disabled={submitting} type="submit">
                {submitting ? 'Saving...' : editingId ? 'Update application' : 'Save application'}
              </button>
            </div>
          </form>
        )}

        {error && !formOpen && !editingId && <p className="form-error">{error}</p>}

        <section className="tracker-summary" aria-label="Application status filters">
          <button
            className={`tracker-chip tracker-chip-all ${activeStatus === 'ALL' ? 'active' : ''}`}
            onClick={() => setActiveStatus('ALL')}
            type="button"
          >
            <span className="status-dot status-dot-all" />
            <strong>{applications.length}</strong>
            <small>All</small>
          </button>
          {pipelineCounts.map((item) => (
            <button
              className={`tracker-chip ${activeStatus === item.status ? 'active' : ''}`}
              key={item.status}
              onClick={() => setActiveStatus(item.status)}
              type="button"
            >
              <span className={`status-dot status-dot-${item.status.toLowerCase()}`} />
              <strong>{item.count}</strong>
              <small>{formatStatus(item.status)}</small>
            </button>
          ))}
        </section>

        <section className="tracker-content">
          <section className="tracker-list-panel">
            <div className="section-heading">
              <div>
                <p className="metric-label">Showing</p>
                <h2>
                  {activeStatusLabel} <span>{filteredApplications.length}</span>
                </h2>
              </div>
              {activeStatus !== 'ALL' && (
                <button className="secondary-button compact-button" onClick={() => setActiveStatus('ALL')} type="button">
                  Clear filter
                </button>
              )}
            </div>

            {loading ? (
              <p className="muted">Loading applications...</p>
            ) : filteredApplications.length === 0 ? (
              <p className="empty-state">No applications in this status yet.</p>
            ) : (
              <div className="application-card-grid">
                {filteredApplications.map((application) => (
                  <article className="application-card" key={application.id}>
                    <div className="application-card-header">
                      <span className="company-avatar">{getCompanyInitials(application.company)}</span>
                      <div>
                        <p className="company-name">{application.company}</p>
                        <h3>{application.jobTitle}</h3>
                      </div>
                      <span className={`status-tag status-${application.status.toLowerCase()}`}>
                        {formatStatus(application.status)}
                      </span>
                    </div>

                    <div className="application-meta">
                      <span>Updated {formatDate(application.updatedAt || application.createdAt)}</span>
                      {application.jobUrl && (
                        <a href={application.jobUrl} rel="noreferrer" target="_blank">
                          View posting
                        </a>
                      )}
                    </div>

                    {application.jobDescription && (
                      <p className="application-preview">{application.jobDescription}</p>
                    )}
                    {application.notes && <p className="application-note">{application.notes}</p>}

                    <div className="application-card-actions">
                      <select
                        aria-label={`Status for ${application.company}`}
                        onChange={(event) => handleStatusChange(application.id, event.target.value)}
                        value={application.status}
                      >
                        {APPLICATION_STATUSES.map((status) => (
                          <option key={status} value={status}>
                            {formatStatus(status)}
                          </option>
                        ))}
                      </select>
                      <details className="action-menu">
                        <summary aria-label={`Actions for ${application.company}`}>...</summary>
                        <div>
                          <button onClick={() => startEdit(application)} type="button">
                            Edit
                          </button>
                          <button className="danger-menu-action" onClick={() => handleDelete(application.id)} type="button">
                            Delete
                          </button>
                        </div>
                      </details>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </section>
        </section>
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
    return 'recently'
  }

  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
  }).format(new Date(value))
}

export default ApplicationsPage
