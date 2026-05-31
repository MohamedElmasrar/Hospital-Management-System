import { BillingReportsPageContent } from '@/components/management/billing-reports-page'
import { Skeleton } from '@/components/ui/skeleton'
import { Suspense } from 'react'

export default function BillingReportsPage() {
  return (
    <Suspense fallback={<Skeleton className="h-80 w-full" />}>
      <BillingReportsPageContent />
    </Suspense>
  )
}
