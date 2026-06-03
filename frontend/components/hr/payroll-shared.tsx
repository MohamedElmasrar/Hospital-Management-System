'use client'

import { useId, useState, type ReactNode } from 'react'
import Link from 'next/link'
import { isAxiosError } from 'axios'
import { useSearchEmployees } from '@/app/api/generated/hr/employee-controller/employee-controller'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Empty, EmptyHeader, EmptyTitle } from '@/components/ui/empty'
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  LoadingRows,
  LoadError,
  OrganizationField,
  PageHeading,
  Pagination,
  useOrganization,
} from './shared'

export function payrollMoney(value?: number, currency = 'MAD') {
  return new Intl.NumberFormat('en', { style: 'currency', currency }).format(
    value ?? 0
  )
}

export function PayrollError({ error }: { error: unknown }) {
  if (!error) return null
  const body = isAxiosError(error) ? error.response?.data : null
  const message =
    typeof body?.error === 'string'
      ? body.error
      : 'Could not save payroll. Check the values and try again.'
  return (
    <Alert variant="destructive">
      <AlertDescription>{message}</AlertDescription>
    </Alert>
  )
}

export function PayrollOrganization({
  settings = false,
  children,
}: {
  settings?: boolean
  children: (id: string) => ReactNode
}) {
  const organization = useOrganization()
  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <PageHeading
          title={settings ? 'Payroll settings' : 'Payroll'}
          description={
            settings
              ? 'Configure payroll currency, pay day, and employee salaries.'
              : 'Prepare monthly payroll, review amounts, and record payments.'
          }
        />
        <Button
          variant="outline"
          nativeButton={false}
          render={
            <Link href={settings ? '/hr/payroll' : '/hr/payroll/settings'} />
          }
        >
          {settings ? 'Back to payroll' : 'Payroll settings'}
        </Button>
      </div>
      {organization.isPending ? (
        <LoadingRows />
      ) : organization.isError ? (
        <LoadError
          onRetry={() => {
            void organization.refetch()
          }}
        />
      ) : !organization.data?.length ? (
        <Empty>
          <EmptyHeader>
            <EmptyTitle>No assigned organizations</EmptyTitle>
          </EmptyHeader>
        </Empty>
      ) : (
        <>
          <OrganizationField
            value={organization.organizationId}
            options={organization.data}
            onChange={organization.setSelected}
          />
          {children(organization.organizationId)}
        </>
      )}
    </div>
  )
}

export function PayrollEmployeePicker({
  organizationId,
  value,
  onChange,
}: {
  organizationId: string
  value: string
  onChange: (id: string) => void
}) {
  const id = useId()
  const [search, setSearch] = useState('')
  const [selectedLabel, setSelectedLabel] = useState('')
  const [page, setPage] = useState(0)
  const query = useSearchEmployees({
    organizationId,
    search: search || undefined,
    page,
    size: 5,
  })
  return (
    <div className="flex flex-col gap-3">
      {value && selectedLabel && (
        <p className="text-sm">Selected employee: {selectedLabel}</p>
      )}
      <Field>
        <FieldLabel htmlFor={id}>Find employee</FieldLabel>
        <Input
          id={id}
          value={search}
          placeholder="Name or identifier"
          onChange={(event) => {
            setSearch(event.target.value)
            setPage(0)
          }}
        />
      </Field>
      {query.isPending ? (
        <LoadingRows />
      ) : query.isError ? (
        <LoadError
          onRetry={() => {
            void query.refetch()
          }}
        />
      ) : !query.data?.content?.length ? (
        <Empty>
          <EmptyHeader>
            <EmptyTitle>No matching employees</EmptyTitle>
          </EmptyHeader>
        </Empty>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Employee</TableHead>
              <TableHead>Identifier</TableHead>
              <TableHead>
                <span className="sr-only">Select employee</span>
              </TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {query.data.content.map((employee) => (
              <TableRow key={employee.id}>
                <TableCell>
                  {employee.givenName} {employee.familyName}
                </TableCell>
                <TableCell>{employee.identifier}</TableCell>
                <TableCell>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      setSelectedLabel(
                        `${employee.givenName} ${employee.familyName}`
                      )
                      onChange(employee.id!)
                    }}
                    disabled={value === employee.id}
                  >
                    {value === employee.id ? 'Selected' : 'Select'}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      <Pagination
        page={page}
        totalPages={query.data?.totalPages ?? 0}
        onChange={setPage}
      />
    </div>
  )
}
