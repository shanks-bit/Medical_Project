import { AlertTriangle, ChevronDown, CircleCheck, FileSearch, RefreshCw, Search, ShieldCheck } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { DocumentList } from '../components/DocumentList'
import { DocumentText } from '../components/DocumentText'
import { FieldPanel } from '../components/FieldPanel'
import { api } from '../services/api'
import type { Catalog, DocumentDetail, DocumentSummary } from '../types'

export function ReviewPage() {
  const [catalog, setCatalog] = useState<Catalog | null>(null)
  const [documents, setDocuments] = useState<DocumentSummary[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [detail, setDetail] = useState<DocumentDetail | null>(null)
  const [selectedCaseIndex, setSelectedCaseIndex] = useState(0)
  const [selectedField, setSelectedField] = useState<string | null>(null)
  const [fieldQuery, setFieldQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [detailLoading, setDetailLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadWorkspace = async () => {
    setLoading(true); setError(null)
    try {
      const [nextCatalog, nextDocuments] = await Promise.all([api.getCatalog(), api.getDocuments()])
      setCatalog(nextCatalog); setDocuments(nextDocuments)
      setSelectedId(current => current ?? nextDocuments[0]?.id ?? null)
    } catch (err) { setError(err instanceof Error ? err.message : 'Unable to load the review workspace.') }
    finally { setLoading(false) }
  }

  useEffect(() => { void loadWorkspace() }, [])

  useEffect(() => {
    if (!selectedId) { setDetail(null); return }
    let cancelled = false
    setDetailLoading(true); setSelectedField(null); setSelectedCaseIndex(0)
    api.getDocument(selectedId).then(value => { if (!cancelled) setDetail(value) })
      .catch(err => { if (!cancelled) setError(err instanceof Error ? err.message : 'Unable to load document.') })
      .finally(() => { if (!cancelled) setDetailLoading(false) })
    return () => { cancelled = true }
  }, [selectedId])

  const cases = detail?.extraction?.cases ?? []
  const selectedCase = cases.find(item => item.case_index === selectedCaseIndex) ?? cases[0] ?? null
  const summary = useMemo(() => documents.find(doc => doc.id === selectedId), [documents, selectedId])

  if (loading) return <main className="center-state"><div className="loader"/><strong>Preparing review workspace</strong><span>Loading catalog and documents…</span></main>
  if (error && !catalog) return <main className="center-state error-state"><AlertTriangle size={28}/><strong>Could not connect to the backend</strong><span>{error}</span><button className="primary-button" onClick={() => void loadWorkspace()}><RefreshCw size={15}/> Try again</button><small>Make sure Spring Boot is running on localhost:8080.</small></main>

  return <div className="app-shell">
    <header className="topbar">
      <div className="brand-mark"><FileSearch size={20}/></div>
      <div className="brand-copy"><strong>Case Intake</strong><span>Clinical safety review</span></div>
      <div className="topbar-divider"/>
      <div className="workspace-label">REVIEW WORKSPACE</div>
      <div className="topbar-spacer"/>
      <span className="api-status"><i/> API connected</span>
      <span className="catalog-version">Catalog v{catalog?.catalog_version ?? '—'}</span>
    </header>

    <div className="workspace">
      <DocumentList documents={documents} selectedId={selectedId} onSelect={setSelectedId}/>
      <main className="review-main">
        <div className="page-heading">
          <div><span className="eyebrow">CASE INTAKE / DOCUMENT REVIEW</span><h1>{selectedId ?? 'No documents available'}</h1><p>{summary?.title ?? 'Choose a document to begin reviewing extracted information.'}</p></div>
          <button className="icon-button" title="Refresh workspace" onClick={() => void loadWorkspace()}><RefreshCw size={16}/></button>
        </div>
        {error && <div className="inline-error"><AlertTriangle size={15}/>{error}<button onClick={() => setError(null)}>Dismiss</button></div>}
        {detailLoading ? <div className="loading-panel"><div className="loader"/> Loading document…</div> : detail ? <>
          <div className="document-toolbar">
            <div className="toolbar-left"><span className="doc-id-tag">{detail.id}</span><span className="toolbar-status"><ShieldCheck size={14}/>{detail.extraction ? 'Extraction available' : 'Awaiting extraction'}</span></div>
            <div className="toolbar-right">
              {cases.length > 0 && <label className="case-picker">Case <select value={selectedCase?.case_index ?? 0} onChange={e => { setSelectedCaseIndex(Number(e.target.value)); setSelectedField(null) }}>{cases.map(item => <option key={item.case_index} value={item.case_index}>{item.label || `Case ${item.case_index + 1}`}</option>)}</select><ChevronDown size={13}/></label>}
              <label className="field-search"><Search size={14}/><input value={fieldQuery} onChange={e => setFieldQuery(e.target.value)} placeholder="Find a field"/></label>
            </div>
          </div>
          {detail.extraction?.flags?.length ? <div className="flag-row"><AlertTriangle size={14}/>{detail.extraction.flags.join(' · ')}</div> : null}
          <div className="review-grid">
            <DocumentText text={detail.text} catalog={catalog!} selectedCase={selectedCase} selectedField={selectedField} onSelectField={setSelectedField}/>
            <FieldPanel catalog={catalog!} selectedCase={selectedCase} selectedField={selectedField} onSelectField={setSelectedField} query={fieldQuery}/>
          </div>
        </> : <div className="empty-workspace"><FileSearch size={34}/><strong>Select a document</strong><span>Choose a report from the left panel to inspect its source and extracted fields.</span></div>}
        <footer className="main-footer"><span><CircleCheck size={13}/> Source text is read-only</span><span>Human verification required before case completion</span></footer>
      </main>
    </div>
  </div>
}
