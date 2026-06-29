import { apiClient } from './apiClient.js'

export async function analyzeResume(payload) {
  const response = await apiClient.post('/api/ai/analyze', payload)
  return response.data
}

export async function getAnalyses() {
  const response = await apiClient.get('/api/ai/analyses')
  return response.data
}

export async function getAnalysis(id) {
  const response = await apiClient.get(`/api/ai/analyses/${id}`)
  return response.data
}
