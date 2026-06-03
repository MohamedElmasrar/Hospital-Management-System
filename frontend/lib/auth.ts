export type UserRole =
  'ADMIN' | 'DOCTOR' | 'EMPLOYEE' | 'RECEPTIONIST' | 'HR_HEAD' | 'BILLER'

export type AuthUser = {
  id: string
  role: UserRole
  email?: string
}

export function subscribeAuth(callback: () => void) {
  if (typeof window === 'undefined') return () => {}
  window.addEventListener('storage', callback)
  window.addEventListener('hms-auth', callback)
  return () => {
    window.removeEventListener('storage', callback)
    window.removeEventListener('hms-auth', callback)
  }
}

export function getTokenSnapshot() {
  return typeof window === 'undefined' ? null : localStorage.getItem('token')
}

export function getServerTokenSnapshot() {
  return null
}

export function notifyAuthChanged() {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('hms-auth'))
  }
}

export function setAccessToken(token: string) {
  if (typeof window === 'undefined') return
  localStorage.setItem('token', token)
  notifyAuthChanged()
}

type JwtPayload = {
  sub?: string
  role?: string
  email?: string
  exp?: number
}

function decodePayload(token: string): JwtPayload | null {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    return JSON.parse(atob(normalized)) as JwtPayload
  } catch {
    return null
  }
}

export function getAuthUser(): AuthUser | null {
  if (typeof window === 'undefined') return null
  const token = localStorage.getItem('token')
  if (!token) return null

  const payload = decodePayload(token)
  if (
    !payload?.sub ||
    !payload.role ||
    ![
      'ADMIN',
      'DOCTOR',
      'EMPLOYEE',
      'RECEPTIONIST',
      'HR_HEAD',
      'BILLER',
    ].includes(payload.role) ||
    (payload.exp && payload.exp * 1000 <= Date.now())
  ) {
    localStorage.removeItem('token')
    return null
  }

  return {
    id: payload.sub,
    role: payload.role as UserRole,
    email: payload.email,
  }
}

export function clearAuth() {
  if (typeof window !== 'undefined') {
    localStorage.removeItem('token')
    notifyAuthChanged()
  }
}
