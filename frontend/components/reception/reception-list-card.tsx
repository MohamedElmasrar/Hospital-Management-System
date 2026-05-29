'use client'

import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { SearchIcon, XIcon } from 'lucide-react'
import type { ReactNode } from 'react'

type ReceptionListCardProps = {
  title: string
  description: string
  total: number
  headers: string[]
  searchPlaceholder: string
  searchInput: string
  activeQuery: string
  onSearchInputChange: (value: string) => void
  onSearch: (event: React.SubmitEvent<HTMLFormElement>) => void
  onClear: () => void
  isPending: boolean
  isError: boolean
  onRetry: () => void
  currentPage: number
  totalPages: number
  onPrevious: () => void
  onNext: () => void
  filters?: ReactNode
  children: ReactNode
}

export function ReceptionListCard({
  title,
  description,
  total,
  headers,
  searchPlaceholder,
  searchInput,
  activeQuery,
  onSearchInputChange,
  onSearch,
  onClear,
  isPending,
  isError,
  onRetry,
  currentPage,
  totalPages,
  onPrevious,
  onNext,
  filters,
  children,
}: ReceptionListCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>
          {total} {description}
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        <form className="flex flex-col gap-3 sm:flex-row" onSubmit={onSearch}>
          <Input
            aria-label={`Search ${title}`}
            className="sm:max-w-md"
            placeholder={searchPlaceholder}
            value={searchInput}
            onChange={(event) => onSearchInputChange(event.target.value)}
          />
          {filters}
          <Button type="submit">
            <SearchIcon data-icon="inline-start" />
            Search
          </Button>
          {activeQuery && (
            <Button type="button" variant="outline" onClick={onClear}>
              <XIcon data-icon="inline-start" />
              Clear
            </Button>
          )}
        </form>

        {isPending ? (
          <div className="flex flex-col gap-3">
            {Array.from({ length: 5 }, (_, index) => (
              <Skeleton className="h-10 w-full" key={index} />
            ))}
          </div>
        ) : isError ? (
          <div className="flex items-center justify-between gap-4 py-8">
            <p className="text-sm text-muted-foreground">
              Unable to load this list.
            </p>
            <Button variant="outline" onClick={onRetry}>
              Try again
            </Button>
          </div>
        ) : (
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
        )}

        <div className="flex items-center justify-between gap-4">
          <p className="text-sm text-muted-foreground">
            Page {totalPages ? currentPage + 1 : 0} of {totalPages}
          </p>
          <div className="flex gap-2">
            <Button
              variant="outline"
              disabled={currentPage === 0 || isPending}
              onClick={onPrevious}
            >
              Previous
            </Button>
            <Button
              variant="outline"
              disabled={currentPage + 1 >= totalPages || isPending}
              onClick={onNext}
            >
              Next
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
