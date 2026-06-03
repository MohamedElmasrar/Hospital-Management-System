'use client'

import { useState, type SubmitEvent } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import {
  getListMyPayrollRecordsQueryKey,
  getListPayrollRecordsQueryKey,
  useApprovePayrollRecord,
  useCreatePayrollRecord,
  useListMyPayrollRecords,
  useListPayrollRecords,
  useMarkPayrollRecordPaid,
  useRecalculatePayrollRecord,
} from '@/app/api/generated/hr/payroll/payroll'
import type {
  RecordResponse,
  ListPayrollRecordsStatus,
} from '@/app/api/generated/hr/model'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Empty, EmptyHeader, EmptyTitle } from '@/components/ui/empty'
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  ListCard,
  LoadingRows,
  LoadError,
  PageHeading,
  Pagination,
  PAGE_SIZE,
  useRole,
} from './shared'
import {
  PayrollEmployeePicker,
  PayrollError,
  payrollMoney,
  PayrollOrganization,
} from './payroll-shared'

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function PayrollTable({
  rows,
  onReview,
}: {
  rows: RecordResponse[]
  onReview: (id: string) => void
}) {
  if (!rows.length)
    return (
      <Empty>
        <EmptyHeader>
          <EmptyTitle>No payroll records found</EmptyTitle>
        </EmptyHeader>
      </Empty>
    )
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Employee</TableHead>
          <TableHead>Month</TableHead>
          <TableHead>Gross pay</TableHead>
          <TableHead>Deductions</TableHead>
          <TableHead>Net pay</TableHead>
          <TableHead>Status</TableHead>
          <TableHead>
            <span className="sr-only">Review</span>
          </TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((record) => (
          <TableRow key={record.id}>
            <TableCell className="font-medium">{record.employeeName}</TableCell>
            <TableCell>{record.period?.slice(0, 7)}</TableCell>
            <TableCell>
              {payrollMoney(record.grossPay, record.currency)}
            </TableCell>
            <TableCell>
              {payrollMoney(record.deductions, record.currency)}
            </TableCell>
            <TableCell>
              {payrollMoney(record.netPay, record.currency)}
            </TableCell>
            <TableCell>
              <Badge
                variant={record.status === 'PAID' ? 'secondary' : 'outline'}
              >
                {record.status}
              </Badge>
            </TableCell>
            <TableCell>
              <Button
                variant="outline"
                size="sm"
                onClick={() => record.id && onReview(record.id)}
              >
                View details
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}

function PayrollDetails({
  record,
  manager,
  refresh,
}: {
  record: RecordResponse
  manager: boolean
  refresh: () => Promise<void>
}) {
  const approve = useApprovePayrollRecord()
  const paid = useMarkPayrollRecordPaid()
  const recalculate = useRecalculatePayrollRecord()
  const [hours, setHours] = useState(String(record.overtimeHours ?? 0))
  const [extraDeductions, setExtraDeductions] = useState(
    String(record.extraDeductions ?? 0)
  )
  const [confirmPayment, setConfirmPayment] = useState(false)
  const busy = approve.isPending || paid.isPending || recalculate.isPending
  const dirty =
    !hours ||
    !extraDeductions ||
    Number(hours) !== (record.overtimeHours ?? 0) ||
    Number(extraDeductions) !== (record.extraDeductions ?? 0)
  async function save(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!record.id || !record.employeeId || !record.period) return
    await recalculate.mutateAsync({
      id: record.id,
      data: {
        employeeId: record.employeeId,
        month: record.period.slice(0, 7),
        overtimeHours: Number(hours),
        extraDeductions: Number(extraDeductions),
      },
    })
    await refresh()
  }
  return (
    <div className="flex flex-col gap-4">
      <dl className="grid grid-cols-2 gap-x-6 gap-y-2 text-sm">
        {(
          [
            ['Base salary', payrollMoney(record.baseSalary, record.currency)],
            ['Allowance', payrollMoney(record.allowance, record.currency)],
            ['Overtime hours', String(record.overtimeHours ?? 0)],
            [
              'Overtime hourly rate',
              payrollMoney(record.overtimeHourlyRate, record.currency),
            ],
            ['Overtime pay', payrollMoney(record.overtimePay, record.currency)],
            ['Gross pay', payrollMoney(record.grossPay, record.currency)],
            [
              'Total deductions',
              payrollMoney(record.deductions, record.currency),
            ],
            ['Net pay', payrollMoney(record.netPay, record.currency)],
            ['Pay date', record.payDate ?? '—'],
            ['Status', record.status ?? '—'],
          ] as const
        ).map(([label, value]) => (
          <div key={label}>
            <dt className="text-muted-foreground">{label}</dt>
            <dd className="font-medium">{value}</dd>
          </div>
        ))}
      </dl>
      {manager && record.status === 'DRAFT' && (
        <>
          <form
            className="flex flex-col gap-4"
            onSubmit={(event) => {
              void save(event).catch(() => {})
            }}
          >
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="draft-overtime">Overtime hours</FieldLabel>
                <Input
                  id="draft-overtime"
                  type="number"
                  min="0"
                  max="744"
                  step="0.01"
                  value={hours}
                  onChange={(event) => setHours(event.target.value)}
                  required
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="draft-extra-deductions">
                  Additional deductions
                </FieldLabel>
                <Input
                  id="draft-extra-deductions"
                  type="number"
                  min="0"
                  max="999999999.99"
                  step="0.01"
                  value={extraDeductions}
                  onChange={(event) => setExtraDeductions(event.target.value)}
                  required
                />
                <FieldDescription>
                  Added to the configured employee monthly deductions.
                </FieldDescription>
              </Field>
            </FieldGroup>
            <Button variant="outline" type="submit" disabled={busy}>
              Recalculate draft
            </Button>
          </form>
          {dirty && (
            <p className="text-sm text-muted-foreground">
              Recalculate the draft before approving your changes.
            </p>
          )}
          <Button
            disabled={busy || dirty}
            onClick={() =>
              record.id &&
              approve.mutate({ id: record.id }, { onSuccess: refresh })
            }
          >
            Approve payroll
          </Button>
        </>
      )}
      {manager &&
        record.status === 'APPROVED' &&
        (confirmPayment ? (
          <div className="flex flex-col gap-3">
            <p className="text-sm">
              Confirm that {payrollMoney(record.netPay, record.currency)} has
              been paid to {record.employeeName}. This records a completed
              payment.
            </p>
            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                disabled={busy}
                onClick={() => setConfirmPayment(false)}
              >
                Cancel
              </Button>
              <Button
                disabled={busy}
                onClick={() =>
                  record.id &&
                  paid.mutate({ id: record.id }, { onSuccess: refresh })
                }
              >
                Confirm payment
              </Button>
            </div>
          </div>
        ) : (
          <Button onClick={() => setConfirmPayment(true)}>Mark as paid</Button>
        ))}
      {record.paidAt && (
        <p className="text-sm text-muted-foreground">
          Payment recorded: {record.paidAt.replace('T', ' ').slice(0, 16)} UTC
        </p>
      )}
      <PayrollError error={recalculate.error || approve.error || paid.error} />
    </div>
  )
}

function CreatePayroll({
  organizationId,
  month,
  refresh,
  close,
}: {
  organizationId: string
  month: string
  refresh: () => Promise<void>
  close: () => void
}) {
  const [employeeId, setEmployeeId] = useState('')
  const create = useCreatePayrollRecord()
  async function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await create.mutateAsync({
      params: { organizationId },
      data: {
        employeeId,
        month: String(form.get('month')),
        overtimeHours: Number(form.get('hours')),
        extraDeductions: Number(form.get('deductions')),
      },
    })
    await refresh()
    close()
  }
  return (
    <div className="flex flex-col gap-4">
      <PayrollEmployeePicker
        organizationId={organizationId}
        value={employeeId}
        onChange={setEmployeeId}
      />
      <form
        className="flex flex-col gap-4"
        onSubmit={(event) => {
          void submit(event).catch(() => {})
        }}
      >
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="new-payroll-month">Month</FieldLabel>
            <Input
              id="new-payroll-month"
              name="month"
              type="month"
              defaultValue={month}
              required
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="new-payroll-hours">Overtime hours</FieldLabel>
            <Input
              id="new-payroll-hours"
              name="hours"
              type="number"
              min="0"
              max="744"
              step="0.01"
              defaultValue="0"
              required
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="new-payroll-deductions">
              Additional deductions
            </FieldLabel>
            <Input
              id="new-payroll-deductions"
              name="deductions"
              type="number"
              min="0"
              max="999999999.99"
              step="0.01"
              defaultValue="0"
              required
            />
            <FieldDescription>
              Salary, allowance, and recurring deductions come from payroll
              settings.
            </FieldDescription>
          </Field>
        </FieldGroup>
        <PayrollError error={create.error} />
        <Button type="submit" disabled={!employeeId || create.isPending}>
          Create draft
        </Button>
      </form>
    </div>
  )
}

