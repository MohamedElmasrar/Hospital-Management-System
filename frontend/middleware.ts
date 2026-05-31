import { NextResponse, type NextRequest } from 'next/server'
import { authConfig, isProtectedPath } from './auth.config'

const API_URL =
  process.env.API_URL ??
  process.env.NEXT_PUBLIC_API_URL ??
  'http://localhost:8000'

async function hasValidSession(request: NextRequest) {
  const refreshToken = request.cookies.get(authConfig.refreshCookieName)?.value
  if (!refreshToken) return false

  try {
    const response = await fetch(`${API_URL}/auth/refresh`, {
      headers: {
        cookie: `${authConfig.refreshCookieName}=${encodeURIComponent(refreshToken)}`,
      },
      cache: 'no-store',
    })

    return response.ok
  } catch {
    return false
  }
}

function loginRedirect(request: NextRequest) {
  const loginUrl = new URL(authConfig.loginPath, request.url)
  loginUrl.searchParams.set(
    'callBackUrl',
    `${request.nextUrl.pathname}${request.nextUrl.search}`
  )

  const response = NextResponse.redirect(loginUrl)
  response.cookies.delete(authConfig.refreshCookieName)
  return response
}

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl
  const sessionIsValid = await hasValidSession(request)

  if (isProtectedPath(pathname) && !sessionIsValid) {
    return loginRedirect(request)
  }

  if (pathname === authConfig.loginPath && sessionIsValid) {
    return NextResponse.redirect(
      new URL(authConfig.defaultAuthenticatedPath, request.url)
    )
  }

  return NextResponse.next()
}

export const config = {
  matcher: [
    '/dashboard/:path*',
    '/profile/:path*',
    '/users/:path*',
    '/audit-logs/:path*',
    '/reception/:path*',
    '/billing/:path*',
    '/hr/:path*',
    '/clinical/:path*',
    '/login',
  ],
}
