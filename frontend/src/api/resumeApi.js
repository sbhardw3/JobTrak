import { apiClient } from './apiClient.js'

export async function getResumes() {
  const response = await apiClient.get('/api/resumes')
  return response.data
}

export async function extractResumeFile(file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await apiClient.post('/api/resumes/extract', formData)

  return response.data
}

export async function createResume(payload) {
  const response = await apiClient.post('/api/resumes', payload)
  return response.data
}

export async function updateResume(id, payload) {
  const response = await apiClient.put(`/api/resumes/${id}`, payload)
  return response.data
}

export async function deleteResume(id) {
  await apiClient.delete(`/api/resumes/${id}`)
}
