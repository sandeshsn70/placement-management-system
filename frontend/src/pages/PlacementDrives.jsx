import CrudPage from '../components/CrudPage'
import StatusPill from '../components/StatusPill'
import { api } from '../api/client'

async function loadCompanyOptions() {
  const rows = await api.get('/companies')
  return rows.map((c) => ({ value: String(c.id), label: c.name }))
}

const STATUS_OPTIONS = [
  { value: 'UPCOMING', label: 'Upcoming' },
  { value: 'ONGOING', label: 'Ongoing' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' }
]

export default function PlacementDrives() {
  return (
    <CrudPage
      title="Placement Drives"
      kicker="Recruitment events"
      description="Scheduled recruitment events run with a company, often bundling several job postings together."
      endpoint="/placement-drives"
      paginated
      pageSize={10}
      emptyMessage="No placement drives scheduled yet."
      optionSources={{ companies: loadCompanyOptions }}
      filters={[{ name: 'companyId', label: 'All companies', optionsKey: 'companies' }]}
      columns={[
        { key: 'name', label: 'Drive' },
        { key: 'companyName', label: 'Company' },
        { key: 'driveDate', label: 'Date' },
        { key: 'status', label: 'Status', render: (row) => <StatusPill value={row.status} /> }
      ]}
      fields={[
        { name: 'name', label: 'Drive name', required: true, full: true },
        { name: 'companyId', label: 'Company', type: 'select', optionsKey: 'companies', required: true },
        { name: 'driveDate', label: 'Drive date', type: 'date', required: true },
        { name: 'status', label: 'Status', type: 'select', options: STATUS_OPTIONS, defaultValue: 'UPCOMING' },
        { name: 'description', label: 'Description', type: 'textarea', full: true }
      ]}
      toFormValues={(row) => ({ ...row, companyId: String(row.companyId) })}
      toRequestBody={(values) => ({ ...values, companyId: Number(values.companyId) })}
    />
  )
}
