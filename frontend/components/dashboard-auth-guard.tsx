'use client'

import { getAuthUser } from '@/lib/auth'
import { usePathname, useRouter } from 'next/navigation'
import {
  getServerTokenSnapshot,
  getTokenSnapshot,
  subscribeAuth,
} from '@/lib/auth'
import { useEffect, useSyncExternalStore } from 'react'

const subscribeHydration = () => () => {}
const getHydratedSnapshot = () => true
const getServerHydratedSnapshot = () => false

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
  const isHydrated = useSyncExternalStore(
    subscribeHydration,
    getHydratedSnapshot,
    getServerHydratedSnapshot
  )

  useEffect(() => {
    if (!isHydrated) return

    if (!token || !getAuthUser()) {
      router.replace(`/login?callBackUrl=${encodeURIComponent(pathname)}`)
    }
  }, [isHydrated, pathname, router, token])

  if (!isHydrated || !token) return null
  return children
}
