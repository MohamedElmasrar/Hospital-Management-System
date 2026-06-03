'use client'

import { useId, useState } from 'react'
import { useGetMyProfile1 } from '@/app/api/generated/hr/employee-controller/employee-controller'
import {
  useListMyPayrollRecords,
  useListPayrollRecords,
} from '@/app/api/generated/hr/payroll/payroll'
import { useShifts } from '@/app/api/generated/hr/workforce-controller/workforce-controller'
import type { RecordResponse } from '@/app/api/generated/hr/model'
import { useGetAvailableSlots } from '@/app/api/generated/reception/schedule-controller/schedule-controller'
import type { ReservationResponse } from '@/app/api/generated/reception/model'
import { useListInvoices } from '@/app/api/generated/billing/invoices/invoices'
import { useUnpaidInvoices } from '@/app/api/generated/billing/billing-report-controller/billing-report-controller'
import { Field, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { ChartCard, TrendChart } from './charts'

function offsetDay(day: string, offset: number) {
  const [year, month, date] = day.split('-').map(Number)
  const result = new Date(year, month - 1, date + offset, 12)
  return `${result.getFullYear()}-${String(result.getMonth() + 1).padStart(2, '0')}-${String(result.getDate()).padStart(2, '0')}`
}

export function daySeries(end: string, dates: (string | undefined)[]) {
  const counts = new Map<string, number>()
  for (const day of dates) if (day) counts.set(day, (counts.get(day) ?? 0) + 1)
  return Array.from({ length: 7 }, (_, index) => {
    const day = offsetDay(end, index - 6)
    return { name: day.slice(5), value: counts.get(day) ?? 0 }
  })
}

function timeHour(value: ReservationResponse['startTime'] | string) {
  return typeof value === 'string' ? Number(value.slice(0, 2)) : value?.hour
}

export function appointmentHourSeries(appointments: ReservationResponse[]) {
  const counts = new Map<number, number>()
  for (const appointment of appointments) {
    if (
      appointment.statusCode === 'CANCELLED' ||
      appointment.statusCode === 'NO_SHOW'
    )
      continue
    const hour = timeHour(appointment.startTime)
    if (hour !== undefined && Number.isInteger(hour) && hour >= 0 && hour < 24)
      counts.set(hour, (counts.get(hour) ?? 0) + 1)
  }
  return Array.from({ length: 24 }, (_, hour) => ({
    name: `${String(hour).padStart(2, '0')}:00`,
    value: counts.get(hour) ?? 0,
  }))
}

function PayrollAmountChart({
  records,
  title,
  description,
  isPending,
  isError,
  retry,
}: {
  records: RecordResponse[]
  title: string
  description: string
  isPending: boolean
  isError: boolean
  retry: () => void
}) {
  const id = useId()
  const [selected, setSelected] = useState('')
  const currencies = [
    ...new Set(
      records.flatMap((record) => (record.currency ? [record.currency] : []))
    ),
  ].sort()
  const currency = currencies.includes(selected) ? selected : currencies[0]
  const totals = new Map<string, number>()
  for (const record of records) {
    if (record.currency !== currency || !record.period) continue
    const month = record.period.slice(0, 7)
    totals.set(month, (totals.get(month) ?? 0) + (record.netPay ?? 0))
  }
  const data = [...totals.entries()]
    .sort(([first], [second]) => first.localeCompare(second))
    .slice(-6)
    .map(([name, value]) => ({ name, value }))
  return (
    <TrendChart
      title={title}
      description={`${description}${currency ? ` · ${currency}` : ''}`}
      data={data}
      label="Net payroll"
      currency={currency}
      isPending={isPending}
      isError={isError}
      retry={retry}
    >
      {currencies.length > 1 && (
        <Field className="max-w-40">
          <FieldLabel htmlFor={id}>Currency</FieldLabel>
          <NativeSelect
            id={id}
            value={currency}
            onChange={(event) => setSelected(event.target.value)}
          >
            {currencies.map((value) => (
              <NativeSelectOption key={value} value={value}>
                {value}
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>
      )}
    </TrendChart>
  )
}

export function PersonalPayrollTrend() {
  const query = useListMyPayrollRecords(
    {
      page: 0,
      size: 100,
      sort: ['period,desc', 'id,desc'],
    },
    { request: { paramsSerializer: { indexes: null } } }
  )
  return (
    <PayrollAmountChart
      title="My net payroll"
      description="Latest six recorded months within your latest 100 approved or paid records"
      records={query.data?.content ?? []}
      isPending={query.isPending}
      isError={query.isError}
      retry={() => void query.refetch()}
    />
  )
}

export function EmployeeScheduleTrend({ today }: { today: string }) {
  const profile = useGetMyProfile1()
  const lastDay = offsetDay(today, 6)
  const shifts = useShifts(
    profile.data?.id ?? '',
    { from: today, to: lastDay },
    { query: { enabled: !!profile.data?.id } }
  )
  return (
    <TrendChart
      title="My upcoming shifts"
      description="Assigned shifts per day, today and the next six days"
      data={daySeries(
        lastDay,
        shifts.data?.map((shift) => shift.shiftDate) ?? []
      )}
      label="Shifts"
      isPending={
        profile.isPending ||
        (!profile.isError && !!profile.data?.id && shifts.isPending)
      }
      isError={profile.isError || shifts.isError}
      retry={() => {
        if (profile.isError) void profile.refetch()
        else void shifts.refetch()
      }}
    />
  )
}

export function DoctorAvailabilityChart({
  date,
  practitionerId,
  profilePending,
  profileError,
  retryProfile,
}: {
  date: string
  practitionerId?: string
  profilePending: boolean
  profileError: boolean
  retryProfile: () => void
}) {
  const slots = useGetAvailableSlots(
    { practitionerId, date },
    { query: { enabled: !!practitionerId } }
  )
  return (
    <ChartCard
      title="My availability today"
      description="Available and booked slots in your configured schedule"
      values={
        slots.data?.map((slot) =>
          slot.isAvailable ? 'Available' : 'Booked'
        ) ?? []
      }
      isPending={
        profilePending || (!profileError && !!practitionerId && slots.isPending)
      }
      isError={profileError || slots.isError}
      retry={() => {
        if (profileError) retryProfile()
        else void slots.refetch()
      }}
    />
  )
}

export function HrPayrollCharts({
  organizationId,
}: {
  organizationId: string
}) {
  const query = useListPayrollRecords(
    {
      organizationId,
      page: 0,
      size: 100,
      sort: ['period,desc', 'id,desc'],
    },
    { request: { paramsSerializer: { indexes: null } } }
  )
  return (
    <>
      <ChartCard
        title="Payroll progress"
        description="Draft, approved, and paid states in this organization's latest 100 payroll records"
        values={query.data?.content?.map((record) => record.status) ?? []}
        isPending={query.isPending}
        isError={query.isError}
        retry={() => void query.refetch()}
      />
      <PayrollAmountChart
        title="Net payroll by month"
        description="Latest six recorded months within the latest 100 records, including drafts"
        records={query.data?.content ?? []}
        isPending={query.isPending}
        isError={query.isError}
        retry={() => void query.refetch()}
      />
    </>
  )
}

export function BillerCharts({ today }: { today: string }) {
  const invoices = useListInvoices(
    {
      page: 0,
      size: 100,
      sort: ['issuedAt,desc', 'id,desc'],
    },
    { request: { paramsSerializer: { indexes: null } } }
  )
  const unpaid = useUnpaidInvoices()
  return (
    <>
      <ChartCard
        title="Invoice status"
        description="Statuses in the latest 100 invoices"
        values={invoices.data?.content?.map((invoice) => invoice.status) ?? []}
        isPending={invoices.isPending}
        isError={invoices.isError}
        retry={() => void invoices.refetch()}
      />
      <ChartCard
        title="Unpaid invoice ageing"
        description="All outstanding invoices grouped by their recorded due date"
        values={
          unpaid.data?.map((invoice) =>
            !invoice.dueDate
              ? 'No due date'
              : invoice.dueDate.slice(0, 10) < today
                ? 'Overdue'
                : invoice.dueDate.slice(0, 10) === today
                  ? 'Due today'
                  : 'Upcoming'
          ) ?? []
        }
        isPending={unpaid.isPending}
        isError={unpaid.isError}
        retry={() => void unpaid.refetch()}
      />
    </>
  )
}
