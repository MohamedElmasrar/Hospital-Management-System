'use client'

import {
  CalendarDays,
  ClipboardList,
  CreditCard,
  FileText,
  Hospital,
  LayoutDashboard,
  LockKeyhole,
  Logs,
  Receipt,
  Stethoscope,
  Users,
  WalletCards,
} from 'lucide-react'
import Link from 'next/link'
import {
  Sidebar,
  SidebarContent,
  SidebarHeader,
  useSidebar,
} from '@/components/ui/sidebar'
import { cn } from '@/lib/utils'
import {
  getServerTokenSnapshot,
  getTokenSnapshot,
  getAuthUser,
  subscribeAuth,
  type UserRole,
} from '@/lib/auth'
import { useSyncExternalStore } from 'react'
import { Logo } from './Logo'
import DashboardNavigation, { type Route } from './nav-main'

const iconClassName = 'size-4'

function link(title: string, path: string, icon: React.ReactNode): Route {
  return {
    id: path || 'dashboard',
    title,
    link: path ? `/${path}` : '/dashboard',
    icon,
  }
}

function routesForRole(role: UserRole): Route[] {
  const reception = [
    link('Patients', 'reception/patients', <Users className={iconClassName} />),
    link(
      'Appointments',
      'reception/appointments',
      <CalendarDays className={iconClassName} />
    ),
    link(
      'Waiting Queue',
      'reception/queue',
      <ClipboardList className={iconClassName} />
    ),
    link(
      'Admissions',
      'reception/admissions',
      <Hospital className={iconClassName} />
    ),
  ]
  const billing = [
    link(
      'Consultation payments',
      'billing/consultations',
      <Receipt className={iconClassName} />
    ),
    link('Invoices', 'billing/invoices', <Receipt className={iconClassName} />),
    link(
      'Payments',
      'billing/payments',
      <CreditCard className={iconClassName} />
    ),
    link(
      'Insurance',
      'billing/insurance',
      <WalletCards className={iconClassName} />
    ),
    link('Reports', 'billing/reports', <FileText className={iconClassName} />),
  ]
  const hr = [
    link('Employees', 'hr/employees', <Users className={iconClassName} />),
    link(
      'Departments',
      'hr/departments',
      <Hospital className={iconClassName} />
    ),
    link(
      'Schedules',
      'hr/schedules',
      <CalendarDays className={iconClassName} />
    ),
    link(
      'Leave Requests',
      'hr/leave',
      <ClipboardList className={iconClassName} />
    ),
    link('Attendance', 'hr/attendance', <Logs className={iconClassName} />),
    link('Payroll', 'hr/payroll', <WalletCards className={iconClassName} />),
  ]

  const roleRoutes: Record<UserRole, Route[]> = {
    ADMIN: [
      link('Users', 'users', <LockKeyhole className={iconClassName} />),
      link('Audit Logs', 'audit-logs', <Logs className={iconClassName} />),
      ...reception,
      ...billing,
      ...hr,
    ],
    DOCTOR: [
      link(
        'My Schedule',
        'clinical/schedule',
        <CalendarDays className={iconClassName} />
      ),
      link(
        'Patients',
        'clinical/patients',
        <Users className={iconClassName} />
      ),
      link(
        'Consultations',
        'clinical/consultations',
        <Stethoscope className={iconClassName} />
      ),
    ],
    BILLER: billing,
    EMPLOYEE: [
      link(
        'My Schedule',
        'hr/schedules',
        <CalendarDays className={iconClassName} />
      ),
      link(
        'Leave Requests',
        'hr/leave',
        <ClipboardList className={iconClassName} />
      ),
      link(
        'My Attendance',
        'hr/attendance',
        <Logs className={iconClassName} />
      ),
    ],
    RECEPTIONIST: reception,
    HR_HEAD: hr,
  }

  return [
    link('Dashboard', '', <LayoutDashboard className={iconClassName} />),
    ...roleRoutes[role],
  ]
}

export function DashboardSidebar() {
  const { state } = useSidebar()
  const token = useSyncExternalStore(
    subscribeAuth,
    getTokenSnapshot,
    getServerTokenSnapshot
  )
  const role = token ? (getAuthUser()?.role ?? null) : null
  const isCollapsed = state === 'collapsed'

  return (
    <Sidebar collapsible="icon" variant="inset">
      <SidebarHeader
        className={cn(
          'flex md:pt-3.5',
          isCollapsed
            ? 'flex-row items-center justify-between gap-y-4 md:flex-col md:items-start md:justify-start'
            : 'flex-row items-center justify-between'
        )}
      >
        <Link className="flex items-center gap-2" href="/dashboard">
          <Logo className="h-8 w-8" />
          {!isCollapsed && (
            <span className="font-semibold text-black dark:text-white">
              HMS
            </span>
          )}
        </Link>
      </SidebarHeader>
      <SidebarContent className="gap-4 px-2 py-4">
        {role && <DashboardNavigation routes={routesForRole(role)} />}
      </SidebarContent>
      <div className="px-4 pb-4 text-xs text-muted-foreground">
        {role ? role.replace('_', ' ') : null}
      </div>
    </Sidebar>
  )
}
