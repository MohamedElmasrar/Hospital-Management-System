'use client'

import {
  useAuditLogs,
  useListUsers,
} from '@/app/api/generated/auth/admin-user-controller/admin-user-controller'
import { useMe } from '@/app/api/generated/auth/auth-controller/auth-controller'
import { useGetMyProfile as usePhysicianProfile } from '@/app/api/generated/hr/physician-controller/physician-controller'
import {
  useListAttendance,
  useListLeave,
} from '@/app/api/generated/hr/workforce-controller/workforce-controller'
import { useSearchTickets } from '@/app/api/generated/reception/queue-controller/queue-controller'
import {
  useSearchDoctorConsultations,
  useSearchReservations,
} from '@/app/api/generated/reception/reservation-controller/reservation-controller'
import {
  OrganizationField,
  useOrganization,
  useRole,
} from '@/components/hr/shared'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import Link from 'next/link'
import type { UserRole } from '@/lib/auth'
import { useState, useSyncExternalStore } from 'react'
import { BillerOverview } from './biller-overview'
import { ChartCard, ChartGrid, TrendChart, titleCase } from './charts'
import {
  PersonalPayrollTrend,
  EmployeeScheduleTrend,
  DoctorAvailabilityChart,
  HrPayrollCharts,
  BillerCharts,
  daySeries,
  appointmentHourSeries,
} from './work-charts'
import { CreateAppointmentDialog } from './create-appointment-dialog'

const LIMIT = 100

function dateText(date: Date) {
  return date.toLocaleDateString('sv-SE')
}

function attendanceState(record: {
  checkedInAt?: string
  checkedOutAt?: string
}) {
  if (record.checkedOutAt) return 'Checked out'
  if (record.checkedInAt) return 'Checked in'
  return 'No check-in'
}

function AdminCharts({ today }: { today: string }) {
  const users = useListUsers()
  const audit = useAuditLogs()
  return (
    <ChartGrid>
      <ChartCard
        title="Accounts by role"
        description="All application accounts"
        values={users.data?.map((user) => user.role) ?? []}
        isPending={users.isPending}
        isError={users.isError}
        retry={() => void users.refetch()}
      />
      <ChartCard
        title="Account activity"
        description="Recorded admin actions"
        values={audit.data?.map((entry) => entry.action) ?? []}
        isPending={audit.isPending}
        isError={audit.isError}
        retry={() => void audit.refetch()}
      />
      <ChartCard
        title="Account access"
        description="Active and inactive application accounts"
        values={
          users.data?.map((user) => (user.active ? 'Active' : 'Inactive')) ?? []
        }
        isPending={users.isPending}
        isError={users.isError}
        retry={() => void users.refetch()}
      />
      <TrendChart
        title="Admin activity over time"
        description="Recorded actions by date over the last seven days"
        data={daySeries(
          today,
          audit.data?.map((entry) => entry.createdAt?.slice(0, 10)) ?? []
        )}
        label="Actions"
        isPending={audit.isPending}
        isError={audit.isError}
        retry={() => void audit.refetch()}
      />
    </ChartGrid>
  )
}

function ReceptionCharts({ today }: { today: string }) {
  const reservations = useSearchReservations({
    date: today,
    page: 0,
    size: LIMIT,
  })
  const tickets = useSearchTickets({ date: today, page: 0, size: LIMIT })
  return (
    <ChartGrid>
      <ChartCard
        title="Today's appointments"
        description="Statuses in the latest 100 appointments"
        values={
          reservations.data?.content?.map(
            (item) => item.statusName ?? item.statusCode
          ) ?? []
        }
        isPending={reservations.isPending}
        isError={reservations.isError}
        retry={() => void reservations.refetch()}
      />
      <ChartCard
        title="Today's queue"
        description="Statuses in the latest 100 tickets"
        values={tickets.data?.content?.map((item) => item.status) ?? []}
        isPending={tickets.isPending}
        isError={tickets.isError}
        retry={() => void tickets.refetch()}
      />
      <ChartCard
        title="Appointment payment status"
        description="Payment state in today's latest 100 appointments; cancelled visits are separate"
        values={
          reservations.data?.content?.map((item) =>
            item.statusCode === 'CANCELLED' || item.statusCode === 'NO_SHOW'
              ? item.statusCode
              : item.isPaid
                ? 'Paid'
                : 'Unpaid'
          ) ?? []
        }
        isPending={reservations.isPending}
        isError={reservations.isError}
        retry={() => void reservations.refetch()}
      />
      <TrendChart
        title="Appointments by hour"
        description="Start times in today's latest 100 appointments, excluding cancelled visits and no-shows"
        data={appointmentHourSeries(reservations.data?.content ?? [])}
        label="Appointments"
        isPending={reservations.isPending}
        isError={reservations.isError}
        retry={() => void reservations.refetch()}
      />
    </ChartGrid>
  )
}

