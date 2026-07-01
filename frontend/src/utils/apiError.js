export function getApiErrorMessage(error, fallback) {
  const data = error.response?.data

  if (!data) {
    return fallback
  }

  if (typeof data === 'string') {
    return data
  }

  if (Array.isArray(data.errors) && data.errors.length > 0) {
    return data.errors
      .map((item) => item.defaultMessage || item.message || item)
      .filter(Boolean)
      .join(' ')
  }

  if (data.errors && typeof data.errors === 'object') {
    return Object.values(data.errors).flat().filter(Boolean).join(' ')
  }

  return data.message || data.detail || data.title || data.error || fallback
}
