import { Suspense } from 'react'
import { PayrollSettingsPage } from '@/components/hr/payroll-settings-page'
import { LoadingRows } from '@/components/hr/shared'

export default function HrPayrollSettingsPage() {
  return (
    <Suspense fallback={<LoadingRows />}>
      <PayrollSettingsPage />
    </Suspense>
  )
}
