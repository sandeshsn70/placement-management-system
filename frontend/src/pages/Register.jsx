import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { api, ApiError } from '../api/client'

const initialForm = {
  username: '',
  email: '',
  password: '',
  role: 'STUDENT',
  fullName: '',
  phone: '',
  rollNumber: '',
  departmentId: '',
  cgpa: '',
  graduationYear: '',
  resumeLink: ''
}

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [departments, setDepartments] = useState([])
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(null) // { message } once a student registration is pending

  const isStudent = form.role === 'STUDENT'

  useEffect(() => {
    // Public endpoint — the department list is needed before the person has
    // an account at all.
    api
      .get('/departments')
      .then(setDepartments)
      .catch(() => setDepartments([]))
  }, [])

  function update(name, value) {
    setForm((prev) => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const payload = { ...form }
      if (!isStudent) {
        // Keep the payload tidy for staff registrations; the backend ignores
        // these fields for non-student roles anyway.
        delete payload.fullName
        delete payload.phone
        delete payload.rollNumber
        delete payload.departmentId
        delete payload.cgpa
        delete payload.graduationYear
        delete payload.resumeLink
      } else {
        payload.departmentId = Number(payload.departmentId)
        payload.cgpa = Number(payload.cgpa)
        payload.graduationYear = Number(payload.graduationYear)
      }

      const result = await register(payload)
      if (result.pending) {
        setSubmitted({ message: result.message })
      } else {
        navigate('/')
      }
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not create the account.')
    } finally {
      setSubmitting(false)
    }
  }

  if (submitted) {
    return (
      <div className="auth-shell">
        <div className="auth-card">
          <div className="auth-brand">PLACEMENT CELL</div>
          <h1>Registration submitted</h1>
          <div className="banner banner-info">{submitted.message}</div>
          <p className="text-muted" style={{ marginTop: 4 }}>
            You'll be able to sign in as soon as a placement officer or administrator reviews your request.
          </p>
          <div className="auth-switch">
            <Link to="/login">Back to sign in</Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-shell">
      <div className="auth-card" style={{ maxWidth: isStudent ? 500 : 400 }}>
        <div className="auth-brand">PLACEMENT CELL</div>
        <h1>Request an account</h1>
        {error && <div className="banner banner-error">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="field">
            <label htmlFor="role">I am a…</label>
            <select id="role" value={form.role} onChange={(e) => update('role', e.target.value)}>
              <option value="STUDENT">Student</option>
              <option value="PLACEMENT_OFFICER">Placement Officer</option>
              <option value="ADMIN">Administrator</option>
            </select>
            {isStudent && (
              <div className="hint">
                Student accounts need approval from a placement officer or administrator before you can sign in.
              </div>
            )}
          </div>

          <div className="form-grid">
            <div className="field full">
              <label htmlFor="username">Username</label>
              <input id="username" value={form.username} onChange={(e) => update('username', e.target.value)} required />
            </div>
            <div className="field full">
              <label htmlFor="email">Email</label>
              <input
                id="email"
                type="email"
                value={form.email}
                onChange={(e) => update('email', e.target.value)}
                required
              />
            </div>
            <div className="field full">
              <label htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                minLength={6}
                value={form.password}
                onChange={(e) => update('password', e.target.value)}
                required
              />
              <div className="hint">At least 6 characters.</div>
            </div>

            {isStudent && (
              <>
                <div className="field full">
                  <label htmlFor="fullName">Full name</label>
                  <input id="fullName" value={form.fullName} onChange={(e) => update('fullName', e.target.value)} required />
                </div>
                <div className="field">
                  <label htmlFor="rollNumber">Roll number</label>
                  <input
                    id="rollNumber"
                    value={form.rollNumber}
                    onChange={(e) => update('rollNumber', e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <label htmlFor="phone">Phone</label>
                  <input id="phone" value={form.phone} onChange={(e) => update('phone', e.target.value)} />
                </div>
                <div className="field">
                  <label htmlFor="departmentId">Department</label>
                  <select
                    id="departmentId"
                    value={form.departmentId}
                    onChange={(e) => update('departmentId', e.target.value)}
                    required
                  >
                    <option value="" disabled>
                      Select…
                    </option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.code})
                      </option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label htmlFor="graduationYear">Graduation year</label>
                  <input
                    id="graduationYear"
                    type="number"
                    value={form.graduationYear}
                    onChange={(e) => update('graduationYear', e.target.value)}
                    required
                  />
                </div>
                <div className="field">
                  <label htmlFor="cgpa">CGPA</label>
                  <input
                    id="cgpa"
                    type="number"
                    step="0.01"
                    min="0"
                    max="10"
                    value={form.cgpa}
                    onChange={(e) => update('cgpa', e.target.value)}
                    required
                  />
                </div>
                <div className="field full">
                  <label htmlFor="resumeLink">Resume link (optional)</label>
                  <input
                    id="resumeLink"
                    placeholder="https://…"
                    value={form.resumeLink}
                    onChange={(e) => update('resumeLink', e.target.value)}
                  />
                </div>
              </>
            )}
          </div>

          <button className="btn btn-primary" type="submit" disabled={submitting} style={{ width: '100%' }}>
            {submitting ? 'Submitting…' : isStudent ? 'Submit for approval' : 'Create account'}
          </button>
        </form>
        <div className="auth-switch">
          Already registered? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  )
}
