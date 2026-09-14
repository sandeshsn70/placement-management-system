import { useEffect, useState } from 'react'
import Modal from '../components/Modal'
import StatusPill from '../components/StatusPill'
import { useAuth } from '../context/AuthContext'
import { useStudentProfile } from '../context/StudentProfileContext'
import { api, ApiError } from '../api/client'

const MODE_OPTIONS = ['ONLINE', 'OFFLINE']
const RESULT_OPTIONS = ['PENDING', 'PASSED', 'FAILED']

function toLocalInputValue(iso) {
  if (!iso) return ''
  return iso.slice(0, 16)
}

export default function Interviews() {
  const { user } = useAuth()
  const { profile, loading: profileLoading } = useStudentProfile()
  const isStaff = user.role === 'ADMIN' || user.role === 'PLACEMENT_OFFICER'

  const [applications, setApplications] = useState([])
  const [applicationId, setApplicationId] = useState('')
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [scheduleOpen, setScheduleOpen] = useState(false)
  const [scheduleForm, setScheduleForm] = useState({ round: 1, interviewDate: '', mode: 'ONLINE', interviewerName: '' })
  const [scheduleError, setScheduleError] = useState('')
  const [saving, setSaving] = useState(false)

  const [resultRow, setResultRow] = useState(null)
  const [resultForm, setResultForm] = useState({ result: 'PENDING', feedback: '' })
  const [resultError, setResultError] = useState('')

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

  async function loadInterviews(appId) {
    if (!appId) {
      setRows([])
      return
    }
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/interviews', { applicationId: appId })
      setRows(res || [])
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not load interviews.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadInterviews(applicationId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [applicationId])

  async function handleSchedule(e) {
    e.preventDefault()
    setSaving(true)
    setScheduleError('')
    try {
      await api.post('/interviews', {
        applicationId: Number(applicationId),
        round: Number(scheduleForm.round),
        interviewDate: scheduleForm.interviewDate,
        mode: scheduleForm.mode,
        interviewerName: scheduleForm.interviewerName || undefined
      })
      setScheduleOpen(false)
      setScheduleForm({ round: 1, interviewDate: '', mode: 'ONLINE', interviewerName: '' })
      loadInterviews(applicationId)
    } catch (e) {
      setScheduleError(e instanceof ApiError ? e.message : 'Could not schedule this interview.')
    } finally {
      setSaving(false)
    }
  }

  async function handleRecordResult(e) {
    e.preventDefault()
    setResultError('')
    try {
      await api.patch(`/interviews/${resultRow.id}/result`, resultForm)
      setResultRow(null)
      loadInterviews(applicationId)
    } catch (e) {
      setResultError(e instanceof ApiError ? e.message : 'Could not record the result.')
    }
  }

  async function handleDelete(row) {
    if (!window.confirm('Remove this interview round?')) return
    try {
      await api.delete(`/interviews/${row.id}`)
      loadInterviews(applicationId)
    } catch (e) {
      window.alert(e instanceof ApiError ? e.message : 'Could not delete.')
    }
  }

  if (!isStaff && profileLoading) {
    return <div className="loading-line">Loading your profile…</div>
  }

  if (!isStaff && !profile) {
    return (
      <div>
        <div className="page-head">
          <div className="kicker">My interviews</div>
          <h1>Interviews</h1>
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
        <div className="kicker">{isStaff ? 'Interview rounds' : 'My interviews'}</div>
        <h1>Interviews</h1>
        <p>Interviews are tracked per application. Choose an application to see or schedule its rounds.</p>
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
        {isStaff && (
          <button className="btn btn-primary" disabled={!applicationId} onClick={() => setScheduleOpen(true)}>
            + Schedule round
          </button>
        )}
      </div>

      {error && <div className="banner banner-error">{error}</div>}

      {!applicationId ? (
        <div className="surface" style={{ padding: 40, textAlign: 'center', color: 'var(--ink-soft)' }}>
          Select an application above to view its interview rounds.
        </div>
      ) : (
        <div className="surface table-wrap">
          <table className="ledger">
            <thead>
              <tr>
                <th>Round</th>
                <th>Date</th>
                <th>Mode</th>
                <th>Interviewer</th>
                <th>Result</th>
                <th>Feedback</th>
                {isStaff && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr className="empty-row">
                  <td colSpan={7}>Loading…</td>
                </tr>
              ) : rows.length === 0 ? (
                <tr className="empty-row">
                  <td colSpan={7}>No rounds scheduled for this application yet.</td>
                </tr>
              ) : (
                rows.map((row) => (
                  <tr key={row.id}>
                    <td className="num">{row.round}</td>
                    <td>{(row.interviewDate || '').replace('T', ' ').slice(0, 16)}</td>
                    <td>{row.mode}</td>
                    <td>{row.interviewerName || '—'}</td>
                    <td>
                      <StatusPill value={row.result} />
                    </td>
                    <td>{row.feedback || '—'}</td>
                    {isStaff && (
                      <td>
                        <div className="actions-cell">
                          <button
                            className="btn btn-small"
                            onClick={() => {
                              setResultRow(row)
                              setResultForm({ result: row.result || 'PENDING', feedback: row.feedback || '' })
                            }}
                          >
                            Record result
                          </button>
                          <button className="btn btn-small btn-danger" onClick={() => handleDelete(row)}>
                            Delete
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {scheduleOpen && (
        <Modal title="Schedule interview round" onClose={() => setScheduleOpen(false)}>
          <form onSubmit={handleSchedule}>
            {scheduleError && <div className="banner banner-error">{scheduleError}</div>}
            <div className="form-grid">
              <div className="field">
                <label>Round number</label>
                <input
                  type="number"
                  min="1"
                  required
                  value={scheduleForm.round}
                  onChange={(e) => setScheduleForm((p) => ({ ...p, round: e.target.value }))}
                />
              </div>
              <div className="field">
                <label>Date &amp; time</label>
                <input
                  type="datetime-local"
                  required
                  value={toLocalInputValue(scheduleForm.interviewDate)}
                  onChange={(e) => setScheduleForm((p) => ({ ...p, interviewDate: e.target.value }))}
                />
                <div className="hint">Must be in the future.</div>
              </div>
              <div className="field">
                <label>Mode</label>
                <select
                  value={scheduleForm.mode}
                  onChange={(e) => setScheduleForm((p) => ({ ...p, mode: e.target.value }))}
                >
                  {MODE_OPTIONS.map((m) => (
                    <option key={m} value={m}>
                      {m}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field">
                <label>Interviewer name</label>
                <input
                  value={scheduleForm.interviewerName}
                  onChange={(e) => setScheduleForm((p) => ({ ...p, interviewerName: e.target.value }))}
                />
              </div>
            </div>
            <div className="form-actions">
              <button type="button" className="btn" onClick={() => setScheduleOpen(false)}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Scheduling…' : 'Schedule'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {resultRow && (
        <Modal title={`Record result — round ${resultRow.round}`} onClose={() => setResultRow(null)}>
          <form onSubmit={handleRecordResult}>
            {resultError && <div className="banner banner-error">{resultError}</div>}
            <div className="field">
              <label>Result</label>
              <select
                value={resultForm.result}
                onChange={(e) => setResultForm((p) => ({ ...p, result: e.target.value }))}
              >
                {RESULT_OPTIONS.map((r) => (
                  <option key={r} value={r}>
                    {r}
                  </option>
                ))}
              </select>
            </div>
            <div className="field">
              <label>Feedback</label>
              <textarea
                value={resultForm.feedback}
                onChange={(e) => setResultForm((p) => ({ ...p, feedback: e.target.value }))}
              />
            </div>
            <div className="form-actions">
              <button type="button" className="btn" onClick={() => setResultRow(null)}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary">
                Save
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
