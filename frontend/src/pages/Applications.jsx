import CrudPage from '../components/CrudPage'
import StatusPill from '../components/StatusPill'
import StatusUpdater from '../components/StatusUpdater'
import { useAuth } from '../context/AuthContext'
import { useStudentProfile } from '../context/StudentProfileContext'
import { api } from '../api/client'

async function loadStudentOptions() {
  const res = await api.get('/students', { page: 0, size: 500 })
  return (res.content || []).map((s) => ({ value: String(s.id), label: `${s.fullName} (${s.rollNumber})` }))
}

async function loadJobOptions() {
  const res = await api.get('/jobs', { page: 0, size: 500 })
  return (res.content || []).map((j) => ({ value: String(j.id), label: `${j.title} — ${j.companyName}` }))
}

const STATUS_OPTIONS = [
  { value: 'APPLIED', label: 'Applied' },
  { value: 'SHORTLISTED', label: 'Shortlisted' },
  { value: 'INTERVIEW_SCHEDULED', label: 'Interview scheduled' },
  { value: 'SELECTED', label: 'Selected' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'WITHDRAWN', label: 'Withdrawn' }
]

export default function Applications() {
  const { user } = useAuth()
  const { profile, loading: profileLoading, error: profileError } = useStudentProfile()
  const isStaff = user.role === 'ADMIN' || user.role === 'PLACEMENT_OFFICER'

  if (!isStaff && !profileLoading && !profile) {
    return (
      <div>
        <div className="page-head">
          <div className="kicker">My applications</div>
          <h1>Applications</h1>
        </div>
        <div className="banner banner-error">
          {profileError || "We couldn't find a student profile linked to your account. Contact the placement cell."}
        </div>
      </div>
    )
  }

  if (!isStaff && profileLoading) {
    return <div className="loading-line">Loading your profile…</div>
  }

  return (
    <CrudPage
      title="Applications"
      kicker={isStaff ? 'Pipeline' : 'My applications'}
      description={
        isStaff
          ? 'Every application submitted against a job posting, with its current stage in the pipeline.'
          : `Applications you've submitted, ${profile.fullName}.`
      }
      endpoint="/applications"
      paginated
      pageSize={10}
      canCreate={isStaff}
      canEdit={false}
      canDelete={isStaff}
      emptyMessage={isStaff ? 'No applications yet.' : "You haven't applied to anything yet — browse job postings to get started."}
      optionSources={{ students: loadStudentOptions, jobs: loadJobOptions }}
      filters={isStaff ? [{ name: 'studentId', label: 'All students', optionsKey: 'students' }] : []}
      fixedParams={isStaff ? {} : { studentId: profile?.id }}
      columns={[
        { key: 'studentName', label: 'Student' },
        { key: 'jobTitle', label: 'Job' },
        { key: 'appliedDate', label: 'Applied', render: (row) => (row.appliedDate || '').slice(0, 10) },
        { key: 'status', label: 'Status', render: (row) => <StatusPill value={row.status} /> }
      ]}
      fields={[
        { name: 'studentId', label: 'Student', type: 'select', optionsKey: 'students', required: true },
        { name: 'jobId', label: 'Job', type: 'select', optionsKey: 'jobs', required: true }
      ]}
      toRequestBody={(values) => ({ studentId: Number(values.studentId), jobId: Number(values.jobId) })}
      renderRowActions={
        isStaff
          ? (row, { refresh }) => (
              <StatusUpdater
                endpoint={`/applications/${row.id}/status`}
                current={row.status}
                options={STATUS_OPTIONS}
                onDone={refresh}
              />
            )
          : undefined
      }
    />
  )
}
