import { Suspense } from 'react'
import { PayrollPage } from '@/components/hr/payroll-page'
import { LoadingRows } from '@/components/hr/shared'

export default function HrPayrollPage() {
  return (
    <Suspense fallback={<LoadingRows />}>
      <PayrollPage />
    </Suspense>
  )
}
