import { BillerOverview } from '@/components/dashboard/biller-overview'
import { Skeleton } from '@/components/ui/skeleton'
import { Suspense } from 'react'

export default function ConsultationPaymentsPage() {
  return (
    <Suspense fallback={<Skeleton className="h-80 w-full" />}>
      <div className="flex flex-col gap-6">
        <h1 className="text-2xl font-semibold">Consultation payments</h1>
        <BillerOverview />
      </div>
    </Suspense>
  )
}
