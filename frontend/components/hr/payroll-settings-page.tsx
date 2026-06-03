'use client'

import { useState, type SubmitEvent } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import {
  getGetEmployeeCompensationQueryKey,
  getGetPayrollSettingsQueryKey,
  useGetEmployeeCompensation,
  useGetPayrollSettings,
  useSaveEmployeeCompensation,
  useSavePayrollSettings,
} from '@/app/api/generated/hr/payroll/payroll'
import type {
  SettingsResponse,
  CompensationResponse,
} from '@/app/api/generated/hr/model'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import {
  ListCard,
  LoadingRows,
  LoadError,
  PageHeading,
  useRole,
} from './shared'
import {
  PayrollEmployeePicker,
  PayrollError,
  PayrollOrganization,
} from './payroll-shared'

function SettingsForm({
  organizationId,
  settings,
}: {
  organizationId: string
  settings: SettingsResponse
}) {
  const save = useSavePayrollSettings()
  const client = useQueryClient()
  const [saved, setSaved] = useState(false)
  async function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await save.mutateAsync({
      params: { organizationId },
      data: {
        currency: String(form.get('currency')),
        payDay: Number(form.get('payDay')),
        overtimeHourlyRate: Number(form.get('overtimeRate')),
      },
    })
    await client.invalidateQueries({
      queryKey: getGetPayrollSettingsQueryKey({ organizationId }),
    })
    setSaved(true)
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
          <FieldLabel htmlFor="payroll-currency">Currency</FieldLabel>
          <NativeSelect
            id="payroll-currency"
            name="currency"
            defaultValue={settings.currency}
          >
            {Array.from(
              new Set([settings.currency ?? 'MAD', 'MAD', 'EUR', 'USD', 'GBP'])
            ).map((currency) => (
              <NativeSelectOption key={currency} value={currency}>
                {currency}
              </NativeSelectOption>
            ))}
          </NativeSelect>
        </Field>
        <Field>
          <FieldLabel htmlFor="payroll-pay-day">Monthly pay day</FieldLabel>
          <Input
            id="payroll-pay-day"
            name="payDay"
            type="number"
            min={1}
            max={28}
            defaultValue={settings.payDay}
            required
          />
          <FieldDescription>
            Day 1–28, available in every month.
          </FieldDescription>
        </Field>
        <Field>
          <FieldLabel htmlFor="payroll-overtime-rate">
            Overtime rate per hour
          </FieldLabel>
          <Input
            id="payroll-overtime-rate"
            name="overtimeRate"
            type="number"
            min="0"
            max="999999999.99"
            step="0.01"
            defaultValue={settings.overtimeHourlyRate}
            required
          />
        </Field>
      </FieldGroup>
      <PayrollError error={save.error} />
      {saved && (
        <Alert>
          <AlertDescription>Payroll settings saved.</AlertDescription>
        </Alert>
      )}
      <Button type="submit" disabled={save.isPending}>
        Save payroll settings
      </Button>
    </form>
  )
}

function CompensationForm({
  organizationId,
  employeeId,
  value,
}: {
  organizationId: string
  employeeId: string
  value: CompensationResponse
}) {
  const save = useSaveEmployeeCompensation()
  const client = useQueryClient()
  async function submit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await save.mutateAsync({
      employeeId,
      params: { organizationId },
      data: {
        baseSalary: Number(form.get('baseSalary')),
        allowance: Number(form.get('allowance')),
        deductions: Number(form.get('deductions')),
      },
    })
    await client.invalidateQueries({
      queryKey: getGetEmployeeCompensationQueryKey(employeeId, {
        organizationId,
      }),
    })
  }
  return (
    <form
      className="flex flex-col gap-4"
      onSubmit={(event) => {
        void submit(event).catch(() => {})
      }}
    >
      <p className="text-sm font-medium">{value.employeeName}</p>
      <FieldGroup>
        {(
          [
            ['baseSalary', 'Monthly base salary', value.baseSalary],
            ['allowance', 'Monthly allowance', value.allowance],
            ['deductions', 'Monthly deductions', value.deductions],
          ] as const
        ).map(([name, label, amount]) => (
          <Field key={name}>
            <FieldLabel htmlFor={`salary-${name}`}>{label}</FieldLabel>
            <Input
              id={`salary-${name}`}
              name={name}
              type="number"
              min="0"
              max="999999999.99"
              step="0.01"
              defaultValue={amount}
              required
            />
          </Field>
        ))}
      </FieldGroup>
      <PayrollError error={save.error} />
      {save.isSuccess && (
        <Alert>
          <AlertDescription>Employee salary saved.</AlertDescription>
        </Alert>
      )}
      <Button type="submit" disabled={save.isPending}>
        Save employee salary
      </Button>
    </form>
  )
}

function EmployeeSalary({
  organizationId,
  employeeId,
}: {
  organizationId: string
  employeeId: string
}) {
  const query = useGetEmployeeCompensation(employeeId, { organizationId })
  return query.isPending ? (
    <LoadingRows />
  ) : query.isError ? (
    <LoadError
      onRetry={() => {
        void query.refetch()
      }}
    />
  ) : query.data ? (
    <CompensationForm
      organizationId={organizationId}
      employeeId={employeeId}
      value={query.data}
    />
  ) : null
}

function OrganizationSettings({ organizationId }: { organizationId: string }) {
  const query = useGetPayrollSettings({ organizationId })
  const [employeeId, setEmployeeId] = useState('')
  return (
    <div className="grid items-start gap-6 lg:grid-cols-2">
      <ListCard
        title="Payroll configuration"
        description="Changes apply to new or recalculated drafts. Approved payroll keeps its saved amounts."
      >
        {query.isPending ? (
          <LoadingRows />
        ) : query.isError ? (
          <LoadError
            onRetry={() => {
              void query.refetch()
            }}
          />
        ) : (
          query.data && (
            <SettingsForm
              organizationId={organizationId}
              settings={query.data}
            />
          )
        )}
      </ListCard>
      <ListCard
        title="Employee salary"
        description={`Amounts are in ${query.data?.currency ?? 'the organization currency'}. Deductions are entered manually.`}
      >
        <PayrollEmployeePicker
          organizationId={organizationId}
          value={employeeId}
          onChange={setEmployeeId}
        />
        {employeeId && (
          <EmployeeSalary
            key={employeeId}
            organizationId={organizationId}
            employeeId={employeeId}
          />
        )}
      </ListCard>
    </div>
  )
}

export function PayrollSettingsPage() {
  const role = useRole()
  if (!role) return <LoadingRows />
  if (role !== 'ADMIN' && role !== 'HR_HEAD')
    return (
      <PageHeading
        title="Payroll settings"
        description="Payroll settings are available to HR staff and administrators."
      />
    )
  return (
    <PayrollOrganization settings>
      {(organizationId) => (
        <OrganizationSettings
          key={organizationId}
          organizationId={organizationId}
        />
      )}
    </PayrollOrganization>
  )
}
