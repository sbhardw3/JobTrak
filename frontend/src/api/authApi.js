import { apiClient } from './apiClient.js'

export async function signupUser(payload) {
  const response = await apiClient.post('/api/auth/signup', payload)
  return response.data
}

export async function loginUser(payload) {
  const response = await apiClient.post('/api/auth/login', payload)
  return response.data
}

export async function requestPasswordReset(payload) {
  const response = await apiClient.post('/api/auth/forgot-password', payload)
  return response.data
}

export async function resetPassword(payload) {
  const response = await apiClient.post('/api/auth/reset-password', payload)
  return response.data
}

export async function verifyEmailChange(payload) {
  const response = await apiClient.post('/api/auth/verify-email-change', payload)
  return response.data
}

export async function getCurrentUser() {
  const response = await apiClient.get('/api/users/me')
  return response.data
}

export async function updateCurrentUser(payload) {
  const response = await apiClient.patch('/api/users/me', payload)
  return response.data
}

export async function requestEmailChange(payload) {
  const response = await apiClient.post('/api/users/me/email-change', payload)
  return response.data
}
