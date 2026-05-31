import { AttendancePage } from '@/components/hr/attendance-page'
import { Suspense } from 'react'

export default function HrAttendancePage() {
  return (
    <Suspense
      fallback={
        <p className="text-sm text-muted-foreground">Loading attendance…</p>
      }
    >
      <AttendancePage />
    </Suspense>
  )
}
