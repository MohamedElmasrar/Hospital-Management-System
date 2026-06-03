'use client'

import { usePrepareConsultationInvoice } from '@/app/api/generated/billing/consultation-billing-controller/consultation-billing-controller'
import { useListCashDesks } from '@/app/api/generated/billing/cash-desks/cash-desks'
import { useListServices } from '@/app/api/generated/billing/service-catalog-tariffs/service-catalog-tariffs'
import {
  getGetInvoiceQueryKey,
  getListInvoicesQueryKey,
  useGetInvoice,
} from '@/app/api/generated/billing/invoices/invoices'
import {
  getUnpaidInvoicesQueryKey,
  getRevenueQueryKey,
  useUnpaidInvoices,
} from '@/app/api/generated/billing/billing-report-controller/billing-report-controller'
import {
  getGetByCashDeskQueryKey,
  useProcessPayment,
} from '@/app/api/generated/billing/payments/payments'
import type { ProcessPaymentRequestDTOMethod } from '@/app/api/generated/billing/model'
import {
  getSearchUnpaidConsultationsQueryKey,
  getSearchReservationsQueryKey,
  useSearchUnpaidConsultations,
  useMarkAsPaid,
} from '@/app/api/generated/reception/reservation-controller/reservation-controller'
import type { ReservationResponse } from '@/app/api/generated/reception/model'
import { DataCard, displayMoney } from '@/components/management/shared'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Skeleton } from '@/components/ui/skeleton'
import { TableCell, TableRow } from '@/components/ui/table'
import { useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { useState } from 'react'
import { useRole } from '@/components/hr/shared'

export function BillerOverview() {
  const role = useRole()
  if (!role) return <Skeleton className="h-80 w-full" />
  if (role !== 'BILLER' && role !== 'ADMIN')
    return (
      <p className="text-sm text-muted-foreground">
        Billing access is required.
      </p>
    )
  return <BillerContent />
}

function BillerContent() {
  const client = useQueryClient()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [query, setQuery] = useState('')
  const [selected, setSelected] = useState<ReservationResponse | null>(null)
  const [deskId, setDeskId] = useState('')
  const [serviceId, setServiceId] = useState('')
  const [invoiceId, setInvoiceId] = useState('')
  const [method, setMethod] = useState<ProcessPaymentRequestDTOMethod>('CASH')
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const queue = useSearchUnpaidConsultations({
    query: query || undefined,
    page,
    size: 10,
  })
  const unpaid = useUnpaidInvoices()
  const desks = useListCashDesks(
    { page: 0, size: 100 },
    { query: { enabled: !!selected } }
  )
  const services = useListServices(
    { departmentId: selected?.departmentId, page: 0, size: 100 },
    { query: { enabled: !!selected } }
  )
  const prepare = usePrepareConsultationInvoice()
  const payment = useProcessPayment()
  const approve = useMarkAsPaid()
  const invoice = useGetInvoice(invoiceId, { query: { enabled: !!invoiceId } })
  const selectedDesk =
    deskId || desks.data?.content?.find((desk) => desk.isActive)?.id || ''
  const busy = prepare.isPending || payment.isPending || approve.isPending

  async function refresh() {
    await Promise.all([
      client.invalidateQueries({
        queryKey: getSearchUnpaidConsultationsQueryKey(),
      }),
      client.invalidateQueries({ queryKey: getSearchReservationsQueryKey() }),
      client.invalidateQueries({ queryKey: getListInvoicesQueryKey() }),
      client.invalidateQueries({ queryKey: getUnpaidInvoicesQueryKey() }),
      client.invalidateQueries({ queryKey: getRevenueQueryKey() }),
      ...(invoice.data?.cashDeskId
        ? [
            client.invalidateQueries({
              queryKey: getGetByCashDeskQueryKey(invoice.data.cashDeskId),
            }),
          ]
        : []),
      ...(invoiceId
        ? [
            client.invalidateQueries({
              queryKey: getGetInvoiceQueryKey(invoiceId),
            }),
          ]
        : []),
    ])
  }

  function showError(value: unknown) {
    const data = axios.isAxiosError(value) ? value.response?.data : null
    setError(
      typeof data?.error === 'string'
        ? data.error
        : typeof data?.message === 'string'
          ? data.message
          : 'The action could not be completed. Refresh the invoice and try again.'
    )
  }

  async function prepareInvoice() {
    if (!selected?.id) return
    setError('')
    try {
      const result = await prepare.mutateAsync({
        reservationId: selected.id,
        data: { cashDeskId: selectedDesk, serviceCatalogId: serviceId },
      })
      setInvoiceId(result.id ?? '')
      await refresh()
    } catch (value) {
      showError(value)
    }
  }

  async function collectAndApprove() {
    if (!selected?.id || !invoice.data?.id || !invoice.data.cashDeskId) return
    setError('')
    try {
      if (invoice.data.status !== 'PAID') {
        await payment.mutateAsync({
          data: {
            invoiceId,
            cashDeskId: invoice.data.cashDeskId,
            amount: invoice.data.balanceDue ?? 0,
            method,
          },
        })
      }
      await approve.mutateAsync({ id: selected.id, params: { invoiceId } })
      setNotice(
        `Payment approved for ${selected.reservationNumber}. The patient can go to ${selected.roomName ?? 'the assigned consultation room'}.`
      )
      setSelected(null)
    } catch (value) {
      showError(value)
    } finally {
      await refresh()
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Consultations awaiting approval</CardTitle>
          </CardHeader>
          <CardContent>
            {queue.isPending ? (
              <Skeleton className="h-8 w-20" />
            ) : queue.isError ? (
              'Unavailable'
            ) : (
              (queue.data?.totalElements ?? 0)
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Outstanding invoice balance</CardTitle>
          </CardHeader>
          <CardContent>
            {unpaid.isPending ? (
              <Skeleton className="h-8 w-24" />
            ) : unpaid.isError ? (
              <Button variant="outline" onClick={() => void unpaid.refetch()}>
                Retry
              </Button>
            ) : (
              displayMoney(
                unpaid.data?.reduce(
                  (sum, item) => sum + (item.balanceDue ?? 0),
                  0
                )
              )
            )}
          </CardContent>
        </Card>
      </div>
      {notice && (
        <p role="status" className="text-sm">
          {notice}
        </p>
      )}
      <DataCard
        title="Consultation payments"
        description="Prepare the invoice, collect the balance, and approve the paid consultation."
        headers={['Reference', 'Patient', 'Date', 'Room', 'Action']}
        pending={queue.isPending}
        error={queue.isError}
        retry={() => void queue.refetch()}
        empty={!queue.data?.content?.length}
        page={page}
        totalPages={queue.data?.totalPages ?? 0}
        previous={() => setPage((value) => Math.max(0, value - 1))}
        next={() => setPage((value) => value + 1)}
        filters={
          <form
            className="flex gap-2"
            onSubmit={(event) => {
              event.preventDefault()
              setQuery(search.trim())
              setPage(0)
            }}
          >
            <Input
              aria-label="Search consultations"
              placeholder="Patient name, phone, or reservation reference"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
            <Button type="submit" variant="outline">
              Search
            </Button>
          </form>
        }
      >
        {queue.data?.content?.map((item) => (
          <TableRow key={item.id}>
            <TableCell>{item.reservationNumber}</TableCell>
            <TableCell>{item.patientName}</TableCell>
            <TableCell>{item.reservationDate}</TableCell>
            <TableCell>{item.roomName ?? 'To be confirmed'}</TableCell>
            <TableCell>
              <Button
                size="sm"
                onClick={() => {
                  setSelected(item)
                  setInvoiceId('')
                  setServiceId('')
                  setError('')
                  setNotice('')
                }}
              >
                Collect payment
              </Button>
            </TableCell>
          </TableRow>
        ))}
      </DataCard>
      <Dialog
        open={!!selected}
        onOpenChange={(open) => {
          if (!busy && !open) setSelected(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Consultation payment</DialogTitle>
            <DialogDescription>
              {selected?.reservationNumber} · {selected?.patientName}
            </DialogDescription>
          </DialogHeader>
          {!invoiceId ? (
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="biller-desk">Cash desk</FieldLabel>
                <NativeSelect
                  id="biller-desk"
                  className="w-full"
                  value={selectedDesk}
                  onChange={(event) => setDeskId(event.target.value)}
                >
                  <NativeSelectOption value="">
                    Select cash desk
                  </NativeSelectOption>
                  {desks.data?.content
                    ?.filter((item) => item.isActive)
                    .map((item) => (
                      <NativeSelectOption key={item.id} value={item.id}>
                        {item.deskCode}
                      </NativeSelectOption>
                    ))}
                </NativeSelect>
              </Field>
              <Field>
                <FieldLabel htmlFor="biller-service">
                  Consultation service
                </FieldLabel>
                <NativeSelect
                  id="biller-service"
                  className="w-full"
                  value={serviceId}
                  onChange={(event) => setServiceId(event.target.value)}
                >
                  <NativeSelectOption value="">
                    Select service and tariff
                  </NativeSelectOption>
                  {services.data?.content
                    ?.filter((item) => item.isActive && item.currentTariff)
                    .map((item) => (
                      <NativeSelectOption key={item.id} value={item.id}>
                        {item.name} ·{' '}
                        {displayMoney(item.currentTariff?.unitPrice)}
                      </NativeSelectOption>
                    ))}
                </NativeSelect>
              </Field>
              {(desks.isPending || services.isPending) && (
                <Skeleton className="h-8 w-full" />
              )}
              {(desks.isError || services.isError) && (
                <Button
                  variant="outline"
                  onClick={() => {
                    void desks.refetch()
                    void services.refetch()
                  }}
                >
                  Retry loading billing options
                </Button>
              )}
              {!services.isPending && !services.data?.content?.length && (
                <p className="text-sm text-muted-foreground">
                  No service tariff is configured for this department.
                </p>
              )}
            </FieldGroup>
          ) : invoice.isPending ? (
            <Skeleton className="h-24 w-full" />
          ) : invoice.isError ? (
            <Button variant="outline" onClick={() => void invoice.refetch()}>
              Retry invoice
            </Button>
          ) : (
            <FieldGroup>
              <p className="text-sm">
                Invoice {invoice.data?.invoiceNumber} · {invoice.data?.status}
              </p>
              <p className="font-medium">
                Balance to collect: {displayMoney(invoice.data?.balanceDue)}
              </p>
              {invoice.data?.status === 'PAID' ? (
                <p className="text-sm text-muted-foreground">
                  Payment is recorded. Approve the consultation to complete the
                  handoff.
                </p>
              ) : (
                <Field>
                  <FieldLabel htmlFor="biller-method">
                    Payment method
                  </FieldLabel>
                  <NativeSelect
                    id="biller-method"
                    value={method}
                    onChange={(event) =>
                      setMethod(
                        event.target.value as ProcessPaymentRequestDTOMethod
                      )
                    }
                  >
                    {(['CASH', 'CARD', 'BANK_TRANSFER', 'CHEQUE'] as const).map(
                      (value) => (
                        <NativeSelectOption key={value} value={value}>
                          {value.replaceAll('_', ' ')}
                        </NativeSelectOption>
                      )
                    )}
                  </NativeSelect>
                </Field>
              )}
            </FieldGroup>
          )}
          {error && (
            <p role="alert" className="text-sm text-destructive">
              {error}
            </p>
          )}
          <DialogFooter>
            {invoiceId ? (
              <Button
                disabled={busy || !invoice.data || invoice.isFetching}
                onClick={() => void collectAndApprove()}
              >
                {busy
                  ? 'Processing…'
                  : invoice.data?.status === 'PAID'
                    ? 'Approve consultation'
                    : 'Collect and approve'}
              </Button>
            ) : (
              <Button
                disabled={busy || !selectedDesk || !serviceId}
                onClick={() => void prepareInvoice()}
              >
                {prepare.isPending ? 'Preparing…' : 'Prepare invoice'}
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