function AttendanceChart({
  organizationId,
  from,
  to,
}: {
  organizationId?: string
  from: string
  to: string
}) {
  const attendance = useListAttendance({
    organizationId,
    from,
    to,
    page: 0,
    size: LIMIT,
  })
  return (
    <ChartCard
      title="Attendance this month"
      description="Check-in state across up to 100 records this month"
      values={attendance.data?.content?.map(attendanceState) ?? []}
      isPending={attendance.isPending}
      isError={attendance.isError}
      retry={() => void attendance.refetch()}
    />
  )
}

function LeaveChart({
  organizationId,
  own = false,
}: {
  organizationId?: string
  own?: boolean
}) {
  const leave = useListLeave({ organizationId, page: 0, size: LIMIT })
  return (
    <ChartCard
      title={own ? 'My leave requests' : 'Leave requests'}
      description="Statuses across up to 100 requests"
      values={leave.data?.content?.map((item) => item.status) ?? []}
      isPending={leave.isPending}
      isError={leave.isError}
      retry={() => void leave.refetch()}
    />
  )
}

function DoctorCharts({ today, from }: { today: string; from: string }) {
  const physician = usePhysicianProfile()
  const consultations = useSearchDoctorConsultations(
    physician.data?.id ?? '',
    { date: today, page: 0, size: LIMIT },
    { query: { enabled: !!physician.data?.id } }
  )
  return (
    <ChartGrid>
      <ChartCard
        title="My consultations today"
        description="Statuses in the latest 100 consultations"
        values={
          consultations.data?.content?.map(
            (item) => item.statusName ?? item.statusCode
          ) ?? []
        }
        isPending={
          physician.isPending || (!physician.isError && consultations.isPending)
        }
        isError={physician.isError || consultations.isError}
        retry={() => {
          if (physician.isError) void physician.refetch()
          else void consultations.refetch()
        }}
      />
      <AttendanceChart from={from} to={today} />
      <DoctorAvailabilityChart
        date={today}
        practitionerId={physician.data?.id}
        profilePending={physician.isPending}
        profileError={physician.isError}
        retryProfile={() => void physician.refetch()}
      />
      <PersonalPayrollTrend />
    </ChartGrid>
  )
}

function HrCharts({ today, from }: { today: string; from: string }) {
  const organizations = useOrganization()
  const organizationId = organizations.organizationId
  if (organizations.isPending) return <Skeleton className="h-64 w-full" />
  if (organizations.isError)
    return (
      <Button variant="outline" onClick={() => void organizations.refetch()}>
        Retry loading organizations
      </Button>
    )
  if (!organizationId)
    return (
      <p className="text-sm text-muted-foreground">
        No managed organization is available.
      </p>
    )
  return (
    <div className="flex flex-col gap-4">
      {organizations.data && organizations.data.length > 1 && (
        <OrganizationField
          value={organizationId}
          onChange={organizations.setSelected}
          options={organizations.data.filter(
            (item): item is { id: string; name: string } =>
              !!item.id && !!item.name
          )}
        />
      )}
      <ChartGrid>
        <LeaveChart organizationId={organizationId} />
        <AttendanceChart
          organizationId={organizationId}
          from={from}
          to={today}
        />
        <HrPayrollCharts key={organizationId} organizationId={organizationId} />
      </ChartGrid>
    </div>
  )
}

function EmployeeCharts({ today, from }: { today: string; from: string }) {
  return (
    <ChartGrid>
      <LeaveChart own />
      <AttendanceChart from={from} to={today} />
      <EmployeeScheduleTrend today={today} />
      <PersonalPayrollTrend />
    </ChartGrid>
  )
}

