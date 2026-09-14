import CrudPage from '../components/CrudPage'

export default function Departments() {
  return (
    <CrudPage
      title="Departments"
      kicker="Academic structure"
      description="The academic departments students are enrolled under. Every student record references one of these."
      endpoint="/departments"
      paginated={false}
      emptyMessage="No departments recorded yet. Add the first one to get started."
      columns={[
        { key: 'id', label: 'ID' },
        { key: 'name', label: 'Department' },
        { key: 'code', label: 'Code' }
      ]}
      fields={[
        { name: 'name', label: 'Department name', required: true, placeholder: 'Computer Science' },
        { name: 'code', label: 'Department code', required: true, placeholder: 'CSE' }
      ]}
    />
  )
}
