import { useEffect, useState } from 'react'
import Modal from '../components/Modal'
import StatusPill from '../components/StatusPill'
import StatusUpdater from '../components/StatusUpdater'
import { useAuth } from '../context/AuthContext'
import { useStudentProfile } from '../context/StudentProfileContext'
import { api, ApiError } from '../api/client'

const STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Pending' },
  { value: 'ACCEPTED', label: 'Accepted' },
  { value: 'DECLINED', label: 'Declined' },
  { value: 'WITHDRAWN', label: 'Withdrawn' }
]

export default function Offers() {
  const { user } = useAuth()
  const { profile, loading: profileLoading } = useStudentProfile()
  const isStaff = user.role === 'ADMIN' || user.role === 'PLACEMENT_OFFICER'

  const [applications, setApplications] = useState([])
  const [applicationId, setApplicationId] = useState('')
  const [offer, setOffer] = useState(null)
  const [notFound, setNotFound] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [createOpen, setCreateOpen] = useState(false)
  const [createForm, setCreateForm] = useState({ ctc: '', joiningDate: '' })
  const [createError, setCreateError] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    async function loadApplications() {
      const params = { page: 0, size: 500 }
      if (!isStaff && profile) params.studentId = profile.id
      try {
        const res = await api.get('/applications', params)
        setApplications(res.content || [])
      } catch {
        setApplications([])
      }
    }
    if (isStaff || profile) loadApplications()
  }, [isStaff, profile?.id])

  async function loadOffer(appId) {
    if (!appId) {
      setOffer(null)
      setNotFound(false)
      return
    }
    setLoading(true)
    setError('')
    setNotFound(false)
    try {
      const res = await api.get('/offers', { applicationId: appId })
      setOffer(res)
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) {
        setOffer(null)
        setNotFound(true)
      } else {
        setError(e instanceof ApiError ? e.message : 'Could not load the offer.')
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadOffer(applicationId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [applicationId])

  async function handleCreate(e) {
    e.preventDefault()
    setSaving(true)
    setCreateError('')
    try {
      await api.post('/offers', {
        applicationId: Number(applicationId),
        ctc: Number(createForm.ctc),
        joiningDate: createForm.joiningDate || null
      })
      setCreateOpen(false)
      setCreateForm({ ctc: '', joiningDate: '' })
      loadOffer(applicationId)
    } catch (e) {
      setCreateError(e instanceof ApiError ? e.message : 'Could not create the offer.')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete() {
    if (!window.confirm('Withdraw and remove this offer record?')) return
    try {
      await api.delete(`/offers/${offer.id}`)
      loadOffer(applicationId)
    } catch (e) {
      window.alert(e instanceof ApiError ? e.message : 'Could not delete this offer.')
    }
  }

  if (!isStaff && profileLoading) {
    return <div className="loading-line">Loading your profile…</div>
  }

  if (!isStaff && !profile) {
    return (
      <div>
        <div className="page-head">
          <div className="kicker">My offers</div>
          <h1>Offers</h1>
        </div>
        <div className="banner banner-error">
          We couldn't find a student profile linked to your account. Contact the placement cell.
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="page-head">
        <div className="kicker">{isStaff ? 'Offer letters' : 'My offers'}</div>
        <h1>Offers</h1>
        <p>An application can carry at most one offer. Choose an application to view or manage it.</p>
      </div>

      <div className="page-toolbar">
        <div className="filter-row">
          <select value={applicationId} onChange={(e) => setApplicationId(e.target.value)}>
            <option value="">Choose an application…</option>
            {applications.map((a) => (
              <option key={a.id} value={a.id}>
                {isStaff ? `${a.studentName} — ${a.jobTitle}` : a.jobTitle}
              </option>
            ))}
          </select>
        </div>
        {isStaff && notFound && applicationId && (
          <button className="btn btn-primary" onClick={() => setCreateOpen(true)}>
            + Issue offer
          </button>
        )}
      </div>

      {error && <div className="banner banner-error">{error}</div>}

      {!applicationId ? (
        <div className="surface" style={{ padding: 40, textAlign: 'center', color: 'var(--ink-soft)' }}>
          Select an application above to see its offer.
        </div>
      ) : loading ? (
        <div className="loading-line">Loading…</div>
      ) : notFound ? (
        <div className="surface" style={{ padding: 40, textAlign: 'center', color: 'var(--ink-soft)' }}>
          No offer has been issued for this application yet.
        </div>
      ) : offer ? (
        <div className="surface panel">
          <h2>CTC {offer.ctc}</h2>
          <p className="sub">
            Offer date {offer.offerDate || '—'} · Joining date {offer.joiningDate || 'not set'}
          </p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16, marginBottom: 16 }}>
            <StatusPill value={offer.status} />
          </div>
          {isStaff ? (
            <div style={{ display: 'flex', gap: 16, alignItems: 'flex-end', flexWrap: 'wrap' }}>
              <StatusUpdater
                endpoint={`/offers/${offer.id}/status`}
                current={offer.status}
                options={STATUS_OPTIONS}
                onDone={() => loadOffer(applicationId)}
              />
              <button className="btn btn-danger" onClick={handleDelete}>
                Delete offer
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', gap: 10 }}>
              {offer.status === 'PENDING' && (
                <>
                  <button
                    className="btn btn-primary"
                    onClick={async () => {
                      await api.patch(`/offers/${offer.id}/status`, { status: 'ACCEPTED' })
                      loadOffer(applicationId)
                    }}
                  >
                    Accept offer
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={async () => {
                      await api.patch(`/offers/${offer.id}/status`, { status: 'DECLINED' })
                      loadOffer(applicationId)
                    }}
                  >
                    Decline offer
                  </button>
                </>
              )}
            </div>
          )}
        </div>
      ) : null}

      {createOpen && (
        <Modal title="Issue offer" onClose={() => setCreateOpen(false)}>
          <form onSubmit={handleCreate}>
            {createError && <div className="banner banner-error">{createError}</div>}
            <div className="field">
              <label>CTC (annual)</label>
              <input
                type="number"
                step="0.01"
                min="0"
                required
                value={createForm.ctc}
                onChange={(e) => setCreateForm((p) => ({ ...p, ctc: e.target.value }))}
              />
            </div>
            <div className="field">
              <label>Joining date</label>
              <input
                type="date"
                value={createForm.joiningDate}
                onChange={(e) => setCreateForm((p) => ({ ...p, joiningDate: e.target.value }))}
              />
            </div>
            <div className="form-actions">
              <button type="button" className="btn" onClick={() => setCreateOpen(false)}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Issue offer'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
