import { useState } from 'react'
import { api, ApiError } from '../api/client'

export default function StatusUpdater({ endpoint, current, options, statusField = 'status', onDone }) {
  const [value, setValue] = useState(current || '')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function apply() {
    if (!value || value === current) return
    setSaving(true)
    setError('')
    try {
      await api.patch(endpoint, { [statusField]: value })
      onDone && onDone()
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not update status.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', gap: 6 }}>
        <select value={value} onChange={(e) => setValue(e.target.value)} disabled={saving}>
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <button className="btn btn-small" onClick={apply} disabled={saving || value === current}>
          {saving ? 'Saving…' : 'Update'}
        </button>
      </div>
      {error && <div className="hint" style={{ color: 'var(--red-seal)' }}>{error}</div>}
    </div>
  )
}