function PayrollRecords({
  organizationId,
  manager,
}: {
  organizationId?: string
  manager: boolean
}) {
  const [month, setMonth] = useState(manager ? currentMonth : () => '')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [creating, setCreating] = useState(false)
  const [selectedId, setSelectedId] = useState('')
  const queryClient = useQueryClient()
  const managed = useListPayrollRecords(
    {
      organizationId: organizationId ?? '',
      month: month || undefined,
      status: (status || undefined) as ListPayrollRecordsStatus | undefined,
      page,
      size: PAGE_SIZE,
    },
    { query: { enabled: manager && !!organizationId } }
  )
  const own = useListMyPayrollRecords(
    { page, size: PAGE_SIZE },
    { query: { enabled: !manager } }
  )
  const query = manager ? managed : own
  const record = query.data?.content?.find((item) => item.id === selectedId)
  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: getListPayrollRecordsQueryKey(),
      }),
      queryClient.invalidateQueries({
        queryKey: getListMyPayrollRecordsQueryKey(),
      }),
    ])
  }
  return (
    <>
      <ListCard
        title={manager ? 'Monthly payroll' : 'My payroll'}
        description={
          manager
            ? `${query.data?.totalElements ?? 0} records. Review each draft before approval.`
            : 'View your approved payroll and recorded salary payments.'
        }
      >
        {manager && (
          <div className="flex flex-wrap items-end gap-4">
            <Field className="max-w-48">
              <FieldLabel htmlFor="payroll-month-filter">Month</FieldLabel>
              <Input
                id="payroll-month-filter"
                type="month"
                value={month}
                onChange={(event) => {
                  setMonth(event.target.value)
                  setPage(0)
                }}
              />
            </Field>
            <Field className="max-w-48">
              <FieldLabel htmlFor="payroll-status-filter">Status</FieldLabel>
              <NativeSelect
                id="payroll-status-filter"
                value={status}
                onChange={(event) => {
                  setStatus(event.target.value)
                  setPage(0)
                }}
              >
                <NativeSelectOption value="">All statuses</NativeSelectOption>
                {['DRAFT', 'APPROVED', 'PAID'].map((value) => (
                  <NativeSelectOption key={value} value={value}>
                    {value}
                  </NativeSelectOption>
                ))}
              </NativeSelect>
            </Field>
            <Button onClick={() => setCreating(true)}>Create payroll</Button>
          </div>
        )}
        {query.isPending ? (
          <LoadingRows />
        ) : query.isError ? (
          <LoadError
            onRetry={() => {
              void query.refetch()
            }}
          />
        ) : (
          <PayrollTable
            rows={query.data?.content ?? []}
            onReview={setSelectedId}
          />
        )}
        <Pagination
          page={page}
          totalPages={query.data?.totalPages ?? 0}
          onChange={setPage}
        />
      </ListCard>
      <Dialog
        open={!!record}
        onOpenChange={(open) => {
          if (!open) setSelectedId('')
        }}
      >
        <DialogContent className="max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Payroll details</DialogTitle>
            <DialogDescription>
              {record?.employeeName} · {record?.period?.slice(0, 7)}
            </DialogDescription>
          </DialogHeader>
          {record && (
            <PayrollDetails
              key={`${record.id}-${query.dataUpdatedAt}`}
              record={record}
              manager={manager}
              refresh={refresh}
            />
          )}
        </DialogContent>
      </Dialog>
      {manager && organizationId && (
        <Dialog open={creating} onOpenChange={setCreating}>
          <DialogContent className="max-h-[85vh] overflow-y-auto sm:max-w-2xl">
            <DialogHeader>
              <DialogTitle>Create payroll</DialogTitle>
              <DialogDescription>
                Choose an employee with salary settings, then prepare a monthly
                draft.
              </DialogDescription>
            </DialogHeader>
            {creating && (
              <CreatePayroll
                organizationId={organizationId}
                month={month || currentMonth()}
                refresh={refresh}
                close={() => setCreating(false)}
              />
            )}
          </DialogContent>
        </Dialog>
      )}
    </>
  )
}

export function PayrollPage() {
  const role = useRole()
  if (!role) return <LoadingRows />
  if (role === 'ADMIN' || role === 'HR_HEAD')
    return (
      <PayrollOrganization>
        {(organizationId) => (
          <PayrollRecords
            key={organizationId}
            organizationId={organizationId}
            manager
          />
        )}
      </PayrollOrganization>
    )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="My payroll"
        description="Your salary records and payment status."
      />
      <PayrollRecords manager={false} />
    </div>
  )
}
