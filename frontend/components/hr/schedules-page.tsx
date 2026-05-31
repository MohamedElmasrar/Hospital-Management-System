'use client'

import {
  useGetMyProfile1,
  useSearchEmployees,
} from '@/app/api/generated/hr/employee-controller/employee-controller'
import { useListDepartments } from '@/app/api/generated/hr/department-controller/department-controller'
import {
  getListShiftsQueryKey,
  useCreateShift,
  useListShifts,
  useShifts,
} from '@/app/api/generated/hr/workforce-controller/workforce-controller'
import type { ShiftResponse } from '@/app/api/generated/hr/model'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
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
import { keepPreviousData, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import {
  FormError,
  ListCard,
  LoadingRows,
  LoadError,
  OrganizationField,
  PageHeading,
  Pagination,
  PAGE_SIZE,
  useDateRange,
  useOrganization,
  useRole,
} from './shared'

function time(value?: string) {
  return value ? value.slice(0, 5) : '—'
}

function ShiftTable({ shifts }: { shifts: ShiftResponse[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Date</TableHead>
          <TableHead>Employee</TableHead>
          <TableHead>Department</TableHead>
          <TableHead>Hours</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {shifts.length ? (
          shifts.map((shift) => (
            <TableRow key={shift.id}>
              <TableCell>{shift.shiftDate}</TableCell>
              <TableCell className="font-medium">
                {shift.employeeName || shift.employeeId}
              </TableCell>
              <TableCell>{shift.departmentName || '—'}</TableCell>
              <TableCell>
                {time(shift.startsAt)}–{time(shift.endsAt)}
              </TableCell>
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              colSpan={4}
              className="h-24 text-center text-muted-foreground"
            >
              No shifts in this date range.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}

function CreateShift({
  organizationId,
  onClose,
}: {
  organizationId: string
  onClose: () => void
}) {
  const [employeeSearch, setEmployeeSearch] = useState('')
  const employees = useSearchEmployees(
    { organizationId, search: employeeSearch || undefined, page: 0, size: 50 },
    { query: { enabled: !!organizationId } }
  )
  const departments = useListDepartments(
    { organizationId, page: 0, size: 200 },
    { query: { enabled: !!organizationId } }
  )
  const create = useCreateShift()
  const queryClient = useQueryClient()
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await create.mutateAsync({
      data: {
        employeeId: String(form.get('employeeId')),
        departmentId: String(form.get('departmentId')),
        shiftDate: String(form.get('date')),
        startsAt: String(form.get('start')),
        endsAt: String(form.get('end')),
      },
    })
    await queryClient.invalidateQueries({ queryKey: getListShiftsQueryKey() })
    onClose()
  }
  return (
    <form
      className="flex flex-col gap-4"
      onSubmit={(event) => {
        void submit(event).catch(() => {})
      }}
    >
      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="shift-search">Find employee</FieldLabel>
          <Input
            id="shift-search"
            value={employeeSearch}
            onChange={(event) => setEmployeeSearch(event.target.value)}
            placeholder="Name or identifier"
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="shift-employee">Employee</FieldLabel>
          <NativeSelect id="shift-employee" name="employeeId" required>
            {(employees.data?.content || []).map((item) => (
              <NativeSelectOption key={item.id} value={item.id}>
                {item.givenName} {item.familyName} ({item.identifier})
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>
        <Field>
          <FieldLabel htmlFor="shift-department">Department</FieldLabel>
          <NativeSelect id="shift-department" name="departmentId" required>
            {(departments.data?.content || []).map((item) => (
              <NativeSelectOption key={item.id} value={item.id}>
                {item.name}
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>
        <Field>
          <FieldLabel htmlFor="shift-date">Date</FieldLabel>
          <Input id="shift-date" type="date" name="date" required />
        </Field>
        <Field>
          <FieldLabel htmlFor="shift-start">Start time</FieldLabel>
          <Input id="shift-start" type="time" name="start" required />
        </Field>
        <Field>
          <FieldLabel htmlFor="shift-end">End time</FieldLabel>
          <Input id="shift-end" type="time" name="end" required />
        </Field>
      </FieldGroup>
      <FormError error={create.error} />
      <Button
        type="submit"
        disabled={
          create.isPending ||
          !employees.data?.content?.length ||
          !departments.data?.content?.length
        }
      >
        Create shift
      </Button>
    </form>
  )
}

function HrSchedules() {
  const organizations = useOrganization()
  const range = useDateRange()
  const [page, setPage] = useState(0)
  const [creating, setCreating] = useState(false)
  const [employeeSearch, setEmployeeSearch] = useState('')
  const employees = useSearchEmployees(
    {
      organizationId: organizations.organizationId,
      search: employeeSearch || undefined,
      page: 0,
      size: 50,
    },
    { query: { enabled: !!organizations.organizationId } }
  )
  const [employeeId, setEmployeeId] = useState('')
  const query = useListShifts(
    {
      organizationId: organizations.organizationId,
      employeeId: employeeId || undefined,
      from: range.from,
      to: range.to,
      page,
      size: PAGE_SIZE,
      sort: ['shiftDate,asc'],
    },
    {
      query: {
        enabled: !!organizations.organizationId && !!range.from && !!range.to,
        placeholderData: keepPreviousData,
      },
    }
  )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Schedules"
        description="Review shifts and assign new shifts."
      />
      {organizations.isPending ? (
        <LoadingRows />
      ) : organizations.isError ? (
        <LoadError
          onRetry={() => {
            void organizations.refetch()
          }}
        />
      ) : !organizations.data?.length ? (
        <p className="text-sm text-muted-foreground">
          No assigned organizations.
        </p>
      ) : (
        <>
          <OrganizationField
            value={organizations.organizationId}
            options={organizations.data}
            onChange={(value) => {
              organizations.setSelected(value)
              setPage(0)
              setEmployeeId('')
            }}
          />
          <ListCard title="Shifts">
            <div className="flex flex-wrap items-end gap-3">
              <Field>
                <FieldLabel htmlFor="schedule-from">From</FieldLabel>
                <Input
                  id="schedule-from"
                  type="date"
                  value={range.from}
                  onChange={(event) => {
                    range.setFrom(event.target.value)
                    setPage(0)
                  }}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="schedule-to">To</FieldLabel>
                <Input
                  id="schedule-to"
                  type="date"
                  value={range.to}
                  onChange={(event) => {
                    range.setTo(event.target.value)
                    setPage(0)
                  }}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="schedule-find">Find employee</FieldLabel>
                <Input
                  id="schedule-find"
                  value={employeeSearch}
                  onChange={(event) => setEmployeeSearch(event.target.value)}
                  placeholder="Name or identifier"
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="schedule-employee">Employee</FieldLabel>
                <NativeSelect
                  id="schedule-employee"
                  value={employeeId}
                  onChange={(event) => {
                    setEmployeeId(event.target.value)
                    setPage(0)
                  }}
                >
                  <NativeSelectOption value="">
                    All employees
                  </NativeSelectOption>
                  {(employees.data?.content || []).map((item) => (
                    <NativeSelectOption key={item.id} value={item.id}>
                      {item.givenName} {item.familyName}
                    </NativeSelectOption>
                  ))}
                </NativeSelect>
              </Field>
              <Button className="ml-auto" onClick={() => setCreating(true)}>
                Add shift
              </Button>
            </div>
            {query.isPending ? (
              <LoadingRows />
            ) : query.isError ? (
              <LoadError
                onRetry={() => {
                  void query.refetch()
                }}
              />
            ) : (
              <ShiftTable shifts={query.data?.content || []} />
            )}
            <Pagination
              page={page}
              totalPages={query.data?.totalPages || 0}
              onChange={setPage}
            />
          </ListCard>
        </>
      )}
      <Dialog open={creating} onOpenChange={setCreating}>
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Add shift</DialogTitle>
            <DialogDescription>
              Assign an employee to a department shift.
            </DialogDescription>
          </DialogHeader>
          <CreateShift
            organizationId={organizations.organizationId}
            onClose={() => setCreating(false)}
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}

function MySchedules() {
  const profile = useGetMyProfile1()
  const range = useDateRange()
  const query = useShifts(
    profile.data?.id || '',
    { from: range.from, to: range.to },
    { query: { enabled: !!profile.data?.id && !!range.from && !!range.to } }
  )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="My Schedule"
        description="Your assigned shifts for the selected dates."
      />
      <ListCard title="My shifts">
        <div className="flex flex-wrap gap-3">
          <Field>
            <FieldLabel htmlFor="my-schedule-from">From</FieldLabel>
            <Input
              id="my-schedule-from"
              type="date"
              value={range.from}
              onChange={(event) => range.setFrom(event.target.value)}
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="my-schedule-to">To</FieldLabel>
            <Input
              id="my-schedule-to"
              type="date"
              value={range.to}
              onChange={(event) => range.setTo(event.target.value)}
            />
          </Field>
        </div>
        {profile.isPending || query.isPending ? (
          <LoadingRows />
        ) : profile.isError || query.isError ? (
          <LoadError
            onRetry={() => {
              void profile.refetch()
              void query.refetch()
            }}
          />
        ) : (
          <ShiftTable shifts={query.data || []} />
        )}
      </ListCard>
    </div>
  )
}

export function SchedulesPage() {
  const role = useRole()
  return role === 'HR_HEAD' || role === 'ADMIN' ? (
    <HrSchedules />
  ) : role === 'EMPLOYEE' ? (
    <MySchedules />
  ) : (
    <PageHeading
      title="Schedules"
      description="This page is available to employees and HR staff."
    />
  )
}
