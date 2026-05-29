'use client'

import { useSearchReservations } from '@/app/api/generated/reception/reservation-controller/reservation-controller'
import type { ReservationResponse } from '@/app/api/generated/reception/model'
import { ReceptionListCard } from '@/components/reception/reception-list-card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { TableCell, TableRow } from '@/components/ui/table'
import { keepPreviousData } from '@tanstack/react-query'
import { useState } from 'react'

const PAGE_SIZE = 10
const HEADERS = [
  'Reference',
  'Patient',
  'Date',
  'Time',
  'Type',
  'Room',
  'Status',
]

function formatTime(value: ReservationResponse['startTime'] | string) {
  if (!value) return '—'
  if (typeof value === 'string') return value.slice(0, 5)
  return `${String(value.hour ?? 0).padStart(2, '0')}:${String(value.minute ?? 0).padStart(2, '0')}`
}

export function ReceptionAppointmentsTable() {
  const [searchInput, setSearchInput] = useState('')
  const [query, setQuery] = useState('')
  const [date, setDate] = useState('')
  const [page, setPage] = useState(0)
  const result = useSearchReservations(
    {
      query: query || undefined,
      date: date || undefined,
      page,
      size: PAGE_SIZE,
    },
    { query: { placeholderData: keepPreviousData } }
  )
  const data = result.data
  const rows = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? page

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">Appointments</h1>
        <p className="text-sm text-muted-foreground">
          Find and review patient reservations.
        </p>
      </div>
      <ReceptionListCard
        title="Appointment directory"
        description="appointments"
        total={data?.totalElements ?? 0}
        headers={HEADERS}
        searchPlaceholder="Search by reference, patient, or phone"
        searchInput={searchInput}
        activeQuery={query}
        onSearchInputChange={setSearchInput}
        onSearch={(event) => {
          event.preventDefault()
          setPage(0)
          setQuery(searchInput.trim())
        }}
        onClear={() => {
          setSearchInput('')
          setQuery('')
          setPage(0)
        }}
        filters={
          <Input
            aria-label="Appointment date"
            className="sm:w-44"
            type="date"
            value={date}
            onChange={(event) => {
              setDate(event.target.value)
              setPage(0)
            }}
          />
        }
        isPending={result.isPending}
        isError={result.isError}
        onRetry={() => result.refetch()}
        currentPage={currentPage}
        totalPages={totalPages}
        onPrevious={() => setPage((value) => Math.max(0, value - 1))}
        onNext={() => setPage((value) => value + 1)}
      >
        {rows.length ? (
          rows.map((appointment) => (
            <TableRow key={appointment.id}>
              <TableCell className="font-medium">
                {appointment.reservationNumber ?? '—'}
              </TableCell>
              <TableCell>{appointment.patientName ?? '—'}</TableCell>
              <TableCell>{appointment.reservationDate ?? '—'}</TableCell>
              <TableCell>{formatTime(appointment.startTime)}</TableCell>
              <TableCell>{appointment.categoryName ?? '—'}</TableCell>
              <TableCell>{appointment.roomName ?? '—'}</TableCell>
              <TableCell>
                <Badge variant="outline">
                  {appointment.statusName ?? 'Unknown'}
                </Badge>
              </TableCell>
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              className="h-24 text-center text-muted-foreground"
              colSpan={7}
            >
              No appointments found.
            </TableCell>
          </TableRow>
        )}
      </ReceptionListCard>
    </div>
  )
}
