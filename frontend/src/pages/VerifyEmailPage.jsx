import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { verifyEmailChange } from '../api/authApi.js'
import { useAuth } from '../hooks/useAuth.js'
import { getApiErrorMessage } from '../utils/apiError.js'

function VerifyEmailPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { saveSession } = useAuth()
  const [status, setStatus] = useState('Verifying your new email...')
  const [error, setError] = useState('')
  const token = searchParams.get('token') || ''

  useEffect(() => {
    let isMounted = true

    async function verify() {
      if (!token) {
        setError('Verification token is missing.')
        setStatus('')
        return
      }

      try {
        const response = await verifyEmailChange({ token })
        if (!isMounted) {
          return
        }
        saveSession(response)
        setStatus('Email verified. Redirecting to settings...')
        window.setTimeout(() => navigate('/settings'), 1000)
      } catch (err) {
        if (isMounted) {
          setStatus('')
          setError(getApiErrorMessage(err, 'Email could not be verified.'))
        }
      }
    }

    verify()

    return () => {
      isMounted = false
    }
  }, [navigate, saveSession, token])

  return (
    <main className="auth-page">
      <section className="auth-panel">
        <p className="eyebrow">Email verification</p>
        <h1>Verify email</h1>
        <p className="auth-copy">JobTrak is checking the verification link for your new email address.</p>

        {status && <p className="form-success">{status}</p>}
        {error && <p className="form-error">{error}</p>}

        <p className="auth-switch">
          <Link to="/settings">Back to settings</Link>
        </p>
      </section>
    </main>
  )
}

export default VerifyEmailPage
