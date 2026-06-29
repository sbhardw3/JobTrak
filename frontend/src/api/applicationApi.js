import { apiClient } from './apiClient.js'

export const APPLICATION_STATUSES = [
  'SAVED',
  'APPLIED',
  'OA',
  'INTERVIEW',
  'REJECTED',
  'OFFER',
  'GHOSTED',
]

export async function getApplications() {
  const response = await apiClient.get('/api/applications')
  return response.data
}

export async function createApplication(payload) {
  const response = await apiClient.post('/api/applications', payload)
  return response.data
}

export async function updateApplication(id, payload) {
  const response = await apiClient.put(`/api/applications/${id}`, payload)
  return response.data
}

export async function updateApplicationStatus(id, status) {
  const response = await apiClient.patch(`/api/applications/${id}/status`, { status })
  return response.data
}

export async function deleteApplication(id) {
  await apiClient.delete(`/api/applications/${id}`)
}
