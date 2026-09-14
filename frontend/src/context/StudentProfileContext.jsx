import { createContext, useContext, useCallback, useEffect, useState } from 'react'
import { useAuth } from './AuthContext'
import { api, ApiError } from '../api/client'

const StudentProfileContext = createContext(null)

export function StudentProfileProvider({ children }) {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    if (!user || user.role !== 'STUDENT') {
      setProfile(null)
      return
    }
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/students/me')
      setProfile(res)
    } catch (e) {
      setProfile(null)
      setError(e instanceof ApiError ? e.message : 'Could not load your student profile.')
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    refresh()
  }, [refresh])

  return (
    <StudentProfileContext.Provider value={{ profile, loading, error, refresh }}>
      {children}
    </StudentProfileContext.Provider>
  )
}

export function useStudentProfile() {
  const ctx = useContext(StudentProfileContext)
  if (!ctx) throw new Error('useStudentProfile must be used within StudentProfileProvider')
  return ctx
}
