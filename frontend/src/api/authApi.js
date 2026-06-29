import { apiClient } from './apiClient.js'

export async function signupUser(payload) {
  const response = await apiClient.post('/api/auth/signup', payload)
  return response.data
}

export async function loginUser(payload) {
  const response = await apiClient.post('/api/auth/login', payload)
  return response.data
}

export async function getCurrentUser() {
  const response = await apiClient.get('/api/users/me')
  return response.data
}
