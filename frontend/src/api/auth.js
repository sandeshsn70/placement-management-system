import { api } from './client'

export function login(username, password) {
  return api.post('/auth/login', { username, password }, { auth: false })
}

export function register(payload) {
  return api.post('/auth/register', payload, { auth: false })
}
