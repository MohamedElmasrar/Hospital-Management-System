'use client'

import { getAuthUser } from '@/lib/auth'
import { usePathname, useRouter } from 'next/navigation'
import {
  getServerTokenSnapshot,
  getTokenSnapshot,
  subscribeAuth,
} from '@/lib/auth'
import { useEffect, useSyncExternalStore } from 'react'

export function DashboardAuthGuard({
  children,
}: {
  children: React.ReactNode
}) {
  const router = useRouter()
  const pathname = usePathname()
  const token = useSyncExternalStore(
    subscribeAuth,
    getTokenSnapshot,
    getServerTokenSnapshot
  )

  useEffect(() => {
    if (!token || !getAuthUser()) {
      router.replace(`/login?callBackUrl=${encodeURIComponent(pathname)}`)
    }
  }, [pathname, router, token])

  if (!token) return null
  return children
}
