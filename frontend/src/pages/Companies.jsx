import CrudPage from '../components/CrudPage'
import { useAuth } from '../context/AuthContext'

export default function Companies() {
  const { user } = useAuth()
  const isStaff = user.role === 'ADMIN' || user.role === 'PLACEMENT_OFFICER'

  return (
    <CrudPage
      title="Companies"
      kicker="Recruiting partners"
      description="Organisations that recruit from campus, either through standalone job postings or a placement drive."
      endpoint="/companies"
      paginated={false}
      canCreate={isStaff}
      canEdit={isStaff}
      canDelete={isStaff}
      emptyMessage="No companies on file yet."
      columns={[
        { key: 'id', label: 'ID' },
        { key: 'name', label: 'Company' },
        { key: 'industry', label: 'Industry' },
        {
          key: 'website',
          label: 'Website',
          render: (row) =>
            row.website ? (
              <a href={row.website} target="_blank" rel="noreferrer">
                {row.website}
              </a>
            ) : (
              '—'
            )
        },
        { key: 'createdAt', label: 'Added', render: (row) => (row.createdAt || '').slice(0, 10) }
      ]}
      fields={[
        { name: 'name', label: 'Company name', required: true, full: true },
        { name: 'industry', label: 'Industry', placeholder: 'Fintech, Manufacturing…' },
        { name: 'website', label: 'Website', placeholder: 'https://example.com' },
        { name: 'description', label: 'Description', type: 'textarea', full: true }
      ]}
    />
  )
}
