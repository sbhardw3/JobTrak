import { useEffect, useMemo, useState } from 'react'
import { analyzeResume, getAnalyses } from '../api/aiApi.js'
import { getApplications } from '../api/applicationApi.js'
import { getResumes } from '../api/resumeApi.js'
import AppShell from '../components/AppShell.jsx'

const emptyForm = {
  resumeMode: 'saved',
  jobMode: 'saved',
  resumeId: '',
  applicationId: '',
  resumeText: '',
  jobDescription: '',
}

function AiAnalysisPage() {
  const [form, setForm] = useState(emptyForm)
  const [resumes, setResumes] = useState([])
  const [applications, setApplications] = useState([])
  const [analyses, setAnalyses] = useState([])
  const [latestAnalysis, setLatestAnalysis] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadPageData()
  }, [])

  const selectedApplication = useMemo(
    () => applications.find((application) => String(application.id) === form.applicationId),
    [applications, form.applicationId],
  )

  async function loadPageData() {
    setLoading(true)
    setError('')

    try {
      const [savedResumes, savedApplications, savedAnalyses] = await Promise.all([
        getResumes(),
        getApplications(),
        getAnalyses(),
      ])

      setResumes(savedResumes)
      setApplications(savedApplications)
      setAnalyses(savedAnalyses)
      setLatestAnalysis(savedAnalyses[0] || null)
      setForm((current) => ({
        ...current,
        resumeId: current.resumeId || savedResumes[0]?.id?.toString() || '',
        applicationId: current.applicationId || savedApplications[0]?.id?.toString() || '',
      }))
    } catch {
      setError('AI workspace data could not be loaded.')
    } finally {
      setLoading(false)
    }
  }

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({
      ...current,
      [name]: value,
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')

    const payload = {
      resumeId: form.resumeMode === 'saved' && form.resumeId ? Number(form.resumeId) : null,
      applicationId: form.jobMode === 'saved' && form.applicationId ? Number(form.applicationId) : null,
      resumeText: form.resumeMode === 'paste' ? form.resumeText : null,
      jobDescription: form.jobMode === 'paste' ? form.jobDescription : null,
    }

    try {
      const analysis = await analyzeResume(payload)
      setLatestAnalysis(analysis)
      setAnalyses((current) => [analysis, ...current.filter((item) => item.id !== analysis.id)])
    } catch (err) {
      setError(err.response?.data?.message || 'AI analysis could not be created.')
    } finally {
      setSubmitting(false)
    }
  }

  function selectHistoryItem(analysis) {
    setLatestAnalysis(analysis)
  }

  return (
    <AppShell eyebrow="AI Copilot" title="Resume analysis">
      <section className="workspace-layout wide analysis-layout">
        <form className="data-form" onSubmit={handleSubmit}>
          <div>
            <p className="metric-label">Analyze</p>
            <h2>Resume match</h2>
          </div>

          <label>
            Resume source
            <select name="resumeMode" onChange={handleChange} value={form.resumeMode}>
              <option value="saved">Use saved resume</option>
              <option value="paste">Paste resume text</option>
            </select>
          </label>

          {form.resumeMode === 'saved' ? (
            <label>
              Saved resume
              <select name="resumeId" onChange={handleChange} required value={form.resumeId}>
                <option value="">Select a resume</option>
                {resumes.map((resume) => (
                  <option key={resume.id} value={resume.id}>
                    {resume.title}
                  </option>
                ))}
              </select>
            </label>
          ) : (
            <label>
              Resume text
              <textarea
                name="resumeText"
                onChange={handleChange}
                required
                rows="9"
                value={form.resumeText}
              />
            </label>
          )}

          <label>
            Job source
            <select name="jobMode" onChange={handleChange} value={form.jobMode}>
              <option value="saved">Use saved application</option>
              <option value="paste">Paste job description</option>
            </select>
          </label>

          {form.jobMode === 'saved' ? (
            <>
              <label>
                Saved application
                <select name="applicationId" onChange={handleChange} required value={form.applicationId}>
                  <option value="">Select an application</option>
                  {applications.map((application) => (
                    <option key={application.id} value={application.id}>
                      {application.company} - {application.jobTitle}
                    </option>
                  ))}
                </select>
              </label>
              {selectedApplication && !selectedApplication.jobDescription && (
                <p className="helper-text">This application has no job description saved.</p>
              )}
            </>
          ) : (
            <label>
              Job description
              <textarea
                name="jobDescription"
                onChange={handleChange}
                required
                rows="9"
                value={form.jobDescription}
              />
            </label>
          )}

          {error && <p className="form-error">{error}</p>}

          <button className="primary-button" disabled={submitting || loading} type="submit">
            {submitting ? 'Analyzing...' : 'Run analysis'}
          </button>
        </form>

        <section className="analysis-results">
          {latestAnalysis ? (
            <AnalysisResult analysis={latestAnalysis} />
          ) : (
            <div className="empty-state">
              {loading ? 'Loading AI analyses...' : 'Run an analysis to see results here.'}
            </div>
          )}

          <section className="data-list history-panel">
            <div className="section-heading">
              <p className="metric-label">History</p>
              <h2>{analyses.length} saved</h2>
            </div>

            {analyses.length === 0 ? (
              <p className="empty-state">No AI analyses saved yet.</p>
            ) : (
              analyses.map((analysis) => (
                <button
                  className="history-item"
                  key={analysis.id}
                  onClick={() => selectHistoryItem(analysis)}
                  type="button"
                >
                  <span>{analysis.matchScore}% match</span>
                  <small>{analysis.source}</small>
                </button>
              ))
            )}
          </section>
        </section>
      </section>
    </AppShell>
  )
}

function AnalysisResult({ analysis }) {
  return (
    <article className="analysis-card">
      <div className="analysis-header">
        <div>
          <p className="metric-label">{analysis.source}</p>
          <h2>Match score</h2>
        </div>
        <div className="score-badge">{analysis.matchScore}%</div>
      </div>

      <div className="analysis-columns">
        <ResultList title="Missing keywords" items={analysis.missingKeywords} />
        <ResultList title="Suggested skills" items={analysis.suggestedSkills} />
      </div>

      <ResultList title="Resume bullet improvements" items={analysis.resumeBulletImprovements} />

      <section className="cover-letter-box">
        <p className="metric-label">Cover letter</p>
        <p>{analysis.coverLetter}</p>
      </section>
    </article>
  )
}

function ResultList({ title, items }) {
  return (
    <section className="result-list">
      <h3>{title}</h3>
      {items.length === 0 ? (
        <p className="muted">No items returned.</p>
      ) : (
        <ul>
          {items.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default AiAnalysisPage
