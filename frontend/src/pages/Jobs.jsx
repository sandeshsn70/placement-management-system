import { useState } from 'react'
import CrudPage from '../components/CrudPage'
import { useAuth } from '../context/AuthContext'
import { useStudentProfile } from '../context/StudentProfileContext'
import { api, ApiError } from '../api/client'

async function loadCompanyOptions() {
  const rows = await api.get('/companies')
  return rows.map((c) => ({ value: String(c.id), label: c.name }))
}

async function loadDriveOptions() {
  const res = await api.get('/placement-drives', { page: 0, size: 200 })
  return (res.content || []).map((d) => ({ value: String(d.id), label: `${d.name} — ${d.companyName}` }))
}

const JOB_TYPE_OPTIONS = [
  { value: 'FULL_TIME', label: 'Full-time' },
  { value: 'INTERNSHIP', label: 'Internship' },
  { value: 'PPO', label: 'PPO' }
]

function ApplyButton({ job, studentId, refresh }) {
  const [state, setState] = useState('idle') // idle | applying | applied | error
  const [message, setMessage] = useState('')

  async function apply() {
    setState('applying')
    setMessage('')
    try {
      await api.post('/applications', { studentId, jobId: job.id })
      setState('applied')
      refresh()
    } catch (e) {
      setState('error')
      setMessage(e instanceof ApiError ? e.message : 'Could not apply.')
    }
  }

  if (state === 'applied') return <span className="pill pill-green">Applied</span>
  return (
    <div>
      <button className="btn btn-small btn-primary" onClick={apply} disabled={state === 'applying'}>
        {state === 'applying' ? 'Applying…' : 'Apply'}
      </button>
      {state === 'error' && <div className="hint" style={{ color: 'var(--red-seal)' }}>{message}</div>}
    </div>
  )
}

export default function Jobs() {
  const { user } = useAuth()
  const { profile } = useStudentProfile()
  const isStaff = user.role === 'ADMIN' || user.role === 'PLACEMENT_OFFICER'
  const isStudent = user.role === 'STUDENT'

  const filters = [
    { name: 'companyId', label: 'All companies', optionsKey: 'companies' },
    { name: 'jobType', label: 'All job types', options: JOB_TYPE_OPTIONS }
  ]
  if (isStudent && profile?.cgpa != null) {
    filters.push({
      name: 'maxCgpaRequired',
      label: 'Eligibility',
      options: [{ value: String(profile.cgpa), label: `I'm eligible (CGPA ${profile.cgpa})` }]
    })
  }

  return (
    <CrudPage
      title="Job Postings"
      kicker="Openings"
      description={
        isStaff
          ? 'Roles open for students to apply to, either standalone or tied to a placement drive.'
          : 'Open roles from partner companies. Apply to the ones you meet the CGPA cut-off for.'
      }
      endpoint="/jobs"
      paginated
      pageSize={10}
      canCreate={isStaff}
      canEdit={isStaff}
      canDelete={isStaff}
      emptyMessage="No job postings match these filters."
      optionSources={{ companies: loadCompanyOptions, drives: loadDriveOptions }}
      filters={filters}
      columns={[
        { key: 'title', label: 'Role' },
        { key: 'companyName', label: 'Company' },
        { key: 'jobType', label: 'Type' },
        { key: 'location', label: 'Location' },
        { key: 'minCgpa', label: 'Min. CGPA', render: (row) => <span className="num">{row.minCgpa}</span> },
        { key: 'ctc', label: 'CTC', render: (row) => <span className="num">{row.ctc}</span> },
        { key: 'deadline', label: 'Deadline' }
      ]}
      fields={[
        { name: 'title', label: 'Role title', required: true, full: true },
        { name: 'companyId', label: 'Company', type: 'select', optionsKey: 'companies', required: true },
        { name: 'placementDriveId', label: 'Placement drive (optional)', type: 'select', optionsKey: 'drives' },
        { name: 'jobType', label: 'Job type', type: 'select', options: JOB_TYPE_OPTIONS, required: true },
        { name: 'location', label: 'Location' },
        { name: 'minCgpa', label: 'Minimum CGPA', type: 'number', step: '0.01', required: true },
        { name: 'ctc', label: 'CTC (annual)', type: 'number', step: '0.01', required: true },
        { name: 'deadline', label: 'Application deadline', type: 'date', required: true },
        { name: 'description', label: 'Description', type: 'textarea', full: true }
      ]}
      toFormValues={(row) => ({
        ...row,
        companyId: String(row.companyId),
        placementDriveId: row.placementDriveId ? String(row.placementDriveId) : ''
      })}
      toRequestBody={(values) => ({
        ...values,
        companyId: Number(values.companyId),
        placementDriveId: values.placementDriveId ? Number(values.placementDriveId) : null,
        minCgpa: Number(values.minCgpa),
        ctc: Number(values.ctc)
      })}
      renderRowActions={
        isStudent
          ? (row, { refresh }) =>
              profile ? (
                <ApplyButton job={row} studentId={profile.id} refresh={refresh} />
              ) : (
                <span className="hint">Your student profile isn't linked yet</span>
              )
          : undefined
      }
    />
  )
}
