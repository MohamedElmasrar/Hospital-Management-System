'use client'

import { useSearchTickets } from '@/app/api/generated/reception/queue-controller/queue-controller'
import { ReceptionListCard } from '@/components/reception/reception-list-card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { TableCell, TableRow } from '@/components/ui/table'
import { keepPreviousData } from '@tanstack/react-query'
import { useState } from 'react'

const PAGE_SIZE = 10
const HEADERS = ['Ticket', 'Patient', 'Room', 'Priority', 'Issued', 'Status']
const today = new Date().toISOString().slice(0, 10)

function formatDateTime(value?: string) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export function ReceptionQueueTable() {
  const [searchInput, setSearchInput] = useState('')
  const [query, setQuery] = useState('')
  const [date, setDate] = useState(today)
  const [page, setPage] = useState(0)
  const result = useSearchTickets(
    { query: query || undefined, date, page, size: PAGE_SIZE },
    { query: { placeholderData: keepPreviousData } }
  )
  const data = result.data
  const rows = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? page

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">Waiting Queue</h1>
        <p className="text-sm text-muted-foreground">
          Review tickets for a selected day.
        </p>
      </div>
      <ReceptionListCard
        title="Queue tickets"
        description="tickets"
        total={data?.totalElements ?? 0}
        headers={HEADERS}
        searchPlaceholder="Search by ticket, patient, or room"
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
            aria-label="Queue date"
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
          rows.map((ticket) => (
            <TableRow key={ticket.id}>
              <TableCell className="font-medium">
                {ticket.ticketCode ?? '—'}
              </TableCell>
              <TableCell>{ticket.patientName ?? '—'}</TableCell>
              <TableCell>{ticket.roomName ?? '—'}</TableCell>
              <TableCell>{ticket.priorityName ?? '—'}</TableCell>
              <TableCell>{formatDateTime(ticket.issuedAt)}</TableCell>
              <TableCell>
                <Badge variant="outline">{ticket.status ?? 'Unknown'}</Badge>
              </TableCell>
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              className="h-24 text-center text-muted-foreground"
              colSpan={6}
            >
              No queue tickets found.
            </TableCell>
          </TableRow>
        )}
      </ReceptionListCard>
    </div>
  )
}
