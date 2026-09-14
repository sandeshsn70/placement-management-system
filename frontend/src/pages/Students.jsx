import CrudPage from '../components/CrudPage'
import { api } from '../api/client'

async function loadDepartmentOptions() {
  const rows = await api.get('/departments')
  return rows.map((d) => ({ value: String(d.id), label: `${d.name} (${d.code})` }))
}

export default function Students() {
  return (
    <CrudPage
      title="Students"
      kicker="Candidate register"
      description="Every enrolled student eligible for placement, with academic standing and contact details. Students who register themselves appear here automatically once approved."
      endpoint="/students"
      paginated
      pageSize={10}
      emptyMessage="No students enrolled yet."
      optionSources={{ departments: loadDepartmentOptions }}
      filters={[{ name: 'departmentId', label: 'All departments', optionsKey: 'departments' }]}
      columns={[
        { key: 'rollNumber', label: 'Roll no.' },
        { key: 'fullName', label: 'Name' },
        { key: 'departmentName', label: 'Department' },
        { key: 'cgpa', label: 'CGPA', render: (row) => <span className="num">{row.cgpa?.toFixed(2)}</span> },
        { key: 'graduationYear', label: 'Grad. year' },
        { key: 'email', label: 'Email' }
      ]}
      fields={[
        { name: 'fullName', label: 'Full name', required: true, full: true },
        { name: 'rollNumber', label: 'Roll number', required: true },
        { name: 'departmentId', label: 'Department', type: 'select', optionsKey: 'departments', required: true },
        { name: 'email', label: 'Email', type: 'text', required: true },
        { name: 'phone', label: 'Phone' },
        { name: 'cgpa', label: 'CGPA', type: 'number', step: '0.01', required: true },
        { name: 'graduationYear', label: 'Graduation year', type: 'number', required: true },
        { name: 'resumeLink', label: 'Resume link', full: true, placeholder: 'https://…' }
      ]}
      toFormValues={(row) => ({ ...row, departmentId: String(row.departmentId) })}
      toRequestBody={(values) => ({
        ...values,
        departmentId: Number(values.departmentId),
        cgpa: Number(values.cgpa),
        graduationYear: Number(values.graduationYear)
      })}
    />
  )
}
