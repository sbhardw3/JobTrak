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
      await loadApplications()
    } catch {
      setError('Application could not be saved.')
    } finally {
      setSubmitting(false)
    }
  }

  function startEdit(application) {
    setEditingId(application.id)
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

  return (
    <AppShell eyebrow="Tracker" title="Applications">
      <section className="workspace-layout wide">
        <form className="data-form" onSubmit={handleSubmit}>
          <div>
            <p className="metric-label">{editingId ? 'Editing' : 'New application'}</p>
            <h2>{editingId ? 'Update application' : 'Save application'}</h2>
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

          <label>
            Job URL
            <input name="jobUrl" onChange={handleChange} type="url" value={form.jobUrl} />
          </label>

          <label>
            Status
            <select name="status" onChange={handleChange} value={form.status}>
              {APPLICATION_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>

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

          {error && <p className="form-error">{error}</p>}

          <div className="form-actions">
            <button className="primary-button" disabled={submitting} type="submit">
              {submitting ? 'Saving...' : editingId ? 'Update application' : 'Save application'}
            </button>
            {editingId && (
              <button className="secondary-button" onClick={cancelEdit} type="button">
                Cancel
              </button>
            )}
          </div>
        </form>

        <section className="data-list">
          <div className="section-heading">
            <p className="metric-label">Tracked</p>
            <h2>{applications.length} applications</h2>
          </div>

          {loading ? (
            <p className="muted">Loading applications...</p>
          ) : applications.length === 0 ? (
            <p className="empty-state">No applications saved yet.</p>
          ) : (
            applications.map((application) => (
              <article className="list-item application-item" key={application.id}>
                <div>
                  <div className="item-title-row">
                    <h3>{application.jobTitle}</h3>
                    <span className={`status-tag status-${application.status.toLowerCase()}`}>
                      {application.status}
                    </span>
                  </div>
                  <p className="company-name">{application.company}</p>
                  {application.jobDescription && <p>{application.jobDescription}</p>}
                  {application.jobUrl && (
                    <a href={application.jobUrl} rel="noreferrer" target="_blank">
                      Job link
                    </a>
                  )}
                </div>
                <div className="item-actions">
                  <select
                    aria-label={`Status for ${application.company}`}
                    onChange={(event) => handleStatusChange(application.id, event.target.value)}
                    value={application.status}
                  >
                    {APPLICATION_STATUSES.map((status) => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                  </select>
                  <button
                    className="secondary-button"
                    onClick={() => startEdit(application)}
                    type="button"
                  >
                    Edit
                  </button>
                  <button
                    className="danger-button"
                    onClick={() => handleDelete(application.id)}
                    type="button"
                  >
                    Delete
                  </button>
                </div>
              </article>
            ))
          )}
        </section>
      </section>
    </AppShell>
  )
}

export default ApplicationsPage
