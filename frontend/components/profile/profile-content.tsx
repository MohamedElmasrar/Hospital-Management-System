'use client'

import { useMeSuspense } from '@/app/api/generated/auth/auth-controller/auth-controller'
import type { UserResponse } from '@/app/api/generated/auth/model'
import { useGetMyProfile1Suspense as useEmployeeProfileSuspense } from '@/app/api/generated/hr/employee-controller/employee-controller'
import { useGetMyProfileSuspense as usePhysicianProfileSuspense } from '@/app/api/generated/hr/physician-controller/physician-controller'
import { useGetMyProfileSuspense as usePatientProfileSuspense } from '@/app/api/generated/reception/patient-controller/patient-controller'
import { Separator } from '@/components/ui/separator'
import { employeeProfile, patientProfile, type Profile } from '@/lib/profile'
import { EmployeeProfileTabs } from './employee-profile-tabs'
import { PatientProfileTabs } from './patient-profile-tabs'
import { ProfileHeader } from './profile-header'

export function ProfileContent() {
  const { data: account } = useMeSuspense()
  const Content =
    account.role === 'PATIENT' ? PatientProfileContent : EmployeeProfileContent
  return <Content account={account} />
}

function PatientProfileContent({ account }: { account: UserResponse }) {
  const { data: patient } = usePatientProfileSuspense()
  return <ProfileView profile={patientProfile(account, patient)} />
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
  const sections = {
    patient: (
      <PatientProfileTabs profile={profile} patient={profile.patient!} />
    ),
    employee: (
      <EmployeeProfileTabs
        profile={profile}
        employee={profile.employee!}
        physician={profile.physician}
      />
    ),
  }

  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-6">
      <ProfileHeader profile={profile} />
      <Separator />
      {sections[profile.kind]}
    </div>
  )
}
