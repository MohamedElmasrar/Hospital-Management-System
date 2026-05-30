'use client'

import { useGetMyProfile } from '@/app/api/generated/hr/physician-controller/physician-controller'
import type { ReservationResponse } from '@/app/api/generated/reception/model'
import { useSearchDoctorConsultations } from '@/app/api/generated/reception/reservation-controller/reservation-controller'
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
  'Reason',
  'Room',
  'Status',
]

function formatTime(value: ReservationResponse['startTime'] | string) {
  if (!value) return '—'
  if (typeof value === 'string') return value.slice(0, 5)
  return `${String(value.hour ?? 0).padStart(2, '0')}:${String(value.minute ?? 0).padStart(2, '0')}`
}

export function DoctorConsultationsTable() {
  const [searchInput, setSearchInput] = useState('')
  const [search, setSearch] = useState('')
  const [date, setDate] = useState('')
  const [page, setPage] = useState(0)
  const physician = useGetMyProfile()
  const practitionerId = physician.data?.id
  const consultations = useSearchDoctorConsultations(
    practitionerId ?? '',
    {
      query: search || undefined,
      date: date || undefined,
      page,
      size: PAGE_SIZE,
    },
    { query: { placeholderData: keepPreviousData } }
  )

  const data = consultations.data
  const rows = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? page
  const isPending = physician.isPending || consultations.isPending
  const isError = physician.isError || consultations.isError

  function retry() {
    if (physician.isError) {
      void physician.refetch()
      return
    }
    void consultations.refetch()
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">Consultations</h1>
        <p className="text-sm text-muted-foreground">
          Review your patient consultation appointments.
        </p>
      </div>

      <ReceptionListCard
        title="My consultations"
        description="consultations"
        total={data?.totalElements ?? 0}
        headers={HEADERS}
        searchPlaceholder="Search by reference, patient, or phone"
        searchInput={searchInput}
        activeQuery={search}
        onSearchInputChange={setSearchInput}
        onSearch={(event) => {
          event.preventDefault()
          setPage(0)
          setSearch(searchInput.trim())
        }}
        onClear={() => {
          setSearchInput('')
          setSearch('')
          setPage(0)
        }}
        filters={
          <Input
            aria-label="Consultation date"
            className="sm:w-44"
            type="date"
            value={date}
            onChange={(event) => {
              setDate(event.target.value)
              setPage(0)
            }}
          />
        }
        isPending={isPending}
        isError={isError}
        onRetry={retry}
        currentPage={currentPage}
        totalPages={totalPages}
        onPrevious={() => setPage((value) => Math.max(0, value - 1))}
        onNext={() => setPage((value) => value + 1)}
      >
        {rows.length ? (
          rows.map((consultation) => (
            <TableRow key={consultation.id}>
              <TableCell className="font-medium">
                {consultation.reservationNumber ?? '—'}
              </TableCell>
              <TableCell>{consultation.patientName ?? '—'}</TableCell>
              <TableCell>{consultation.reservationDate ?? '—'}</TableCell>
              <TableCell>{formatTime(consultation.startTime)}</TableCell>
              <TableCell className="max-w-56 truncate">
                {consultation.reasonForVisit ?? '—'}
              </TableCell>
              <TableCell>{consultation.roomName ?? '—'}</TableCell>
              <TableCell>
                <Badge variant="outline">
                  {consultation.statusName ?? 'Unknown'}
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
              No consultations found.
            </TableCell>
          </TableRow>
        )}
      </ReceptionListCard>
    </div>
  )
}
