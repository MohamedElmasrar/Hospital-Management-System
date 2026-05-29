'use client'

import { useSearchPatients } from '@/app/api/generated/reception/patient-controller/patient-controller'
import type { Response as Patient } from '@/app/api/generated/reception/model'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { keepPreviousData } from '@tanstack/react-query'
import { SearchIcon, XIcon } from 'lucide-react'
import { useState } from 'react'

const PAGE_SIZE = 10

function formatDate(value?: string) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(
    new Date(value)
  )
}

function PatientRow({ patient }: { patient: Patient }) {
  const fullName = `${patient.firstName ?? ''} ${patient.lastName ?? ''}`.trim()

  return (
    <TableRow>
      <TableCell className="font-medium">{fullName || '—'}</TableCell>
      <TableCell>{patient.mrn ?? '—'}</TableCell>
      <TableCell>{patient.nationalId ?? '—'}</TableCell>
      <TableCell>{formatDate(patient.dateOfBirth)}</TableCell>
      <TableCell>{patient.gender ?? '—'}</TableCell>
      <TableCell>{patient.phone ?? '—'}</TableCell>
      <TableCell>
        <Badge variant={patient.isActive ? 'secondary' : 'outline'}>
          {patient.isActive ? 'Active' : 'Inactive'}
        </Badge>
      </TableCell>
    </TableRow>
  )
}

function PatientsTableSkeleton() {
  return (
    <div className="flex flex-col gap-3">
      {Array.from({ length: 5 }, (_, index) => (
        <Skeleton className="h-10 w-full" key={index} />
      ))}
    </div>
  )
}

export function ReceptionPatientsTable() {
  const [searchInput, setSearchInput] = useState('')
  const [query, setQuery] = useState('')
  const [page, setPage] = useState(0)
  const { data, isPending, isError, refetch } = useSearchPatients(
    {
      query: query || undefined,
      pageable: { page, size: PAGE_SIZE, sort: ['lastName,asc'] },
    },
    { query: { placeholderData: keepPreviousData } }
  )

  const patients = data?.content ?? []
  const totalPages = data?.totalPages ?? 0
  const currentPage = data?.page ?? page

  function submitSearch(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    setPage(0)
    setQuery(searchInput.trim())
  }

  function clearSearch() {
    setSearchInput('')
    setQuery('')
    setPage(0)
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">Patients</h1>
        <p className="text-sm text-muted-foreground">
          Search and review registered patient information.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Patient directory</CardTitle>
          <CardDescription>
            {data?.totalElements ?? 0} registered patients
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <form onSubmit={submitSearch}>
            <FieldGroup>
              <Field orientation="responsive">
                <FieldLabel className="sr-only" htmlFor="patient-search">
                  Search patients
                </FieldLabel>
                <Input
                  id="patient-search"
                  placeholder="Search by name, MRN, national ID, or phone"
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                />
                <Button type="submit">
                  <SearchIcon data-icon="inline-start" />
                  Search
                </Button>
                {query && (
                  <Button type="button" variant="outline" onClick={clearSearch}>
                    <XIcon data-icon="inline-start" />
                    Clear
                  </Button>
                )}
              </Field>
            </FieldGroup>
          </form>

          {isPending ? (
            <PatientsTableSkeleton />
          ) : isError ? (
            <div className="flex items-center justify-between gap-4 py-8">
              <p className="text-sm text-muted-foreground">
                Unable to load patients.
              </p>
              <Button variant="outline" onClick={() => refetch()}>
                Try again
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>MRN</TableHead>
                  <TableHead>National ID</TableHead>
                  <TableHead>Date of birth</TableHead>
                  <TableHead>Gender</TableHead>
                  <TableHead>Phone</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {patients.length ? (
                  patients.map((patient, index) => (
                    <PatientRow
                      key={patient.id ?? `${patient.mrn}-${index}`}
                      patient={patient}
                    />
                  ))
                ) : (
                  <TableRow>
                    <TableCell
                      className="h-24 text-center text-muted-foreground"
                      colSpan={7}
                    >
                      No patients found.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}

          <div className="flex items-center justify-between gap-4">
            <p className="text-sm text-muted-foreground">
              Page {totalPages ? currentPage + 1 : 0} of {totalPages}
            </p>
            <div className="flex gap-2">
              <Button
                variant="outline"
                disabled={page === 0 || isPending}
                onClick={() => setPage((value) => Math.max(0, value - 1))}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                disabled={page + 1 >= totalPages || isPending}
                onClick={() => setPage((value) => value + 1)}
              >
                Next
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
