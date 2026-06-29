import { useEffect, useState } from 'react'
import { getCurrentUser, loginUser, signupUser } from '../api/authApi.js'
import { TOKEN_STORAGE_KEY } from '../api/apiClient.js'
import { AuthContext } from './authContext.js'

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(() => localStorage.getItem(TOKEN_STORAGE_KEY))
  const [loading, setLoading] = useState(Boolean(token))

  useEffect(() => {
    let isMounted = true

    async function loadUser() {
      if (!token) {
        setLoading(false)
        return
      }

      try {
        const currentUser = await getCurrentUser()
        if (isMounted) {
          setUser(currentUser)
        }
      } catch {
        localStorage.removeItem(TOKEN_STORAGE_KEY)
        if (isMounted) {
          setToken(null)
          setUser(null)
        }
      } finally {
        if (isMounted) {
          setLoading(false)
        }
      }
    }

    loadUser()

    return () => {
      isMounted = false
    }
  }, [token])

  async function signup(payload) {
    const response = await signupUser(payload)
    saveSession(response)
    return response
  }

  async function login(payload) {
    const response = await loginUser(payload)
    saveSession(response)
    return response
  }

  function saveSession(response) {
    localStorage.setItem(TOKEN_STORAGE_KEY, response.token)
    setToken(response.token)
    setUser(response.user)
  }

  function logout() {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    setToken(null)
    setUser(null)
  }

  const value = {
    user,
    token,
    loading,
    isAuthenticated: Boolean(token && user),
    signup,
    login,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
