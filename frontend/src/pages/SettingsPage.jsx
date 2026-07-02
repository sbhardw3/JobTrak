import { useEffect, useState } from 'react'
import AppShell from '../components/AppShell.jsx'
import { requestEmailChange, updateCurrentUser } from '../api/authApi.js'
import { useAuth } from '../hooks/useAuth.js'
import { getApiErrorMessage } from '../utils/apiError.js'

function SettingsPage() {
  const { user, refreshUser } = useAuth()
  const [name, setName] = useState(user?.name || '')
  const [newEmail, setNewEmail] = useState('')
  const [profileMessage, setProfileMessage] = useState('')
  const [emailMessage, setEmailMessage] = useState('')
  const [profileError, setProfileError] = useState('')
  const [emailError, setEmailError] = useState('')
  const [savingName, setSavingName] = useState(false)
  const [sendingEmail, setSendingEmail] = useState(false)

  useEffect(() => {
    setName(user?.name || '')
  }, [user?.name])

  async function handleNameSubmit(event) {
    event.preventDefault()
    setProfileError('')
    setProfileMessage('')
    setSavingName(true)

    try {
      await updateCurrentUser({ name })
      await refreshUser()
      setProfileMessage('Name updated.')
    } catch (err) {
      setProfileError(getApiErrorMessage(err, 'Name could not be updated.'))
    } finally {
      setSavingName(false)
    }
  }

  async function handleEmailSubmit(event) {
    event.preventDefault()
    setEmailError('')
    setEmailMessage('')
    setSendingEmail(true)

    try {
      const response = await requestEmailChange({ newEmail })
      setEmailMessage(`${response.message} Open the verification link printed in the backend terminal.`)
      setNewEmail('')
    } catch (err) {
      setEmailError(getApiErrorMessage(err, 'Email change could not be started.'))
    } finally {
      setSendingEmail(false)
    }
  }

  return (
    <AppShell eyebrow="Account" title="Settings">
      <section className="settings-layout">
        <article className="settings-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Profile</p>
              <h2>Basic information</h2>
            </div>
          </div>

          <form className="data-form flat-form" onSubmit={handleNameSubmit}>
            <label>
              Name
              <input
                autoComplete="name"
                name="name"
                onChange={(event) => setName(event.target.value)}
                required
                type="text"
                value={name}
              />
            </label>

            {profileError && <p className="form-error">{profileError}</p>}
            {profileMessage && <p className="form-success">{profileMessage}</p>}

            <div className="form-actions">
              <button className="primary-button" disabled={savingName} type="submit">
                {savingName ? 'Saving...' : 'Save name'}
              </button>
            </div>
          </form>
        </article>

        <article className="settings-card">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Security</p>
              <h2>Email address</h2>
            </div>
          </div>

          <div className="current-email-box">
            <small>Current email</small>
            <strong>{user?.email}</strong>
          </div>

          <form className="data-form flat-form" onSubmit={handleEmailSubmit}>
            <label>
              New email
              <input
                autoComplete="email"
                name="newEmail"
                onChange={(event) => setNewEmail(event.target.value)}
                required
                type="email"
                value={newEmail}
              />
            </label>

            <p className="helper-text">
              JobTrak will not change your login email until the new address is verified.
            </p>

            {emailError && <p className="form-error">{emailError}</p>}
            {emailMessage && <p className="form-success">{emailMessage}</p>}

            <div className="form-actions">
              <button className="primary-button" disabled={sendingEmail} type="submit">
                {sendingEmail ? 'Creating verification...' : 'Create verification link'}
              </button>
            </div>
          </form>
        </article>
      </section>
    </AppShell>
  )
}

export default SettingsPage
