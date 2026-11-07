import { FileText, Search } from 'lucide-react'
import { useMemo, useState } from 'react'
import type { DocumentSummary } from '../types'

interface Props {
  documents: DocumentSummary[]
  selectedId: string | null
  onSelect: (id: string) => void
}

export function DocumentList({ documents, selectedId, onSelect }: Props) {
  const [query, setQuery] = useState('')
  const filtered = useMemo(() => documents.filter(doc =>
    `${doc.id} ${doc.title}`.toLowerCase().includes(query.toLowerCase())), [documents, query])

  return <aside className="sidebar">
    <div className="sidebar-heading"><div><span className="eyebrow">WORKSPACE</span><h2>Documents</h2></div><span className="count-bubble">{documents.length}</span></div>
    <label className="search-box"><Search size={15}/><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Find a document" /></label>
    <div className="document-list">
      {filtered.map(doc => <button key={doc.id} className={`document-item ${selectedId === doc.id ? 'active' : ''}`} onClick={() => onSelect(doc.id)}>
        <span className="file-icon"><FileText size={17}/></span>
        <span className="document-copy"><strong>{doc.id}</strong><small title={doc.title}>{doc.title}</small><span className="document-meta">{doc.cases} {doc.cases === 1 ? 'case' : 'cases'}{doc.flags.length ? ` · ${doc.flags.length} flags` : ''}</span></span>
        <span className="item-chevron">›</span>
      </button>)}
      {!filtered.length && <div className="empty-filter">No matching documents</div>}
    </div>
    <div className="sidebar-footer"><span className="online-dot"/> Connected to local API</div>
  </aside>
}
