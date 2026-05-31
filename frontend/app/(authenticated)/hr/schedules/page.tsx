import { SchedulesPage } from '@/components/hr/schedules-page'
import { Suspense } from 'react'

export default function HrSchedulesPage() {
  return (
    <Suspense
      fallback={
        <p className="text-sm text-muted-foreground">Loading schedules…</p>
      }
    >
      <SchedulesPage />
    </Suspense>
  )
}
