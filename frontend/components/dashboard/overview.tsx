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
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { Skeleton } from '@/components/ui/skeleton'
import Link from 'next/link'
import type { UserRole } from '@/lib/auth'
import { useState, useSyncExternalStore, type ReactNode } from 'react'
import { Bar, BarChart, CartesianGrid, XAxis, YAxis } from 'recharts'
import { CreateAppointmentDialog } from './create-appointment-dialog'

const LIMIT = 100
const chartConfig = {
  count: { label: 'Records', color: 'var(--chart-2)' },
} satisfies ChartConfig

function dateText(date: Date) {
  return date.toLocaleDateString('sv-SE')
}

function titleCase(value?: string) {
  return (value || 'Unspecified')
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

function attendanceState(record: {
  checkedInAt?: string
  checkedOutAt?: string
}) {
  if (record.checkedOutAt) return 'Checked out'
  if (record.checkedInAt) return 'Checked in'
  return 'No check-in'
}

function ChartCard({
  title,
  description,
  values,
  isPending,
  isError,
  retry,
}: {
  title: string
  description: string
  values: (string | undefined)[]
  isPending: boolean
  isError: boolean
  retry: () => void
}) {
  const counts = values.reduce<Record<string, number>>((result, value) => {
    const name = titleCase(value)
    result[name] = (result[name] ?? 0) + 1
    return result
  }, {})
  const data = Object.entries(counts)
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)

  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {isPending ? (
          <Skeleton className="h-56 w-full" />
        ) : isError ? (
          <div className="flex h-56 flex-col items-center justify-center gap-3 text-sm text-muted-foreground">
            <p>Could not load this chart.</p>
            <Button variant="outline" size="sm" onClick={retry}>
              Retry
            </Button>
          </div>
        ) : data.length === 0 ? (
          <p className="flex h-56 items-center justify-center text-sm text-muted-foreground">
            No records in this period.
          </p>
        ) : (
          <ChartContainer config={chartConfig} className="h-56 w-full">
            <BarChart
              accessibilityLayer
              data={data}
              layout="vertical"
              margin={{ left: 4, right: 16 }}
            >
              <CartesianGrid horizontal={false} />
              <XAxis
                type="number"
                allowDecimals={false}
                tickLine={false}
                axisLine={false}
              />
              <YAxis
                type="category"
                dataKey="name"
                width={112}
                tickLine={false}
                axisLine={false}
              />
              <ChartTooltip content={<ChartTooltipContent />} />
              <Bar dataKey="count" fill="var(--color-count)" radius={4} />
            </BarChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}

function ChartGrid({ children }: { children: ReactNode }) {
  return <div className="grid gap-4 lg:grid-cols-2">{children}</div>
}

function AdminCharts() {
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
      description="Check-in state in the latest 100 records"
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
      description="Statuses in the latest 100 requests"
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
      </ChartGrid>
    </div>
  )
}

function EmployeeCharts({ today, from }: { today: string; from: string }) {
  return (
    <ChartGrid>
      <LeaveChart own />
      <AttendanceChart from={from} to={today} />
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
  EMPLOYEE: [
    { label: 'Create invoice', href: '/billing/invoices' },
    { label: 'Record payment', href: '/billing/payments' },
    { label: 'My attendance', href: '/hr/attendance' },
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
        <AdminCharts />
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
