import { useEffect, useMemo, useState } from 'react'
import { analyzeResume, getAnalyses, getAnalysis } from '../api/aiApi.js'
import { getApplications } from '../api/applicationApi.js'
import { extractResumeFile, getResumes } from '../api/resumeApi.js'
import AppShell from '../components/AppShell.jsx'

const emptyForm = {
  resumeMode: 'saved',
  jobMode: 'saved',
  resumeId: '',
  applicationId: '',
  resumeText: '',
  jobDescription: '',
  uploadedResumeName: '',
}

function AiAnalysisPage() {
  const [form, setForm] = useState(emptyForm)
  const [resumes, setResumes] = useState([])
  const [applications, setApplications] = useState([])
  const [analyses, setAnalyses] = useState([])
  const [latestAnalysis, setLatestAnalysis] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [loadingAnalysisId, setLoadingAnalysisId] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

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
      setLatestAnalysis(null)
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

  async function handleResumeUpload(event) {
    const file = event.target.files?.[0]

    if (!file) {
      return
    }

    try {
      const parsedResume = await extractResumeFile(file)
      setForm((current) => ({
        ...current,
        resumeMode: 'upload',
        resumeText: parsedResume.content,
        uploadedResumeName: file.name,
      }))
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
    setNotice('')
    setLatestAnalysis(null)

    const payload = {
      resumeId: form.resumeMode === 'saved' && form.resumeId ? Number(form.resumeId) : null,
      applicationId: form.jobMode === 'saved' && form.applicationId ? Number(form.applicationId) : null,
      resumeText: form.resumeMode !== 'saved' ? form.resumeText : null,
      jobDescription: form.jobMode === 'paste' ? form.jobDescription : null,
    }

    try {
      await analyzeResume(payload)
      const refreshedAnalyses = await getAnalyses()
      setAnalyses(refreshedAnalyses)
      setLatestAnalysis(null)
      setNotice('Analysis saved to history. Select it from History to view the full resume improvement plan.')
    } catch (err) {
      setError(err.response?.data?.message || 'AI analysis could not be created.')
    } finally {
      setSubmitting(false)
    }
  }

  async function selectHistoryItem(analysis) {
    setError('')
    setNotice('')
    setLoadingAnalysisId(analysis.id)

    try {
      setLatestAnalysis(await getAnalysis(analysis.id))
    } catch (err) {
      setError(err.response?.data?.message || 'AI analysis could not be refreshed.')
    } finally {
      setLoadingAnalysisId(null)
    }
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
              <option value="saved">Select saved resume</option>
              <option value="upload">Upload resume file</option>
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
          ) : form.resumeMode === 'upload' ? (
            <>
              <label className="upload-dropzone">
                <span>Upload resume</span>
                <small>Choose a .pdf, .doc, .docx, .txt, or .md resume.</small>
                <input
                  accept=".pdf,.doc,.docx,.txt,.md,.text,application/pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                  onChange={handleResumeUpload}
                  type="file"
                />
              </label>
              {form.uploadedResumeName && (
                <p className="helper-text">Loaded file: {form.uploadedResumeName}</p>
              )}
              <label>
                Uploaded resume text
                <textarea
                  name="resumeText"
                  onChange={handleChange}
                  required
                  rows="9"
                  value={form.resumeText}
                />
              </label>
            </>
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
          {notice && <p className="helper-text">{notice}</p>}

          <button className="primary-button" disabled={submitting || loading} type="submit">
            {submitting ? 'Analyzing...' : 'Run analysis'}
          </button>
        </form>

        <section className="analysis-results">
          {latestAnalysis ? (
            <AnalysisResult analysis={latestAnalysis} />
          ) : submitting ? (
            <div className="empty-state">Running analysis and saving it to history...</div>
          ) : (
            <div className="empty-state">
              {loading ? 'Loading AI analyses...' : 'Select a saved analysis from History to view details.'}
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
                  disabled={loadingAnalysisId === analysis.id}
                  key={analysis.id}
                  onClick={() => selectHistoryItem(analysis)}
                  type="button"
                >
                  <span>{analysis.matchScore}% match</span>
                  <small>{loadingAnalysisId === analysis.id ? 'Refreshing...' : analysis.source}</small>
                </button>
              ))
            )}
          </section>
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

      <section className="recommendation-panel">
        <p className="metric-label">Recommendation</p>
        <h3>{getRecommendationTitle(analysis.matchScore)}</h3>
        <p>{getRecommendationCopy(analysis.matchScore)}</p>
      </section>

      <div className="analysis-columns">
        <ResultList title="Missing keywords" items={analysis.missingKeywords} />
        <ResultList title="Suggested skills" items={analysis.suggestedSkills} />
      </div>

      <ResultList title="Resume-wide improvement plan" items={analysis.resumeRewritePlan} />
      <ResultList title="Resume bullet improvements" items={analysis.resumeBulletImprovements} />
      <ResultList title="Where to add the bullets" items={analysis.bulletPlacementSuggestions} />
      <ResultList title="Where to add keywords and skills" items={analysis.keywordPlacementSuggestions} />

      <section className="cover-letter-box">
        <p className="metric-label">Cover letter</p>
        <p>{analysis.coverLetter}</p>
      </section>
    </article>
  )
}

function getRecommendationTitle(score) {
  if (score >= 80) {
    return 'Strong fit'
  }
  if (score >= 60) {
    return 'Good fit with targeted edits'
  }
  return 'Needs stronger alignment'
}

function getRecommendationCopy(score) {
  if (score >= 80) {
    return 'Keep the core resume structure and sharpen the language around the highest-value keywords from this role.'
  }
  if (score >= 60) {
    return 'Use the missing keywords and bullet improvements below before applying so your resume mirrors the role more clearly.'
  }
  return 'Start by adding relevant skills, measurable project details, and role-specific language before sending this resume.'
}

function ResultList({ title, items }) {
  const safeItems = items ?? []

  return (
    <section className="result-list">
      <h3>{title}</h3>
      {safeItems.length === 0 ? (
        <p className="muted">No items returned.</p>
      ) : (
        <ul>
          {safeItems.map((item, index) => (
            <li key={`${item}-${index}`}>{item}</li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default AiAnalysisPage
