import { AlertCircle, CheckCircle2, CircleHelp, Quote, Search } from 'lucide-react'
import type { Catalog, CatalogField, CaseResult, FieldResult } from '../types'
import { ConfidencePill, StatusPill } from './StatusPill'

function displayValue(value: unknown): string {
  if (Array.isArray(value)) return value.map(String).join(', ')
  if (value === null || value === undefined || value === '') return '—'
  return String(value)
}

function FieldValue({ field, result }: { field: CatalogField; result: FieldResult }) {
  const value = result.value
  if (result.status !== 'extracted') return <div className={`state-message state-${result.status}`}>
    {result.status === 'unknown' ? <CircleHelp size={16}/> : <AlertCircle size={16}/>}
    <span>{result.status === 'unknown' ? 'Not stated in the source' : 'Conflicting information in the source'}</span>
  </div>

  if (field.type === 'coded') {
    return <select className="value-control" value={String(value ?? '')} disabled>
      <option value="">Not selected</option>
      {field.options?.map(option => <option key={option.code} value={option.code}>{option.label}</option>)}
    </select>
  }
  if (field.type === 'date') return <input className="value-control" type="date" value={String(value ?? '')} disabled />
  if (field.type === 'coded_multi' || field.type === 'string_list' || Array.isArray(value)) {
    const values = Array.isArray(value) ? value : []
    const labels = field.options?.reduce<Record<string, string>>((acc, option) => ({ ...acc, [option.code]: option.label }), {}) ?? {}
    return <div className="value-chips">{values.length ? values.map((item, index) => <span className="value-chip" key={`${String(item)}-${index}`}>{labels[String(item)] ?? String(item)}</span>) : <span className="empty-value">No items reported</span>}</div>
  }
  return <div className="value-control value-readonly">{displayValue(value)}</div>
}

export function FieldPanel({ catalog, selectedCase, selectedField, onSelectField, query }: {
  catalog: Catalog; selectedCase: CaseResult | null; selectedField: string | null; onSelectField: (id: string) => void; query: string
}) {
  const normalizedQuery = query.trim().toLowerCase()
  return <section className="fields-panel">
    <div className="panel-header"><div><span className="eyebrow">EXTRACTION REVIEW</span><h2>Extracted fields</h2></div><span className="field-total">{catalog.fields.length} fields</span></div>
    {!selectedCase ? <div className="no-case"><CircleHelp size={22}/><strong>No case to review</strong><span>This document has no qualifying patient case, or extraction has not been generated yet.</span></div> : <div className="field-sections">
      {catalog.sections.map(section => {
        const fields = catalog.fields.filter(field => field.section === section.id && (!normalizedQuery || `${field.label} ${field.guidance ?? ''}`.toLowerCase().includes(normalizedQuery)))
        if (!fields.length) return null
        return <div className="field-section" key={section.id}>
          <div className="section-heading"><h3>{section.label}</h3><span>{fields.length}</span></div>
          {fields.map(field => {
            const result = selectedCase.fields[field.id] ?? { status: 'unknown' as const, note: 'Field missing from extraction output.' }
            const selected = selectedField === field.id
            return <article key={field.id} className={`field-card ${selected ? 'selected' : ''}`}>
              <button className="field-select" onClick={() => onSelectField(field.id)} aria-pressed={selected}>
                <span className="field-title">{field.label}</span>
                <span className="field-badges"><StatusPill status={result.status}/><ConfidencePill confidence={result.confidence}/></span>
              </button>
              <div className="field-value"><FieldValue field={field} result={result}/></div>
              {result.note && <p className="field-note">{result.note}</p>}
              {result.status === 'extracted' && result.provenance?.length ? <button className="evidence-link" onClick={() => onSelectField(field.id)}><Quote size={13}/>{result.provenance.length} evidence {result.provenance.length === 1 ? 'quote' : 'quotes'}</button> : null}
              {field.guidance && <details className="field-guidance"><summary>Field guidance</summary><p>{field.guidance}</p></details>}
            </article>
          })}
        </div>
      })}
      {normalizedQuery && !catalog.fields.some(field => `${field.label} ${field.guidance ?? ''}`.toLowerCase().includes(normalizedQuery)) && <div className="empty-filter"><Search size={16}/> No fields match your search.</div>}
    </div>}
  </section>
}
