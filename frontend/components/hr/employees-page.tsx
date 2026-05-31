'use client'

import {
  getSearchEmployeesQueryKey,
  useSearchEmployees,
  useCreateAssignedEmployee,
  useGetEmployee,
  useUpdateEmployee,
  useAssignToDepartment,
  getGetEmployeeQueryKey,
} from '@/app/api/generated/hr/employee-controller/employee-controller'
import { useListDepartments } from '@/app/api/generated/hr/department-controller/department-controller'
import type {
  EmployeeResponseDTO,
  SearchEmployeesStatus,
  UpdateEmployeeRequestDTOEmploymentStatus,
} from '@/app/api/generated/hr/model'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
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
import { useQueryClient, keepPreviousData } from '@tanstack/react-query'
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
  formatDate,
  useOrganization,
  useRole,
} from './shared'

const statuses = ['Active', 'OnLeave', 'Suspended', 'Terminated'] as const

function EmployeeEditor({
  employee,
  organizationId,
  onClose,
}: {
  employee?: EmployeeResponseDTO
  organizationId: string
  onClose: () => void
}) {
  const queryClient = useQueryClient()
  const departments = useListDepartments(
    { organizationId, page: 0, size: 200 },
    { query: { enabled: !!organizationId } }
  )
  const create = useCreateAssignedEmployee()
  const update = useUpdateEmployee()
  const busy = create.isPending || update.isPending
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const givenName = String(form.get('givenName') || '').trim()
    const familyName = String(form.get('familyName') || '').trim()
    const employmentStatus = String(
      form.get('status') || 'Active'
    ) as UpdateEmployeeRequestDTOEmploymentStatus
    if (employee?.id) {
      await update.mutateAsync({
        id: employee.id,
        data: { givenName, familyName, employmentStatus },
      })
      await queryClient.invalidateQueries({
        queryKey: getGetEmployeeQueryKey(employee.id),
      })
    } else {
      await create.mutateAsync({
        data: {
          employee: {
            identifier: String(form.get('identifier') || '').trim(),
            givenName,
            familyName,
            employmentStatus,
          },
          assignment: {
            departmentId: String(form.get('departmentId')),
            jobTitle: String(form.get('jobTitle') || '').trim(),
            startDate: String(form.get('startDate')),
          },
        },
      })
    }
    await queryClient.invalidateQueries({
      queryKey: getSearchEmployeesQueryKey(),
    })
    onClose()
  }
  return (
    <form
      onSubmit={(event) => {
        void submit(event).catch(() => {})
      }}
      className="flex flex-col gap-4"
    >
      <FieldGroup>
        {!employee && (
          <Field>
            <FieldLabel htmlFor="employee-identifier">Identifier</FieldLabel>
            <Input id="employee-identifier" name="identifier" required />
          </Field>
        )}
        <Field>
          <FieldLabel htmlFor="employee-given">Given name</FieldLabel>
          <Input
            id="employee-given"
            name="givenName"
            defaultValue={employee?.givenName}
            required
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="employee-family">Family name</FieldLabel>
          <Input
            id="employee-family"
            name="familyName"
            defaultValue={employee?.familyName}
            required
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="employee-status">Status</FieldLabel>
          <NativeSelect
            id="employee-status"
            name="status"
            defaultValue={employee?.employmentStatus || 'Active'}
          >
            {statuses.map((status) => (
              <NativeSelectOption key={status} value={status}>
                {status}
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>
        {!employee && (
          <>
            <Field>
              <FieldLabel htmlFor="employee-department">
                Initial department
              </FieldLabel>
              <NativeSelect
                id="employee-department"
                name="departmentId"
                required
              >
                {(departments.data?.content || []).map((department) => (
                  <NativeSelectOption key={department.id} value={department.id}>
                    {department.name}
                  </NativeSelectOption>
                ))}
              </NativeSelect>
            </Field>
            <Field>
              <FieldLabel htmlFor="employee-job">Job title</FieldLabel>
              <Input id="employee-job" name="jobTitle" required />
            </Field>
            <Field>
              <FieldLabel htmlFor="employee-start">Start date</FieldLabel>
              <Input
                id="employee-start"
                name="startDate"
                type="date"
                required
              />
            </Field>
          </>
        )}
      </FieldGroup>
      <FormError error={create.error || update.error} />
      <Button
        disabled={busy || (!employee && !departments.data?.content?.length)}
        type="submit"
      >
        {busy ? 'Saving…' : 'Save employee'}
      </Button>
    </form>
  )
}

function EmployeeDetails({
  employee,
  organizationId,
  onClose,
}: {
  employee: EmployeeResponseDTO
  organizationId: string
  onClose: () => void
}) {
  const details = useGetEmployee(employee.id || '', {
    query: { enabled: !!employee.id },
  })
  const departments = useListDepartments(
    { organizationId, page: 0, size: 200 },
    { query: { enabled: !!organizationId } }
  )
  const assign = useAssignToDepartment()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await assign.mutateAsync({
      id: employee.id!,
      data: {
        departmentId: String(form.get('departmentId')),
        jobTitle: String(form.get('jobTitle')),
        startDate: String(form.get('startDate')),
      },
    })
    await queryClient.invalidateQueries({
      queryKey: getGetEmployeeQueryKey(employee.id),
    })
    event.currentTarget.reset()
  }
  if (editing)
    return (
      <EmployeeEditor
        employee={employee}
        organizationId={organizationId}
        onClose={() => {
          setEditing(false)
          onClose()
        }}
      />
    )
  return (
    <div className="flex flex-col gap-5">
      {details.isPending ? (
        <LoadingRows />
      ) : details.isError ? (
        <LoadError
          onRetry={() => {
            void details.refetch()
          }}
        />
      ) : (
        <>
          <div className="text-sm">
            <p>Identifier: {details.data?.identifier || '—'}</p>
            <p>Status: {details.data?.employmentStatus || '—'}</p>
            <p>Created: {formatDate(details.data?.dateCreated)}</p>
          </div>
          <div>
            <p className="mb-2 font-medium">Assignments</p>
            {details.data?.assignments?.length ? (
              details.data.assignments.map((item) => (
                <p className="text-sm text-muted-foreground" key={item.id}>
                  {item.departmentName} · {item.jobTitle} ·{' '}
                  {formatDate(item.startDate)}
                </p>
              ))
            ) : (
              <p className="text-sm text-muted-foreground">No assignments.</p>
            )}
          </div>
        </>
      )}
      <Button variant="outline" onClick={() => setEditing(true)}>
        Edit employee
      </Button>
      <form
        className="flex flex-col gap-3 border-t pt-4"
        onSubmit={(event) => {
          void submit(event).catch(() => {})
        }}
      >
        <p className="font-medium">Add department assignment</p>
        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="assignment-department">Department</FieldLabel>
            <NativeSelect
              id="assignment-department"
              name="departmentId"
              required
            >
              {(departments.data?.content || []).map((department) => (
                <NativeSelectOption key={department.id} value={department.id}>
                  {department.name}
                </NativeSelectOption>
              ))}
            </NativeSelect>
          </Field>
          <Field>
            <FieldLabel htmlFor="assignment-job">Job title</FieldLabel>
            <Input id="assignment-job" name="jobTitle" required />
          </Field>
          <Field>
            <FieldLabel htmlFor="assignment-date">Start date</FieldLabel>
            <Input id="assignment-date" name="startDate" type="date" required />
          </Field>
        </FieldGroup>
        <FormError error={assign.error} />
        <Button
          type="submit"
          disabled={assign.isPending || !departments.data?.content?.length}
        >
          Assign
        </Button>
      </form>
    </div>
  )
}

export function EmployeesPage() {
  const role = useRole()
  const organizations = useOrganization(role === 'HR_HEAD' || role === 'ADMIN')
  const [page, setPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState<SearchEmployeesStatus | ''>('')
  const [selected, setSelected] = useState<EmployeeResponseDTO | null>(null)
  const [creating, setCreating] = useState(false)
  const query = useSearchEmployees(
    {
      organizationId: organizations.organizationId || undefined,
      search: search || undefined,
      status: status || undefined,
      page,
      size: PAGE_SIZE,
      sort: ['familyName,asc'],
    },
    {
      query: {
        enabled: !!organizations.organizationId,
        placeholderData: keepPreviousData,
      },
    }
  )
  if (role !== 'HR_HEAD' && role !== 'ADMIN')
    return (
      <PageHeading
        title="Employees"
        description="This page is available to HR staff."
      />
    )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Employees"
        description="Manage employee records and department assignments."
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
            onChange={(value) => {
              organizations.setSelected(value)
              setPage(0)
            }}
            options={organizations.data}
          />
          <ListCard
            title="Employee directory"
            description={`${query.data?.totalElements || 0} employees`}
          >
            <div className="flex flex-wrap items-end gap-3">
              <form
                className="flex gap-2"
                onSubmit={(event) => {
                  event.preventDefault()
                  setSearch(searchInput.trim())
                  setPage(0)
                }}
              >
                <Field>
                  <FieldLabel htmlFor="employee-search">
                    Search employees
                  </FieldLabel>
                  <Input
                    id="employee-search"
                    value={searchInput}
                    onChange={(event) => setSearchInput(event.target.value)}
                    placeholder="Name or identifier"
                  />
                </Field>
                <Button type="submit" className="self-end">
                  Search
                </Button>
              </form>
              <Field>
                <FieldLabel htmlFor="employee-filter">Status</FieldLabel>
                <NativeSelect
                  id="employee-filter"
                  value={status}
                  onChange={(event) => {
                    setStatus(event.target.value as SearchEmployeesStatus | '')
                    setPage(0)
                  }}
                >
                  <NativeSelectOption value="">All statuses</NativeSelectOption>
                  {statuses.map((item) => (
                    <NativeSelectOption key={item} value={item}>
                      {item}
                    </NativeSelectOption>
                  ))}
                </NativeSelect>
              </Field>
              <Button className="ml-auto" onClick={() => setCreating(true)}>
                Add employee
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
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Identifier</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {query.data?.content?.length ? (
                    query.data.content.map((employee) => (
                      <TableRow key={employee.id}>
                        <TableCell className="font-medium">
                          {employee.givenName} {employee.familyName}
                        </TableCell>
                        <TableCell>{employee.identifier}</TableCell>
                        <TableCell>
                          <Badge variant="secondary">
                            {employee.employmentStatus}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {formatDate(employee.dateCreated)}
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setSelected(employee)}
                          >
                            View
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={5}
                        className="h-24 text-center text-muted-foreground"
                      >
                        No employees found.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
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
            <DialogTitle>Add employee</DialogTitle>
            <DialogDescription>
              Create a record with its first department assignment.
            </DialogDescription>
          </DialogHeader>
          <EmployeeEditor
            organizationId={organizations.organizationId}
            onClose={() => setCreating(false)}
          />
        </DialogContent>
      </Dialog>
      <Dialog
        open={!!selected}
        onOpenChange={(open) => {
          if (!open) setSelected(null)
        }}
      >
        <DialogContent className="max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {selected?.givenName} {selected?.familyName}
            </DialogTitle>
            <DialogDescription>
              Employee details and assignments
            </DialogDescription>
          </DialogHeader>
          {selected && (
            <EmployeeDetails
              employee={selected}
              organizationId={organizations.organizationId}
              onClose={() => setSelected(null)}
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
