'use client'

import { useMeSuspense } from '@/app/api/generated/auth/auth-controller/auth-controller'
import type { UserResponse } from '@/app/api/generated/auth/model'
import { useGetMyProfile1Suspense as useEmployeeProfileSuspense } from '@/app/api/generated/hr/employee-controller/employee-controller'
import { useGetMyProfileSuspense as usePhysicianProfileSuspense } from '@/app/api/generated/hr/physician-controller/physician-controller'
import { Separator } from '@/components/ui/separator'
import { employeeProfile, type Profile } from '@/lib/profile'
import { EmployeeProfileTabs } from './employee-profile-tabs'
import { ProfileHeader } from './profile-header'

export function ProfileContent() {
  const { data: account } = useMeSuspense()
  return <EmployeeProfileContent account={account} />
}

function EmployeeProfileContent({ account }: { account: UserResponse }) {
  const Content =
    account.role === 'DOCTOR' ? PhysicianProfileContent : StaffProfileContent
  return <Content account={account} />
}

function StaffProfileContent({ account }: { account: UserResponse }) {
  const { data: employee } = useEmployeeProfileSuspense()
  return <ProfileView profile={employeeProfile(account, employee)} />
}

function PhysicianProfileContent({ account }: { account: UserResponse }) {
  const { data: employee } = useEmployeeProfileSuspense()
  const { data: physician } = usePhysicianProfileSuspense()
  return <ProfileView profile={employeeProfile(account, employee, physician)} />
}

function ProfileView({ profile }: { profile: Profile }) {
  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-6">
      <ProfileHeader profile={profile} />
      <Separator />
      <EmployeeProfileTabs
        profile={profile}
        employee={profile.employee!}
        physician={profile.physician}
      />
    </div>
  )
}
