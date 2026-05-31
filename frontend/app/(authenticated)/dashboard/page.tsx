import { DashboardOverview } from '@/components/dashboard/overview'
import { Skeleton } from '@/components/ui/skeleton'
import { Suspense } from 'react'

export default function DashboardPage() {
  return (
    <Suspense fallback={<Skeleton className="h-80 w-full" />}>
      <DashboardOverview />
    </Suspense>
  )
}
