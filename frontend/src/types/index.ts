export type Status = 'extracted' | 'unknown' | 'conflict'
export type Confidence = 'high' | 'medium' | 'low'

export interface Provenance {
  quote: string
  start: number
  end: number
}

export interface FieldResult {
  status: Status
  value?: unknown
  confidence?: Confidence
  provenance?: Provenance[]
  note?: string
}

export interface CaseResult {
  case_index: number
  label?: string
  fields: Record<string, FieldResult>
}

export interface Extraction {
  document_id: string
  catalog_version?: string
  model?: string
  flags?: string[]
  cases: CaseResult[]
}

export interface DocumentSummary {
  id: string
  title: string
  cases: number
  flags: string[]
}

export interface DocumentDetail {
  id: string
  text: string
  extraction: Extraction | null
}

export interface CatalogOption {
  code: string
  label: string
}

export interface CatalogField {
  id: string
  label: string
  section: string
  type: 'string' | 'integer' | 'number' | 'date' | 'coded' | 'coded_multi' | 'string_list'
  options?: CatalogOption[]
  guidance?: string
  validation?: { pattern?: string; min?: number; max?: number }
}

export interface Catalog {
  catalog_version: string
  sections: { id: string; label: string }[]
  fields: CatalogField[]
}
