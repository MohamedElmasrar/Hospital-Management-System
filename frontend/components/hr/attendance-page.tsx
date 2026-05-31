'use client'

import {
  useGetMyProfile1,
  useSearchEmployees,
} from '@/app/api/generated/hr/employee-controller/employee-controller'
import {
  getListAttendanceQueryKey,
  useAttendance,
  useListAttendance,
} from '@/app/api/generated/hr/workforce-controller/workforce-controller'
import type { AttendanceResponse } from '@/app/api/generated/hr/model'
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

function AttendanceTable({ records }: { records: AttendanceResponse[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Date</TableHead>
          <TableHead>Employee</TableHead>
          <TableHead>Check in</TableHead>
          <TableHead>Check out</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {records.length ? (
          records.map((record) => (
            <TableRow key={record.id}>
              <TableCell>{record.attendanceDate}</TableCell>
              <TableCell className="font-medium">
                {record.employeeName || record.employeeId}
              </TableCell>
              <TableCell>
                {record.checkedInAt
                  ? new Date(record.checkedInAt).toLocaleTimeString('en', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })
                  : '—'}
              </TableCell>
              <TableCell>
                {record.checkedOutAt
                  ? new Date(record.checkedOutAt).toLocaleTimeString('en', {
                      hour: '2-digit',
                      minute: '2-digit',
                    })
                  : '—'}
              </TableCell>
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              colSpan={4}
              className="h-24 text-center text-muted-foreground"
            >
              No attendance records in this date range.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}

function RecordAttendance({
  organizationId,
  onClose,
}: {
  organizationId: string
  onClose: () => void
}) {
  const [search, setSearch] = useState('')
  const employees = useSearchEmployees(
    { organizationId, search: search || undefined, page: 0, size: 50 },
    { query: { enabled: !!organizationId } }
  )
  const mutation = useAttendance()
  const queryClient = useQueryClient()
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const action = String(form.get('action'))
    await mutation.mutateAsync({
      data: {
        employeeId: String(form.get('employeeId')),
        attendanceDate: String(form.get('date')),
        checkIn: action === 'in',
        checkOut: action === 'out',
      },
    })
    await queryClient.invalidateQueries({
      queryKey: getListAttendanceQueryKey(),
    })
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
          <FieldLabel htmlFor="attendance-search">Find employee</FieldLabel>
          <Input
            id="attendance-search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Name or identifier"
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="attendance-employee">Employee</FieldLabel>
          <NativeSelect id="attendance-employee" name="employeeId" required>
            {(employees.data?.content || []).map((item) => (
              <NativeSelectOption key={item.id} value={item.id}>
                {item.givenName} {item.familyName} ({item.identifier})
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>
        <Field>
          <FieldLabel htmlFor="attendance-date">Date</FieldLabel>
          <Input id="attendance-date" name="date" type="date" required />
        </Field>
        <Field>
          <FieldLabel htmlFor="attendance-action">Action</FieldLabel>
          <NativeSelect id="attendance-action" name="action">
            <NativeSelectOption value="in">Check in</NativeSelectOption>
            <NativeSelectOption value="out">Check out</NativeSelectOption>
          </NativeSelect>
        </Field>
      </FieldGroup>
      <FormError error={mutation.error} />
      <Button
        type="submit"
        disabled={mutation.isPending || !employees.data?.content?.length}
      >
        Save attendance
      </Button>
    </form>
  )
}

function HrAttendance() {
  const organizations = useOrganization()
  const range = useDateRange()
  const [page, setPage] = useState(0)
  const [recording, setRecording] = useState(false)
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
  const query = useListAttendance(
    {
      organizationId: organizations.organizationId,
      employeeId: employeeId || undefined,
      from: range.from,
      to: range.to,
      page,
      size: PAGE_SIZE,
      sort: ['attendanceDate,desc'],
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
        title="Attendance"
        description="Review check-ins and check-outs and record attendance."
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
          <ListCard title="Attendance records">
            <div className="flex flex-wrap items-end gap-3">
              <Field>
                <FieldLabel htmlFor="attendance-from">From</FieldLabel>
                <Input
                  id="attendance-from"
                  type="date"
                  value={range.from}
                  onChange={(event) => {
                    range.setFrom(event.target.value)
                    setPage(0)
                  }}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="attendance-to">To</FieldLabel>
                <Input
                  id="attendance-to"
                  type="date"
                  value={range.to}
                  onChange={(event) => {
                    range.setTo(event.target.value)
                    setPage(0)
                  }}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="attendance-find">Find employee</FieldLabel>
                <Input
                  id="attendance-find"
                  value={employeeSearch}
                  onChange={(event) => setEmployeeSearch(event.target.value)}
                  placeholder="Name or identifier"
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="attendance-filter">Employee</FieldLabel>
                <NativeSelect
                  id="attendance-filter"
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
              <Button className="ml-auto" onClick={() => setRecording(true)}>
                Record attendance
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
              <AttendanceTable records={query.data?.content || []} />
            )}
            <Pagination
              page={page}
              totalPages={query.data?.totalPages || 0}
              onChange={setPage}
            />
          </ListCard>
        </>
      )}
      <Dialog open={recording} onOpenChange={setRecording}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Record attendance</DialogTitle>
            <DialogDescription>
              Choose an employee, date, and action.
            </DialogDescription>
          </DialogHeader>
          <RecordAttendance
            organizationId={organizations.organizationId}
            onClose={() => setRecording(false)}
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}

function MyAttendance() {
  const profile = useGetMyProfile1()
  const range = useDateRange()
  const [page, setPage] = useState(0)
  const query = useListAttendance(
    { from: range.from, to: range.to, page, size: PAGE_SIZE },
    {
      query: {
        enabled: !!profile.data?.id && !!range.from && !!range.to,
        placeholderData: keepPreviousData,
      },
    }
  )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="My Attendance"
        description="Your check-in and check-out records."
      />
      <ListCard title="My records">
        <div className="flex flex-wrap gap-3">
          <Field>
            <FieldLabel htmlFor="my-attendance-from">From</FieldLabel>
            <Input
              id="my-attendance-from"
              type="date"
              value={range.from}
              onChange={(event) => {
                range.setFrom(event.target.value)
                setPage(0)
              }}
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="my-attendance-to">To</FieldLabel>
            <Input
              id="my-attendance-to"
              type="date"
              value={range.to}
              onChange={(event) => {
                range.setTo(event.target.value)
                setPage(0)
              }}
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
          <AttendanceTable records={query.data?.content || []} />
        )}
        <Pagination
          page={page}
          totalPages={query.data?.totalPages || 0}
          onChange={setPage}
        />
      </ListCard>
    </div>
  )
}

export function AttendancePage() {
  const role = useRole()
  return role === 'HR_HEAD' || role === 'ADMIN' ? (
    <HrAttendance />
  ) : role === 'EMPLOYEE' ? (
    <MyAttendance />
  ) : (
    <PageHeading
      title="Attendance"
      description="This page is available to employees and HR staff."
    />
  )
}
