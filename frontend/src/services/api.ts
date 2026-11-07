import type { Catalog, DocumentDetail, DocumentSummary } from '../types'

async function getJson<T>(url: string): Promise<T> {
  const response = await fetch(url)
  if (!response.ok) {
    const message = await response.text()
    throw new Error(`${response.status} ${response.statusText}: ${message || 'Request failed'}`)
  }
  return response.json() as Promise<T>
}

export const api = {
  getCatalog: () => getJson<Catalog>('/api/catalog'),
  getDocuments: () => getJson<DocumentSummary[]>('/api/documents'),
  getDocument: (id: string) => getJson<DocumentDetail>(`/api/documents/${encodeURIComponent(id)}`),
}
