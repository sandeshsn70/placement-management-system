const TONE_MAP = {
  // application status
  APPLIED: 'neutral',
  SHORTLISTED: 'brass',
  INTERVIEW_SCHEDULED: 'brass',
  SELECTED: 'green',
  REJECTED: 'red',
  WITHDRAWN: 'neutral',
  // offer status
  PENDING: 'brass',
  ACCEPTED: 'green',
  APPROVED: 'green',
  DECLINED: 'red',
  // drive status
  UPCOMING: 'brass',
  ONGOING: 'brass',
  COMPLETED: 'green',
  CANCELLED: 'red',
  // interview result
  PASSED: 'green',
  FAILED: 'red'
}

const CLASS_MAP = {
  green: 'pill pill-green',
  red: 'pill pill-red',
  neutral: 'pill pill-neutral',
  brass: 'pill'
}

export default function StatusPill({ value }) {
  if (!value) return <span className="pill pill-neutral">—</span>
  const tone = TONE_MAP[value] || 'neutral'
  return <span className={CLASS_MAP[tone]}>{value.replaceAll('_', ' ')}</span>
}
