'use client'

import { useListCashDesks } from '@/app/api/generated/billing/cash-desks/cash-desks'
import {
  getRevenueQueryKey,
  getUnpaidInvoicesQueryKey,
} from '@/app/api/generated/billing/billing-report-controller/billing-report-controller'
import {
  getListInvoicesQueryKey,
  useListInvoices,
} from '@/app/api/generated/billing/invoices/invoices'
import type {
  PaymentResponseDTO,
  ProcessPaymentRequestDTOMethod,
} from '@/app/api/generated/billing/model'
import {
  getGetByCashDeskQueryKey,
  useGetByCashDesk,
  useProcessPayment,
  useRefundPayment,
} from '@/app/api/generated/billing/payments/payments'
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

const methods = ['CASH', 'CARD', 'BANK_TRANSFER', 'CHEQUE'] as const

export function PaymentsPageContent() {
  const role = useRole()
  const queryClient = useQueryClient()
  const [cashDeskId, setCashDeskId] = useState('')
  const [page, setPage] = useState(0)
  const [creating, setCreating] = useState(false)
  const [invoiceId, setInvoiceId] = useState('')
  const [amount, setAmount] = useState('')
  const [method, setMethod] = useState<ProcessPaymentRequestDTOMethod>('CASH')
  const [refunding, setRefunding] = useState<PaymentResponseDTO | null>(null)
  const [error, setError] = useState(false)
  const desks = useListCashDesks({ page: 0, size: 100 })
  const selectedDesk =
    cashDeskId || desks.data?.content?.find((item) => item.isActive)?.id || ''
  const payments = useGetByCashDesk(
    selectedDesk,
    { page, size: PAGE_SIZE, sort: ['paidAt,desc'] },
    { query: { enabled: !!selectedDesk } }
  )
  const invoices = useListInvoices(
    { status: 'ISSUED', page: 0, size: 100 },
    { query: { enabled: creating } }
  )
  const process = useProcessPayment()
  const refund = useRefundPayment()
  const rows = payments.data?.content ?? []

  async function refresh() {
    await queryClient.invalidateQueries({
      queryKey: getGetByCashDeskQueryKey(selectedDesk),
    })
    await queryClient.invalidateQueries({ queryKey: getListInvoicesQueryKey() })
    await queryClient.invalidateQueries({
      queryKey: getUnpaidInvoicesQueryKey(),
    })
    await queryClient.invalidateQueries({ queryKey: getRevenueQueryKey() })
  }

  async function savePayment() {
    setError(false)
    try {
      await process.mutateAsync({
        data: {
          invoiceId,
          cashDeskId: selectedDesk,
          amount: Number(amount),
          method,
        },
      })
      await refresh()
      setCreating(false)
      setInvoiceId('')
      setAmount('')
    } catch {
      setError(true)
    }
  }

  async function refundPayment() {
    if (!refunding?.id) return
    setError(false)
    try {
      await refund.mutateAsync({ id: refunding.id })
      await refresh()
      setRefunding(null)
    } catch {
      setError(true)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Payments"
        description="Record and review payments at a cash desk."
        action={
          <Button
            disabled={!selectedDesk}
            onClick={() => {
              setError(false)
              setCreating(true)
            }}
          >
            Record payment
          </Button>
        }
      />
      <DataCard
        title="Cash desk payments"
        description={String(payments.data?.totalElements ?? 0) + ' payments'}
        headers={[
          'Reference',
          'Invoice',
          'Amount',
          'Method',
          'Status',
          'Paid',
          '',
        ]}
        empty={rows.length === 0}
        pending={desks.isPending || (!!selectedDesk && payments.isPending)}
        error={desks.isError || payments.isError}
        retry={() => {
          if (desks.isError) void desks.refetch()
          else void payments.refetch()
        }}
        page={page}
        totalPages={payments.data?.totalPages ?? 0}
        previous={() => setPage((value) => value - 1)}
        next={() => setPage((value) => value + 1)}
        filters={
          <Field className="max-w-xs">
            <FieldLabel htmlFor="payment-desk">Cash desk</FieldLabel>
            <NativeSelect
              id="payment-desk"
              value={selectedDesk}
              onChange={(event) => {
                setCashDeskId(event.target.value)
                setPage(0)
              }}
            >
              {desks.data?.content
                ?.filter((item) => item.id)
                .map((item) => (
                  <NativeSelectOption key={item.id} value={item.id}>
                    {item.deskCode}
                  </NativeSelectOption>
                ))}
            </NativeSelect>
          </Field>
        }
      >
        {rows.map((item) => (
          <TableRow key={item.id}>
            <TableCell>{item.paymentReference ?? '—'}</TableCell>
            <TableCell>{item.invoiceNumber ?? '—'}</TableCell>
            <TableCell>{displayMoney(item.amount)}</TableCell>
            <TableCell>{item.method?.replaceAll('_', ' ') ?? '—'}</TableCell>
            <TableCell>
              <Badge variant="outline">{item.status ?? '—'}</Badge>
            </TableCell>
            <TableCell>{displayDate(item.paidAt)}</TableCell>
            <TableCell>
              {role === 'ADMIN' && item.status !== 'REFUNDED' && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => {
                    setError(false)
                    setRefunding(item)
                  }}
                >
                  Refund
                </Button>
              )}
            </TableCell>
          </TableRow>
        ))}
      </DataCard>
      {!desks.isPending && !desks.isError && !selectedDesk && (
        <p className="text-sm text-muted-foreground">
          No cash desk is available.
        </p>
      )}
      <Dialog open={creating} onOpenChange={setCreating}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Record payment</DialogTitle>
            <DialogDescription>
              Apply a payment to an issued invoice.
            </DialogDescription>
          </DialogHeader>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="payment-invoice">Invoice</FieldLabel>
              <NativeSelect
                id="payment-invoice"
                value={invoiceId}
                onChange={(event) => setInvoiceId(event.target.value)}
              >
                <NativeSelectOption value="">
                  Select issued invoice
                </NativeSelectOption>
                {invoices.data?.content
                  ?.filter((item) => item.id)
                  .map((item) => (
                    <NativeSelectOption key={item.id} value={item.id}>
                      {item.invoiceNumber} — {displayMoney(item.balanceDue)} due
                    </NativeSelectOption>
                  ))}
              </NativeSelect>
            </Field>
            <Field>
              <FieldLabel htmlFor="payment-amount">Amount</FieldLabel>
              <Input
                id="payment-amount"
                type="number"
                min="0.01"
                step="0.01"
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="payment-method">Method</FieldLabel>
              <NativeSelect
                id="payment-method"
                value={method}
                onChange={(event) =>
                  setMethod(
                    event.target.value as ProcessPaymentRequestDTOMethod
                  )
                }
              >
                {methods.map((value) => (
                  <NativeSelectOption key={value} value={value}>
                    {value.replaceAll('_', ' ')}
                  </NativeSelectOption>
                ))}
              </NativeSelect>
            </Field>
          </FieldGroup>
          {invoices.isPending && (
            <p className="text-sm text-muted-foreground">Loading invoices…</p>
          )}
          {invoices.isError && (
            <Button variant="outline" onClick={() => void invoices.refetch()}>
              Retry loading invoices
            </Button>
          )}
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setCreating(false)}>
              Cancel
            </Button>
            <Button
              disabled={
                !invoiceId ||
                !Number.isFinite(Number(amount)) ||
                Number(amount) <= 0 ||
                process.isPending
              }
              onClick={() => void savePayment()}
            >
              Record payment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <Dialog
        open={!!refunding}
        onOpenChange={(open) => {
          if (!open) setRefunding(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Refund payment</DialogTitle>
            <DialogDescription>
              Refund {refunding?.paymentReference} for{' '}
              {displayMoney(refunding?.amount)}?
            </DialogDescription>
          </DialogHeader>
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setRefunding(null)}>
              Cancel
            </Button>
            <Button
              variant="destructive"
              disabled={refund.isPending}
              onClick={() => void refundPayment()}
            >
              Refund
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
