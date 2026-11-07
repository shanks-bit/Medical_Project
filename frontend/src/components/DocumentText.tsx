import { useEffect, useMemo, useRef } from 'react'
import type { CaseResult, Catalog, FieldResult } from '../types'

interface Range { start: number; end: number; fieldId: string; quote: string }

// API provenance offsets are Unicode code-point positions. JS string slicing uses UTF-16.
function codePointToUtf16(text: string, position: number): number {
  return Array.from(text).slice(0, Math.max(0, position)).join('').length
}

export function DocumentText({ text, catalog, selectedCase, selectedField, onSelectField }: {
  text: string; catalog: Catalog; selectedCase: CaseResult | null; selectedField: string | null; onSelectField: (id: string) => void
}) {
  const activeMark = useRef<HTMLElement | null>(null)
  const ranges = useMemo(() => {
    const all: Range[] = []
    if (!selectedCase) return all
    Object.entries(selectedCase.fields).forEach(([fieldId, result]: [string, FieldResult]) => {
      result.provenance?.forEach(source => {
        const start = codePointToUtf16(text, source.start)
        const end = codePointToUtf16(text, source.end)
        if (start >= 0 && end > start && end <= text.length && text.slice(start, end) === source.quote) {
          all.push({ start, end, fieldId, quote: source.quote })
        }
      })
    })
    return all.sort((a, b) => a.start - b.start || a.end - b.end)
  }, [selectedCase, text])

  useEffect(() => { activeMark.current?.scrollIntoView({ behavior: 'smooth', block: 'center' }) }, [selectedField, ranges])

  const segments: { value: string; range?: Range }[] = []
  let cursor = 0
  for (const range of ranges) {
    if (range.start < cursor) continue
    if (range.start > cursor) segments.push({ value: text.slice(cursor, range.start) })
    segments.push({ value: text.slice(range.start, range.end), range })
    cursor = range.end
  }
  if (cursor < text.length) segments.push({ value: text.slice(cursor) })

  return <section className="source-panel">
    <div className="panel-header"><div><span className="eyebrow">SOURCE EVIDENCE</span><h2>Original document</h2></div><span className="source-hint">Select a field to locate its quote</span></div>
    <div className="document-paper" aria-label="Original document text">
      <div className="paper-topline"><span>ORIGINAL SOURCE</span><span>{text.length.toLocaleString()} characters</span></div>
      <pre>{segments.map((segment, index) => segment.range ? <mark key={`${segment.range.fieldId}-${segment.range.start}-${index}`} ref={segment.range.fieldId === selectedField ? (node) => { activeMark.current = node } : undefined} className={segment.range.fieldId === selectedField ? 'evidence active' : 'evidence'} title={`${catalog.fields.find(f => f.id === segment.range?.fieldId)?.label ?? segment.range.fieldId} · Click to select field`} onClick={() => onSelectField(segment.range!.fieldId)}>{segment.value}</mark> : <span key={index}>{segment.value}</span>)}</pre>
    </div>
    <div className="legend"><span><i className="legend-dot selected"/> Selected evidence</span><span><i className="legend-dot other"/> Other cited evidence</span></div>
  </section>
}
