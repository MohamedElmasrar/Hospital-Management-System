'use client'

import { useMe } from '@/app/api/generated/auth/auth-controller/auth-controller'
import type {
  UserResponse,
  UserResponseRole,
} from '@/app/api/generated/auth/model'
import { useGetMyProfile1 as useEmployeeProfile } from '@/app/api/generated/hr/employee-controller/employee-controller'
import type {
  EmployeeDetailResponseDTO,
  PhysicianResponseDTO,
} from '@/app/api/generated/hr/model'
import { useGetMyProfile as usePhysicianProfile } from '@/app/api/generated/hr/physician-controller/physician-controller'

export type EmployeeProfile = EmployeeDetailResponseDTO
export type PhysicianProfile = PhysicianResponseDTO

type ProfileAccount = Required<
  Pick<UserResponse, 'id' | 'email' | 'role' | 'active'>
>

export type Profile = {
  account: ProfileAccount
  employee: EmployeeProfile | null
  physician: PhysicianProfile | null
  displayName: string
  initials: string
}

function accountFrom(response: UserResponse): ProfileAccount {
  return {
    id: response.id ?? '',
    email: response.email ?? '',
    role: response.role ?? 'EMPLOYEE',
    active: response.active ?? false,
  }
}

function getInitials(firstName?: string, lastName?: string) {
  return `${firstName?.charAt(0) ?? ''}${lastName?.charAt(0) ?? ''}`.toUpperCase()
}

export function employeeProfile(
  accountResponse: UserResponse,
  employee: EmployeeProfile,
  physician: PhysicianProfile | null = null
): Profile {
  const account = accountFrom(accountResponse)
  const displayName =
    `${employee.givenName ?? ''} ${employee.familyName ?? ''}`.trim()

  return {
    account,
    employee,
    physician,
    displayName,
    initials: getInitials(employee.givenName, employee.familyName),
  }
}

export function useProfile() {
  const account = useMe()
  const role = account.data?.role
  const employee = useEmployeeProfile({
    query: { enabled: Boolean(role) },
  })
  const physician = usePhysicianProfile({
    query: { enabled: role === 'DOCTOR' },
  })

  const data =
    account.data && role && employee.data
      ? employeeProfile(account.data, employee.data, physician.data ?? null)
      : undefined

  return { data }
}

export function formatRole(role: UserResponseRole) {
  return role
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}
