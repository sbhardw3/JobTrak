import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useState } from 'react'
import { resetPassword } from '../api/authApi.js'
import { getApiErrorMessage } from '../utils/apiError.js'

function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [form, setForm] = useState({
    newPassword: '',
    confirmPassword: '',
  })
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const token = searchParams.get('token') || ''

  function handleChange(event) {
    setForm((current) => ({
      ...current,
      [event.target.name]: event.target.value,
    }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!token) {
      setError('Reset token is missing.')
      return
    }

    if (form.newPassword !== form.confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setSubmitting(true)

    try {
      const response = await resetPassword({ token, newPassword: form.newPassword })
      setMessage(response.message)
      window.setTimeout(() => navigate('/login'), 1200)
    } catch (err) {
      setError(getApiErrorMessage(err, 'Password could not be reset.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <p className="eyebrow">Account recovery</p>
        <h1>Choose a new password</h1>
        <p className="auth-copy">Use at least 8 characters. After this, log in with your new password.</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            New password
            <input
              autoComplete="new-password"
              minLength={8}
              name="newPassword"
              onChange={handleChange}
              required
              type="password"
              value={form.newPassword}
            />
          </label>

          <label>
            Confirm password
            <input
              autoComplete="new-password"
              minLength={8}
              name="confirmPassword"
              onChange={handleChange}
              required
              type="password"
              value={form.confirmPassword}
            />
          </label>

          {error && <p className="form-error">{error}</p>}
          {message && <p className="form-success">{message}</p>}

          <button className="primary-button" disabled={submitting} type="submit">
            {submitting ? 'Resetting...' : 'Reset password'}
          </button>
        </form>

        <p className="auth-switch">
          Need a new link? <Link to="/forgot-password">Request reset</Link>
        </p>
      </section>
    </main>
  )
}

export default ResetPasswordPage
