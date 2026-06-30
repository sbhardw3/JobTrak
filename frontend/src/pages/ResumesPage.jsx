import { useEffect, useState } from 'react'
import {
  createResume,
  deleteResume,
  extractResumeFile,
  getResumes,
  updateResume,
} from '../api/resumeApi.js'
import AppShell from '../components/AppShell.jsx'

const emptyForm = {
  title: '',
  content: '',
}

function ResumesPage() {
  const [resumes, setResumes] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadResumes()
  }, [])

  async function loadResumes() {
    setLoading(true)
    setError('')

    try {
      setResumes(await getResumes())
    } catch {
      setError('Resumes could not be loaded.')
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

  async function handleFileUpload(event) {
    const file = event.target.files?.[0]

    if (!file) {
      return
    }

    try {
      const parsedResume = await extractResumeFile(file)
      setForm({ title: parsedResume.title, content: parsedResume.content })
      setEditingId(null)
      setError('')
    } catch (err) {
      setError(getUploadErrorMessage(err))
    } finally {
      event.target.value = ''
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    try {
      if (editingId) {
        await updateResume(editingId, form)
      } else {
        await createResume(form)
      }
      setForm(emptyForm)
      setEditingId(null)
      await loadResumes()
    } catch {
      setError('Resume could not be saved.')
    } finally {
      setSubmitting(false)
    }
  }

  function startEdit(resume) {
    setEditingId(resume.id)
    setForm({
      title: resume.title,
      content: resume.content,
    })
  }

  function cancelEdit() {
    setEditingId(null)
    setForm(emptyForm)
  }

  async function handleDelete(id) {
    setError('')

    try {
      await deleteResume(id)
      await loadResumes()
      if (editingId === id) {
        cancelEdit()
      }
    } catch {
      setError('Resume could not be deleted.')
    }
  }

  return (
    <AppShell eyebrow="Workspace" title="Resumes">
      <section className="workspace-layout">
        <form className="data-form" onSubmit={handleSubmit}>
          <div>
            <p className="metric-label">{editingId ? 'Editing' : 'New resume'}</p>
            <h2>{editingId ? 'Update resume' : 'Save resume'}</h2>
          </div>

          <label className="upload-dropzone">
            <span>Upload resume file</span>
            <small>Choose a .pdf, .doc, .docx, .txt, or .md resume.</small>
            <input
              accept=".pdf,.doc,.docx,.txt,.md,.text,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              onChange={handleFileUpload}
              type="file"
            />
          </label>

          <label>
            Title
            <input name="title" onChange={handleChange} required type="text" value={form.title} />
          </label>

          <label>
            Resume content
            <textarea
              name="content"
              onChange={handleChange}
              required
              rows="12"
              value={form.content}
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <div className="form-actions">
            <button className="primary-button" disabled={submitting} type="submit">
              {submitting ? 'Saving...' : editingId ? 'Update resume' : 'Save resume'}
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
            <p className="metric-label">Saved</p>
            <h2>{resumes.length} resumes</h2>
          </div>

          {loading ? (
            <p className="muted">Loading resumes...</p>
          ) : resumes.length === 0 ? (
            <p className="empty-state">No resumes saved yet.</p>
          ) : (
            resumes.map((resume) => (
              <article className="list-item" key={resume.id}>
                <div>
                  <h3>{resume.title}</h3>
                  <p>{resume.content}</p>
                </div>
                <div className="item-actions">
                  <button className="secondary-button" onClick={() => startEdit(resume)} type="button">
                    Edit
                  </button>
                  <button className="danger-button" onClick={() => handleDelete(resume.id)} type="button">
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

function getUploadErrorMessage(err) {
  const serverMessage = err.response?.data?.detail || err.response?.data?.message

  if (serverMessage) {
    return serverMessage
  }

  return 'Resume file could not be parsed. Try a PDF, Word, text, or markdown file.'
}

export default ResumesPage
