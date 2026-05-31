'use client'

import { useListCashDesks } from '@/app/api/generated/billing/cash-desks/cash-desks'
import { getUnpaidInvoicesQueryKey } from '@/app/api/generated/billing/billing-report-controller/billing-report-controller'
import {
  getListInvoicesQueryKey,
  getGetInvoiceQueryKey,
  useCreateAutomaticInvoice,
  useGetInvoice,
  useListInvoices,
  useUpdateStatus,
  useVoidInvoice,
} from '@/app/api/generated/billing/invoices/invoices'
import type {
  InvoiceSummaryResponseDTO,
  ListInvoicesStatus,
} from '@/app/api/generated/billing/model'
import { useRole } from '@/components/hr/shared'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Skeleton } from '@/components/ui/skeleton'
import { TableCell, TableRow } from '@/components/ui/table'
import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import {
  DataCard,
  displayDate,
  displayMoney,
  FormError,
  PAGE_SIZE,
  PageHeading,
} from './shared'

const statuses = ['DRAFT', 'ISSUED', 'PAID', 'VOID', 'REFUNDED'] as const

export function InvoicesPageContent() {
  const role = useRole()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [patientInput, setPatientInput] = useState('')
  const [patientId, setPatientId] = useState('')
  const [status, setStatus] = useState('')
  const [selected, setSelected] = useState<InvoiceSummaryResponseDTO | null>(
    null
  )
  const [voiding, setVoiding] = useState<InvoiceSummaryResponseDTO | null>(null)
  const [creating, setCreating] = useState(false)
  const [newPatientId, setNewPatientId] = useState('')
  const [cashDeskId, setCashDeskId] = useState('')
  const [dueDate, setDueDate] = useState('')
  const [error, setError] = useState(false)
  const invoices = useListInvoices({
    patientId: patientId || undefined,
    status: (status || undefined) as ListInvoicesStatus | undefined,
    page,
    size: PAGE_SIZE,
    sort: ['dateCreated,desc'],
  })
  const desks = useListCashDesks({ page: 0, size: 100 })
  const create = useCreateAutomaticInvoice()
  const issue = useUpdateStatus()
  const voidInvoice = useVoidInvoice()
  const detail = useGetInvoice(selected?.id ?? '', {
    query: { enabled: !!selected?.id },
  })
  const rows = invoices.data?.content ?? []

  async function refresh() {
    await queryClient.invalidateQueries({ queryKey: getListInvoicesQueryKey() })
    await queryClient.invalidateQueries({
      queryKey: getUnpaidInvoicesQueryKey(),
    })
    if (selected?.id)
      await queryClient.invalidateQueries({
        queryKey: getGetInvoiceQueryKey(selected.id),
      })
  }

  async function createInvoice() {
    setError(false)
    try {
      await create.mutateAsync({
        data: {
          patientId: newPatientId.trim(),
          cashDeskId,
          dueDate: dueDate || undefined,
        },
      })
      await refresh()
      setCreating(false)
      setNewPatientId('')
      setCashDeskId('')
      setDueDate('')
    } catch {
      setError(true)
    }
  }

  async function issueInvoice() {
    if (!selected?.id) return
    setError(false)
    try {
      await issue.mutateAsync({ id: selected.id, data: { status: 'ISSUED' } })
      await refresh()
      setSelected(null)
    } catch {
      setError(true)
    }
  }

  async function voidSelected() {
    if (!voiding?.id) return
    setError(false)
    try {
      await voidInvoice.mutateAsync({ id: voiding.id })
      await refresh()
      setVoiding(null)
    } catch {
      setError(true)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Invoices"
        description="Review patient invoices and billing status."
        action={
          <Button
            onClick={() => {
              setError(false)
              setCreating(true)
            }}
          >
            Create from charges
          </Button>
        }
      />
      <DataCard
        title="Invoices"
        description={String(invoices.data?.totalElements ?? 0) + ' invoices'}
        headers={[
          'Invoice',
          'Patient ID',
          'Status',
          'Total',
          'Balance',
          'Due',
          '',
        ]}
        empty={rows.length === 0}
        pending={invoices.isPending}
        error={invoices.isError}
        retry={() => void invoices.refetch()}
        page={page}
        totalPages={invoices.data?.totalPages ?? 0}
        previous={() => setPage((value) => value - 1)}
        next={() => setPage((value) => value + 1)}
        filters={
          <form
            className="flex flex-wrap items-end gap-3"
            onSubmit={(event) => {
              event.preventDefault()
              setPatientId(patientInput.trim())
              setPage(0)
            }}
          >
            <Field className="max-w-xs">
              <FieldLabel htmlFor="invoice-patient">Patient ID</FieldLabel>
              <Input
                id="invoice-patient"
                placeholder="Optional patient UUID"
                value={patientInput}
                onChange={(event) => setPatientInput(event.target.value)}
              />
            </Field>
            <Field className="max-w-48">
              <FieldLabel htmlFor="invoice-status">Status</FieldLabel>
              <NativeSelect
                id="invoice-status"
                value={status}
                onChange={(event) => {
                  setStatus(event.target.value)
                  setPage(0)
                }}
              >
                <NativeSelectOption value="">All statuses</NativeSelectOption>
                {statuses.map((value) => (
                  <NativeSelectOption key={value} value={value}>
                    {value}
                  </NativeSelectOption>
                ))}
              </NativeSelect>
            </Field>
            <Button type="submit" variant="outline">
              Apply
            </Button>
          </form>
        }
      >
        {rows.map((item) => (
          <TableRow key={item.id}>
            <TableCell>{item.invoiceNumber ?? '—'}</TableCell>
            <TableCell className="font-mono text-xs">
              {item.patientId ?? '—'}
            </TableCell>
            <TableCell>
              <Badge variant="outline">{item.status ?? '—'}</Badge>
            </TableCell>
            <TableCell>{displayMoney(item.totalAmount)}</TableCell>
            <TableCell>{displayMoney(item.balanceDue)}</TableCell>
            <TableCell>{displayDate(item.dueDate)}</TableCell>
            <TableCell>
              <Button
                size="sm"
                variant="outline"
                onClick={() => {
                  setSelected(item)
                  setError(false)
                }}
              >
                View
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </DataCard>
      <Dialog open={creating} onOpenChange={setCreating}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Create invoice from charges</DialogTitle>
            <DialogDescription>
              Collect all unbilled charges for one patient.
            </DialogDescription>
          </DialogHeader>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="new-invoice-patient">Patient ID</FieldLabel>
              <Input
                id="new-invoice-patient"
                required
                value={newPatientId}
                onChange={(event) => setNewPatientId(event.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="new-invoice-desk">Cash desk</FieldLabel>
              <NativeSelect
                id="new-invoice-desk"
                required
                value={cashDeskId}
                onChange={(event) => setCashDeskId(event.target.value)}
              >
                <NativeSelectOption value="">
                  Select cash desk
                </NativeSelectOption>
                {desks.data?.content
                  ?.filter((item) => item.id && item.isActive)
                  .map((item) => (
                    <NativeSelectOption key={item.id} value={item.id}>
                      {item.deskCode}
                    </NativeSelectOption>
                  ))}
              </NativeSelect>
            </Field>
            <Field>
              <FieldLabel htmlFor="new-invoice-due">Due date</FieldLabel>
              <Input
                id="new-invoice-due"
                type="date"
                value={dueDate}
                onChange={(event) => setDueDate(event.target.value)}
              />
            </Field>
          </FieldGroup>
          {desks.isError && (
            <p className="text-sm text-destructive">
              Cash desks could not be loaded.
            </p>
          )}
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreating(false)}>
              Cancel
            </Button>
            <Button
              disabled={!newPatientId.trim() || !cashDeskId || create.isPending}
              onClick={() => void createInvoice()}
            >
              Create invoice
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <Dialog
        open={!!selected}
        onOpenChange={(open) => {
          if (!open) setSelected(null)
        }}
      >
        <DialogContent className="sm:max-w-xl">
          <DialogHeader>
            <DialogTitle>{selected?.invoiceNumber ?? 'Invoice'}</DialogTitle>
            <DialogDescription>Invoice details and actions</DialogDescription>
          </DialogHeader>
          {detail.isPending ? (
            <Skeleton className="h-32 w-full" />
          ) : detail.isError ? (
            <Button variant="outline" onClick={() => void detail.refetch()}>
              Retry loading invoice
            </Button>
          ) : (
            <div className="flex flex-col gap-2 text-sm">
              <p>
                Patient:{' '}
                <span className="font-mono">{detail.data?.patientId}</span>
              </p>
              <p>Status: {detail.data?.status}</p>
              <p>Total: {displayMoney(detail.data?.totalAmount)}</p>
              <p>Paid: {displayMoney(detail.data?.paidAmount)}</p>
              <p>Balance: {displayMoney(detail.data?.balanceDue)}</p>
              <p>Line items: {detail.data?.lineItems?.length ?? 0}</p>
            </div>
          )}
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelected(null)}>
              Close
            </Button>
            {selected?.status === 'DRAFT' && (
              <Button
                disabled={issue.isPending}
                onClick={() => void issueInvoice()}
              >
                Issue
              </Button>
            )}
            {role === 'ADMIN' &&
              selected?.status !== 'VOID' &&
              selected?.status !== 'PAID' && (
                <Button
                  variant="destructive"
                  disabled={voidInvoice.isPending}
                  onClick={() => {
                    setVoiding(selected)
                    setSelected(null)
                    setError(false)
                  }}
                >
                  Void
                </Button>
              )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <Dialog
        open={!!voiding}
        onOpenChange={(open) => {
          if (!open) setVoiding(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Void invoice</DialogTitle>
            <DialogDescription>
              Void {voiding?.invoiceNumber}? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setVoiding(null)}>
              Cancel
            </Button>
            <Button
              variant="destructive"
              disabled={voidInvoice.isPending}
              onClick={() => void voidSelected()}
            >
              Void invoice
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
