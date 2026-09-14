import CrudPage from '../components/CrudPage'
import StatusPill from '../components/StatusPill'

export default function Users() {
  return (
    <CrudPage
      title="User Accounts"
      kicker="Access control"
      description="Everyone with a login to the placement system. Accounts are created through registration; from here you can review roles and remove access."
      endpoint="/users"
      paginated
      pageSize={10}
      canCreate={false}
      canEdit={false}
      emptyMessage="No accounts yet."
      columns={[
        { key: 'username', label: 'Username' },
        { key: 'email', label: 'Email' },
        { key: 'role', label: 'Role', render: (row) => <StatusPill value={row.role} /> },
        { key: 'status', label: 'Status', render: (row) => <StatusPill value={row.status} /> },
        { key: 'createdAt', label: 'Joined', render: (row) => (row.createdAt || '').slice(0, 10) }
      ]}
    />
  )
}
