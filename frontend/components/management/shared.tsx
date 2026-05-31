'use client'

import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import type { ReactNode } from 'react'

export const PAGE_SIZE = 10

export function PageHeading({
  title,
  description,
  action,
}: {
  title: string
  description: string
  action?: ReactNode
}) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">{title}</h1>
        <p className="text-sm text-muted-foreground">{description}</p>
      </div>
      {action}
    </div>
  )
}

export function DataCard({
  title,
  description,
  headers,
  children,
  empty,
  pending,
  error,
  retry,
  page,
  totalPages,
  previous,
  next,
  filters,
}: {
  title: string
  description?: string
  headers: string[]
  children: ReactNode
  empty: boolean
  pending: boolean
  error: boolean
  retry: () => void
  page?: number
  totalPages?: number
  previous?: () => void
  next?: () => void
  filters?: ReactNode
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {filters}
        {pending ? (
          <div className="flex flex-col gap-3">
            {Array.from({ length: 5 }, (_, index) => (
              <Skeleton key={index} className="h-9 w-full" />
            ))}
          </div>
        ) : error ? (
          <div className="flex items-center justify-between gap-3 py-8">
            <p className="text-sm text-muted-foreground">
              Unable to load data.
            </p>
            <Button variant="outline" onClick={retry}>
              Try again
            </Button>
          </div>
        ) : empty ? (
          <p className="py-10 text-center text-sm text-muted-foreground">
            No records found.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  {headers.map((header) => (
                    <TableHead key={header}>{header}</TableHead>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>{children}</TableBody>
            </Table>
          </div>
        )}
        {page !== undefined && totalPages !== undefined && (
          <div className="flex items-center justify-between gap-3 text-sm">
            <span className="text-muted-foreground">
              Page {totalPages ? page + 1 : 0} of {totalPages}
            </span>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={page === 0 || pending}
                onClick={previous}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={page + 1 >= totalPages || pending}
                onClick={next}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}

export function displayDate(value?: string) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(date)
}

export function displayMoney(value?: number) {
  return value === undefined
    ? '—'
    : new Intl.NumberFormat('en', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(value)
}

export function FormError({ visible }: { visible: boolean }) {
  return visible ? (
    <p role="alert" className="text-sm text-destructive">
      Could not save. Check the details and try again.
    </p>
  ) : null
}
