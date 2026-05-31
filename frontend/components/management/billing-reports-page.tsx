'use client'

import {
  useRevenue,
  useUnpaidInvoices,
} from '@/app/api/generated/billing/billing-report-controller/billing-report-controller'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { TableCell, TableRow } from '@/components/ui/table'
import { useState } from 'react'
import {
  DataCard,
  displayDate,
  displayMoney,
  PAGE_SIZE,
  PageHeading,
} from './shared'

export function BillingReportsPageContent() {
  const [today] = useState(() => new Date())
  const [from, setFrom] = useState(() =>
    new Date(today.getFullYear(), today.getMonth(), 1).toLocaleDateString(
      'sv-SE'
    )
  )
  const [to, setTo] = useState(() => today.toLocaleDateString('sv-SE'))
  const [page, setPage] = useState(0)
  const validRange = !!from && !!to && from <= to
  const revenue = useRevenue({ from, to }, { query: { enabled: validRange } })
  const unpaid = useUnpaidInvoices()
  const rows = (unpaid.data ?? []).slice(
    page * PAGE_SIZE,
    (page + 1) * PAGE_SIZE
  )
  const totalPages = Math.ceil((unpaid.data?.length ?? 0) / PAGE_SIZE)
  const balance = unpaid.data?.reduce(
    (sum, item) => sum + (item.balanceDue ?? 0),
    0
  )

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Billing reports"
        description="Review revenue and outstanding invoices."
      />
      <div className="flex flex-wrap gap-4">
        <Field className="max-w-48">
          <FieldLabel htmlFor="report-from">From</FieldLabel>
          <Input
            id="report-from"
            type="date"
            value={from}
            onChange={(event) => setFrom(event.target.value)}
          />
        </Field>
        <Field className="max-w-48">
          <FieldLabel htmlFor="report-to">To</FieldLabel>
          <Input
            id="report-to"
            type="date"
            value={to}
            onChange={(event) => setTo(event.target.value)}
          />
        </Field>
      </div>
      {!validRange && (
        <p role="alert" className="text-sm text-destructive">
          The start date must be before the end date.
        </p>
      )}
      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Revenue</CardTitle>
          </CardHeader>
          <CardContent>
            {!validRange ? (
              'Choose valid dates'
            ) : revenue.isPending ? (
              <Skeleton className="h-8 w-24" />
            ) : revenue.isError ? (
              'Unavailable'
            ) : (
              displayMoney(revenue.data?.totalRevenue)
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Payments</CardTitle>
          </CardHeader>
          <CardContent>
            {!validRange ? (
              'Choose valid dates'
            ) : revenue.isPending ? (
              <Skeleton className="h-8 w-24" />
            ) : revenue.isError ? (
              'Unavailable'
            ) : (
              (revenue.data?.paymentCount ?? 0)
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Outstanding balance</CardTitle>
          </CardHeader>
          <CardContent>
            {unpaid.isPending ? (
              <Skeleton className="h-8 w-24" />
            ) : unpaid.isError ? (
              'Unavailable'
            ) : (
              displayMoney(balance)
            )}
          </CardContent>
        </Card>
      </div>
      <DataCard
        title="Unpaid invoices"
        description={String(unpaid.data?.length ?? 0) + ' invoices'}
        headers={['Invoice', 'Patient ID', 'Total', 'Paid', 'Balance', 'Due']}
        empty={rows.length === 0}
        pending={unpaid.isPending}
        error={unpaid.isError}
        retry={() => void unpaid.refetch()}
        page={page}
        totalPages={totalPages}
        previous={() => setPage((value) => value - 1)}
        next={() => setPage((value) => value + 1)}
      >
        {rows.map((item) => (
          <TableRow key={item.invoiceId}>
            <TableCell>{item.invoiceNumber ?? '—'}</TableCell>
            <TableCell className="font-mono text-xs">
              {item.patientId ?? '—'}
            </TableCell>
            <TableCell>{displayMoney(item.totalAmount)}</TableCell>
            <TableCell>{displayMoney(item.paidAmount)}</TableCell>
            <TableCell>{displayMoney(item.balanceDue)}</TableCell>
            <TableCell>{displayDate(item.dueDate)}</TableCell>
          </TableRow>
        ))}
      </DataCard>
    </div>
  )
}
