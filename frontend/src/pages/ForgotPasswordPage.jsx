import { Link } from 'react-router-dom'
import { useState } from 'react'
import { requestPasswordReset } from '../api/authApi.js'
import { getApiErrorMessage } from '../utils/apiError.js'

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    setSubmitting(true)

    try {
      const response = await requestPasswordReset({ email })
      setMessage(`${response.message} Check the Spring Boot terminal for the dev reset link.`)
    } catch (err) {
      setError(getApiErrorMessage(err, 'Password reset could not be started.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <p className="eyebrow">Account recovery</p>
        <h1>Reset password</h1>
        <p className="auth-copy">Enter your account email and JobTrak will create a secure reset link.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              autoComplete="email"
              name="email"
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              value={email}
            />
          </label>

          {error && <p className="form-error">{error}</p>}
          {message && <p className="form-success">{message}</p>}

          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Creating link...' : 'Create reset link'}
          </button>
        </form>

        <p className="auth-switch">
          Remembered it? <Link to="/login">Back to login</Link>
        </p>
      </section>
    </main>
  )
}

export default ForgotPasswordPage
