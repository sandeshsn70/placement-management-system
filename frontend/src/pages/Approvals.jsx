import { useCallback, useEffect, useState } from 'react'
import { api, ApiError } from '../api/client'

export default function Approvals() {
  const [rows, setRows] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  const fetchPending = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/users/pending', { page, size: 10, sort: 'createdAt,asc' })
      setRows(res.content || [])
      setTotalPages(res.totalPages ?? 1)
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not load pending registrations.')
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    fetchPending()
  }, [fetchPending])

  async function decide(id, action) {
    setBusyId(id)
    try {
      await api.patch(`/users/${id}/${action}`)
      fetchPending()
    } catch (e) {
      window.alert(e instanceof ApiError ? e.message : `Could not ${action} this account.`)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div>
      <div className="page-head">
        <div className="kicker">Access control</div>
        <h1>Pending Approvals</h1>
        <p>
          New student registrations wait here until a placement officer or administrator approves them. Once
          approved, the student can sign in and their profile appears in the Students register.
        </p>
      </div>

      {error && <div className="banner banner-error">{error}</div>}

      <div className="surface table-wrap">
        <table className="ledger">
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Requested</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr className="empty-row">
                <td colSpan={4}>Loading…</td>
              </tr>
            ) : rows.length === 0 ? (
              <tr className="empty-row">
                <td colSpan={4}>Nothing waiting on review. New student sign-ups will show up here.</td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{row.username}</td>
                  <td>{row.email}</td>
                  <td>{(row.createdAt || '').slice(0, 10)}</td>
                  <td>
                    <div className="actions-cell">
                      <button
                        className="btn btn-small btn-primary"
                        disabled={busyId === row.id}
                        onClick={() => decide(row.id, 'approve')}
                      >
                        Approve
                      </button>
                      <button
                        className="btn btn-small btn-danger"
                        disabled={busyId === row.id}
                        onClick={() => decide(row.id, 'reject')}
                      >
                        Reject
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        {!loading && rows.length > 0 && (
          <div className="pagination">
            <button className="btn btn-small" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
              Previous
            </button>
            <span>
              Page {page + 1} of {Math.max(totalPages, 1)}
            </span>
            <button className="btn btn-small" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
