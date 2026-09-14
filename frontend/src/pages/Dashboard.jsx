import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useStudentProfile } from '../context/StudentProfileContext'
import { api } from '../api/client'

function StatCard({ label, value }) {
  return (
    <div className="surface stat-card">
      <div className="label">{label}</div>
      <div className="value">{value === null ? '—' : value}</div>
    </div>
  )
}

function useCount(fetcher, deps = []) {
  const [value, setValue] = useState(null)
  useEffect(() => {
    let cancelled = false
    fetcher()
      .then((n) => !cancelled && setValue(n))
      .catch(() => !cancelled && setValue(null))
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)
  return value
}

export default function Dashboard() {
  const { user } = useAuth()
  const { profile } = useStudentProfile()
  const isStaff = user.role === 'ADMIN' || user.role === 'PLACEMENT_OFFICER'

  const students = useCount(
    () => (isStaff ? api.get('/students', { page: 0, size: 1 }).then((p) => p.totalElements) : Promise.resolve(null)),
    [isStaff]
  )
  const pendingApprovals = useCount(
    () => (isStaff ? api.get('/users/pending', { page: 0, size: 1 }).then((p) => p.totalElements) : Promise.resolve(null)),
    [isStaff]
  )
  const companies = useCount(() => api.get('/companies').then((rows) => rows.length))
  const jobs = useCount(() => api.get('/jobs', { page: 0, size: 1 }).then((p) => p.totalElements))
  const drives = useCount(
    () => (isStaff ? api.get('/placement-drives', { page: 0, size: 1 }).then((p) => p.totalElements) : Promise.resolve(null)),
    [isStaff]
  )
  const applications = useCount(() => {
    const params = { page: 0, size: 1 }
    if (user.role === 'STUDENT') {
      if (!profile) return Promise.resolve(0)
      params.studentId = profile.id
    }
    return api.get('/applications', params).then((p) => p.totalElements)
  }, [profile?.id])

  return (
    <div>
      <div className="page-head">
        <div className="kicker">Overview</div>
        <h1>Good to see you, {user.username}</h1>
        <p>
          A running snapshot of the placement register. Use the sections on the left to manage records in
          detail.
        </p>
      </div>

      {isStaff && pendingApprovals > 0 && (
        <div className="link-well">
          <span>
            {pendingApprovals} student {pendingApprovals === 1 ? 'registration is' : 'registrations are'} waiting
            on your review.
          </span>
          <Link className="btn btn-small" to="/approvals">
            Review approvals
          </Link>
        </div>
      )}

      <div className="stat-grid">
        {isStaff && <StatCard label="Registered students" value={students} />}
        {isStaff && <StatCard label="Pending approvals" value={pendingApprovals} />}
        <StatCard label="Partner companies" value={companies} />
        <StatCard label="Open job postings" value={jobs} />
        {isStaff && <StatCard label="Placement drives" value={drives} />}
        <StatCard
          label={user.role === 'STUDENT' ? 'My applications' : 'Total applications'}
          value={applications}
        />
      </div>

      <div className="surface panel">
        <h2>Where to start</h2>
        <p className="sub">A short map of the register, in the order most people work through it.</p>
        {isStaff ? (
          <ol style={{ margin: 0, paddingLeft: 18, lineHeight: 1.9 }}>
            <li>Review <Link to="/approvals">pending student registrations</Link> and set up <Link to="/departments">departments</Link>.</li>
            <li>Add <Link to="/companies">companies</Link> and open <Link to="/jobs">job postings</Link> or a <Link to="/drives">placement drive</Link>.</li>
            <li>Track <Link to="/applications">applications</Link> as they come in, move candidates through <Link to="/interviews">interview rounds</Link>.</li>
            <li>Issue and follow up on <Link to="/offers">offers</Link>.</li>
          </ol>
        ) : (
          <ol style={{ margin: 0, paddingLeft: 18, lineHeight: 1.9 }}>
            <li>Browse open <Link to="/jobs">job postings</Link> and apply to the ones you're eligible for.</li>
            <li>Track your <Link to="/applications">applications</Link> and upcoming <Link to="/interviews">interviews</Link>.</li>
            <li>Review and respond to any <Link to="/offers">offers</Link> you receive.</li>
          </ol>
        )}
      </div>
    </div>
  )
}
