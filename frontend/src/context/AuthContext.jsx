import { createContext, useContext, useMemo, useState, useCallback } from 'react'
import * as authApi from '../api/auth'

const AuthContext = createContext(null)

function readStoredUser() {
  const raw = localStorage.getItem('pms_user')
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser)

  const login = useCallback(async (username, password) => {
    const res = await authApi.login(username, password)
    const nextUser = { username: res.username, role: res.role }
    localStorage.setItem('pms_token', res.token)
    localStorage.setItem('pms_user', JSON.stringify(nextUser))
    setUser(nextUser)
    return nextUser
  }, [])

  const register = useCallback(async (fields) => {
    const res = await authApi.register(fields)
    // Student registrations come back with no token: the account is PENDING
    // until a placement officer or admin approves it, so there's nothing to
    // log in with yet.
    if (!res.token) {
      return { pending: true, message: res.message }
    }
    const nextUser = { username: res.username, role: res.role }
    localStorage.setItem('pms_token', res.token)
    localStorage.setItem('pms_user', JSON.stringify(nextUser))
    setUser(nextUser)
    return { pending: false, user: nextUser }
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('pms_token')
    localStorage.removeItem('pms_user')
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, login, register, logout }), [user, login, register, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
