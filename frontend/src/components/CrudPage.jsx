import { useEffect, useMemo, useState, useCallback } from 'react'
import Modal from './Modal'
import { api, ApiError } from '../api/client'

/*
  Config shape:
  {
    title, kicker, description,
    endpoint: '/departments',
    paginated: bool,               // true if the API returns a Spring Page<T>
    pageSize: 10,
    columns: [{ key, label, render?: (row) => node }],
    fields: [{
      name, label, type: 'text'|'number'|'textarea'|'date'|'datetime-local'|'select',
      required, step, placeholder, hint,
      options: [{ value, label }]        // for type: 'select'
      optionsKey: 'departments'          // pulls from asyncOptions loaded via optionSources
    }],
    optionSources: { departments: () => api.get('/departments').then(rows => rows.map(...)) },
    filters: [{ name, label, type: 'select', options }]   // rendered as toolbar dropdowns, sent as query params
    canCreate, canEdit, canDelete: bool,
    toResponse: (row) => row            // optional post-fetch mapping
    toRequestBody: (values) => body     // optional pre-submit mapping
    toFormValues: (row) => values       // optional mapping when opening the edit modal
    renderRowActions: (row, { refresh }) => node   // extra buttons per row, alongside edit/delete
    emptyMessage
  }
*/
export default function CrudPage(config) {
  const {
    title,
    kicker,
    description,
    endpoint,
    paginated = false,
    pageSize = 10,
    columns,
    fields = [],
    optionSources = {},
    filters = [],
    fixedParams = {},
    canCreate = true,
    canEdit = true,
    canDelete = true,
    toRequestBody,
    toFormValues,
    renderRowActions,
    emptyMessage = 'Nothing on record yet.'
  } = config

  const [rows, setRows] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filterValues, setFilterValues] = useState({})
  const [asyncOptions, setAsyncOptions] = useState({})

  const [modalMode, setModalMode] = useState(null) // 'create' | 'edit' | null
  const [editingRow, setEditingRow] = useState(null)
  const [formValues, setFormValues] = useState({})
  const [formError, setFormError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    let cancelled = false
    async function loadOptions() {
      const entries = await Promise.all(
        Object.entries(optionSources).map(async ([key, loader]) => {
          try {
            const opts = await loader()
            return [key, opts]
          } catch {
            return [key, []]
          }
        })
      )
      if (!cancelled) setAsyncOptions(Object.fromEntries(entries))
    }
    if (Object.keys(optionSources).length) loadOptions()
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const fetchRows = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const params = { ...fixedParams, ...filterValues }
      if (paginated) {
        params.page = page
        params.size = pageSize
        params.sort = 'id,desc'
        const res = await api.get(endpoint, params)
        setRows(res.content || [])
        setTotalPages(res.totalPages ?? 1)
      } else {
        const res = await api.get(endpoint, params)
        setRows(Array.isArray(res) ? res : res.content || [])
      }
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'Could not load records.')
    } finally {
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [endpoint, JSON.stringify(filterValues), JSON.stringify(fixedParams), page, paginated, pageSize])

  useEffect(() => {
    fetchRows()
  }, [fetchRows])

  useEffect(() => {
    setPage(0)
  }, [JSON.stringify(filterValues)])

  function resolvedOptions(field) {
    if (field.optionsKey) return asyncOptions[field.optionsKey] || []
    return field.options || []
  }

  function openCreate() {
    const defaults = {}
    fields.forEach((f) => {
      defaults[f.name] = f.defaultValue ?? ''
    })
    setFormValues(defaults)
    setEditingRow(null)
    setFormError('')
    setModalMode('create')
  }

  function openEdit(row) {
    const values = toFormValues ? toFormValues(row) : { ...row }
    setFormValues(values)
    setEditingRow(row)
    setFormError('')
    setModalMode('edit')
  }

  function closeModal() {
    setModalMode(null)
    setEditingRow(null)
  }

  function updateField(name, value) {
    setFormValues((prev) => ({ ...prev, [name]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setSubmitting(true)
    setFormError('')
    try {
      const body = toRequestBody ? toRequestBody(formValues) : formValues
      if (modalMode === 'create') {
        await api.post(endpoint, body)
      } else {
        await api.put(`${endpoint}/${editingRow.id}`, body)
      }
      closeModal()
      fetchRows()
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : 'Could not save this record.')
    } finally {
      setSubmitting(false)
    }
  }

  async function handleDelete(row) {
    if (!window.confirm('Remove this record? This cannot be undone.')) return
    try {
      await api.delete(`${endpoint}/${row.id}`)
      fetchRows()
    } catch (e) {
      window.alert(e instanceof ApiError ? e.message : 'Could not delete this record.')
    }
  }

  const showToolbar = canCreate || filters.length > 0

  return (
    <div>
      <div className="page-head">
        {kicker && <div className="kicker">{kicker}</div>}
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>

      {showToolbar && (
        <div className="page-toolbar">
          <div className="filter-row">
            {filters.map((f) => (
              <select
                key={f.name}
                value={filterValues[f.name] ?? ''}
                onChange={(e) =>
                  setFilterValues((prev) => ({ ...prev, [f.name]: e.target.value || undefined }))
                }
              >
                <option value="">{f.label}</option>
                {(f.optionsKey ? asyncOptions[f.optionsKey] || [] : f.options || []).map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            ))}
          </div>
          {canCreate && (
            <button className="btn btn-primary" onClick={openCreate}>
              + New entry
            </button>
          )}
        </div>
      )}

      {error && <div className="banner banner-error">{error}</div>}

      <div className="surface table-wrap">
        <table className="ledger">
          <thead>
            <tr>
              {columns.map((c) => (
                <th key={c.key}>{c.label}</th>
              ))}
              {(canEdit || canDelete || renderRowActions) && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr className="empty-row">
                <td colSpan={columns.length + 1}>Loading records…</td>
              </tr>
            ) : rows.length === 0 ? (
              <tr className="empty-row">
                <td colSpan={columns.length + 1}>{emptyMessage}</td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id}>
                  {columns.map((c) => (
                    <td key={c.key}>{c.render ? c.render(row) : row[c.key]}</td>
                  ))}
                  {(canEdit || canDelete || renderRowActions) && (
                    <td>
                      <div className="actions-cell">
                        {renderRowActions && renderRowActions(row, { refresh: fetchRows })}
                        {canEdit && (
                          <button className="btn btn-small" onClick={() => openEdit(row)}>
                            Edit
                          </button>
                        )}
                        {canDelete && (
                          <button className="btn btn-small btn-danger" onClick={() => handleDelete(row)}>
                            Delete
                          </button>
                        )}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
        {paginated && !loading && rows.length > 0 && (
          <div className="pagination">
            <button
              className="btn btn-small"
              disabled={page === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              Previous
            </button>
            <span>
              Page {page + 1} of {Math.max(totalPages, 1)}
            </span>
            <button
              className="btn btn-small"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </button>
          </div>
        )}
      </div>

      {modalMode && (
        <Modal title={modalMode === 'create' ? `Add ${title.replace(/s$/, '')}` : `Edit record`} onClose={closeModal}>
          <form onSubmit={handleSubmit}>
            {formError && <div className="banner banner-error">{formError}</div>}
            <div className="form-grid">
              {fields.map((f) => (
                <div className={'field' + (f.full ? ' full' : '')} key={f.name}>
                  <label htmlFor={f.name}>{f.label}</label>
                  {f.type === 'textarea' ? (
                    <textarea
                      id={f.name}
                      required={f.required}
                      value={formValues[f.name] ?? ''}
                      placeholder={f.placeholder}
                      onChange={(e) => updateField(f.name, e.target.value)}
                    />
                  ) : f.type === 'select' ? (
                    <select
                      id={f.name}
                      required={f.required}
                      value={formValues[f.name] ?? ''}
                      onChange={(e) => updateField(f.name, e.target.value)}
                    >
                      <option value="" disabled>
                        Select…
                      </option>
                      {resolvedOptions(f).map((o) => (
                        <option key={o.value} value={o.value}>
                          {o.label}
                        </option>
                      ))}
                    </select>
                  ) : (
                    <input
                      id={f.name}
                      type={f.type || 'text'}
                      step={f.step}
                      required={f.required}
                      placeholder={f.placeholder}
                      value={formValues[f.name] ?? ''}
                      onChange={(e) => updateField(f.name, e.target.value)}
                    />
                  )}
                  {f.hint && <div className="hint">{f.hint}</div>}
                </div>
              ))}
            </div>
            <div className="form-actions">
              <button type="button" className="btn" onClick={closeModal}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={submitting}>
                {submitting ? 'Saving…' : 'Save'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
