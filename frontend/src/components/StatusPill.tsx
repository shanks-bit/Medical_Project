import type { Confidence, Status } from '../types'

export function StatusPill({ status }: { status: Status }) {
  return <span className={`pill status-${status}`}>{status}</span>
}

export function ConfidencePill({ confidence }: { confidence?: Confidence }) {
  if (!confidence) return <span className="muted-label">No confidence</span>
  return <span className={`pill confidence-${confidence}`}>{confidence} confidence</span>
}
