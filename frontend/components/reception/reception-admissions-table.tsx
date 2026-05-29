'use client'

import { useSearchAdmissions } from '@/app/api/generated/reception/admission-controller/admission-controller'
import { ReceptionListCard } from '@/components/reception/reception-list-card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { TableCell, TableRow } from '@/components/ui/table'
import { keepPreviousData } from '@tanstack/react-query'
import { useState } from 'react'

const PAGE_SIZE = 10
const HEADERS = [
  'Patient',
  'Phone',
  'Bed',
  'Ward',
  'Admitted',
  'Reason',
  'Status',
]

function formatDateTime(value?: string) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

export function ReceptionAdmissionsTable() {
  const [searchInput, setSearchInput] = useState('')
  const [query, setQuery] = useState('')
  const [activeOnly, setActiveOnly] = useState(true)
  const [page, setPage] = useState(0)
  const result = useSearchAdmissions(
    { query: query || undefined, activeOnly, page, size: PAGE_SIZE },
    { query: { placeholderData: keepPreviousData } }
  )
  const data = result.data
  const rows = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? page

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">Admissions</h1>
        <p className="text-sm text-muted-foreground">
          Review current and previous admissions.
        </p>
      </div>
      <ReceptionListCard
        title="Admission directory"
        description={activeOnly ? 'active admissions' : 'admissions'}
        total={data?.totalElements ?? 0}
        headers={HEADERS}
        searchPlaceholder="Search by patient, bed, or ward"
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
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              setActiveOnly((value) => !value)
              setPage(0)
            }}
          >
            {activeOnly ? 'Show all' : 'Active only'}
          </Button>
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
          rows.map((admission) => (
            <TableRow key={admission.id}>
              <TableCell className="font-medium">
                {admission.patientName ?? '—'}
              </TableCell>
              <TableCell>{admission.patientPhone ?? '—'}</TableCell>
              <TableCell>{admission.bedCode ?? '—'}</TableCell>
              <TableCell>{admission.ward ?? '—'}</TableCell>
              <TableCell>{formatDateTime(admission.admittedAt)}</TableCell>
              <TableCell className="max-w-56 truncate">
                {admission.reason ?? '—'}
              </TableCell>
              <TableCell>
                <Badge
                  variant={admission.dischargedAt ? 'outline' : 'secondary'}
                >
                  {admission.dischargedAt ? 'Discharged' : 'Admitted'}
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
              No admissions found.
            </TableCell>
          </TableRow>
        )}
      </ReceptionListCard>
    </div>
  )
}