function AccountCard() {
  const account = useMe()
  return (
    <Card>
      <CardHeader>
        <CardTitle>Account</CardTitle>
        <CardDescription>Your application access</CardDescription>
      </CardHeader>
      <CardContent>
        {account.isPending ? (
          <Skeleton className="h-8 w-48" />
        ) : account.isError ? (
          <Button
            variant="outline"
            size="sm"
            onClick={() => void account.refetch()}
          >
            Retry loading account
          </Button>
        ) : (
          <div className="flex flex-wrap items-center gap-3">
            <span className="text-sm">{account.data?.email ?? 'No email'}</span>
            <Badge variant={account.data?.active ? 'secondary' : 'outline'}>
              {account.data?.active ? 'Active' : 'Inactive'}
            </Badge>
            <Badge variant="outline">{titleCase(account.data?.role)}</Badge>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

const headings: Record<UserRole, string> = {
  ADMIN: 'Hospital overview',
  BILLER: 'Billing overview',
  RECEPTIONIST: 'Reception overview',
  DOCTOR: 'Clinical overview',
  HR_HEAD: 'Workforce overview',
  EMPLOYEE: 'My work overview',
}

const quickLinks: Record<UserRole, { label: string; href: string }[]> = {
  ADMIN: [
    { label: 'Manage users', href: '/users' },
    { label: 'Review invoices', href: '/billing/invoices' },
    { label: 'Audit logs', href: '/audit-logs' },
  ],
  RECEPTIONIST: [
    { label: 'Find patient', href: '/reception/patients' },
    { label: 'Appointments', href: '/reception/appointments' },
    { label: 'Waiting queue', href: '/reception/queue' },
  ],
  DOCTOR: [
    { label: 'My schedule', href: '/clinical/schedule' },
    { label: 'Consultations', href: '/clinical/consultations' },
    { label: 'Patients', href: '/clinical/patients' },
  ],
  HR_HEAD: [
    { label: 'Employees', href: '/hr/employees' },
    { label: 'Leave requests', href: '/hr/leave' },
    { label: 'Schedules', href: '/hr/schedules' },
  ],
  BILLER: [
    { label: 'Create invoice', href: '/billing/invoices' },
    { label: 'Record payment', href: '/billing/payments' },
    { label: 'Reports', href: '/billing/reports' },
  ],
  EMPLOYEE: [
    { label: 'My attendance', href: '/hr/attendance' },
    { label: 'My schedule', href: '/hr/schedules' },
    { label: 'Leave requests', href: '/hr/leave' },
  ],
}

function QuickActions({ role }: { role: UserRole }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Quick actions</CardTitle>
        <CardDescription>
          {role === 'RECEPTIONIST'
            ? 'Register an arrival and send the patient to billing before the consultation.'
            : 'Open the tasks you use most often.'}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-wrap gap-2">
        {quickLinks[role].map((item) => (
          <Button
            key={item.href}
            variant="outline"
            nativeButton={false}
            render={<Link href={item.href} />}
          >
            {item.label}
          </Button>
        ))}
      </CardContent>
    </Card>
  )
}

function DashboardContent() {
  const role = useRole()
  const [date] = useState(() => new Date())
  const today = dateText(date)
  const monthStart = dateText(new Date(date.getFullYear(), date.getMonth(), 1))

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold">
            {role ? headings[role] : 'Dashboard'}
          </h1>
          <p className="text-sm text-muted-foreground">
            A quick view of your account and current work.
          </p>
        </div>
        {role === 'RECEPTIONIST' && <CreateAppointmentDialog />}
      </div>
      <AccountCard />
      {role && <QuickActions role={role} />}
      {!role ? (
        <Skeleton className="h-64 w-full" />
      ) : role === 'ADMIN' ? (
        <AdminCharts today={today} />
      ) : role === 'BILLER' ? (
        <>
          <ChartGrid>
            <BillerCharts today={today} />
          </ChartGrid>
          <BillerOverview />
        </>
      ) : role === 'RECEPTIONIST' ? (
        <ReceptionCharts today={today} />
      ) : role === 'DOCTOR' ? (
        <DoctorCharts today={today} from={monthStart} />
      ) : role === 'HR_HEAD' ? (
        <HrCharts today={today} from={monthStart} />
      ) : (
        <EmployeeCharts today={today} from={monthStart} />
      )}
    </div>
  )
}

function subscribeHydration() {
  return () => {}
}

function getClientHydrationSnapshot() {
  return true
}

function getServerHydrationSnapshot() {
  return false
}

export function DashboardOverview() {
  const hydrated = useSyncExternalStore(
    subscribeHydration,
    getClientHydrationSnapshot,
    getServerHydrationSnapshot
  )

  if (!hydrated) return <Skeleton className="h-80 w-full" />

  return <DashboardContent />
}
