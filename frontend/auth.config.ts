export const authConfig = {
  refreshCookieName: 'refreshToken',
  loginPath: '/login',
  defaultAuthenticatedPath: '/dashboard',
  protectedPathPrefixes: [
    '/dashboard',
    '/profile',
    '/users',
    '/audit-logs',
    '/reception',
    '/billing',
    '/hr',
    '/clinical',
  ],
} as const

export function isProtectedPath(pathname: string) {
  return authConfig.protectedPathPrefixes.some(
    (prefix) => pathname === prefix || pathname.startsWith(`${prefix}/`)
  )
}
