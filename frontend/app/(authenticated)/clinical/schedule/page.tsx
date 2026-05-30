import { DoctorSchedule } from '@/components/clinical/doctor-schedule'
import { Skeleton } from '@/components/ui/skeleton'
import { Suspense } from 'react'

export default function ClinicalSchedulePage() {
  return (
    <Suspense fallback={<Skeleton className="h-64 w-full" />}>
      <DoctorSchedule />
    </Suspense>
  )
}
