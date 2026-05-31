'use client'

import { useOrganizations } from '@/app/api/generated/hr/workforce-controller/workforce-controller'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Field, FieldLabel } from '@/components/ui/field'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { Skeleton } from '@/components/ui/skeleton'
import {
  getAuthUser,
  getServerTokenSnapshot,
  getTokenSnapshot,
  subscribeAuth,
} from '@/lib/auth'
import { useState, useSyncExternalStore } from 'react'

export const PAGE_SIZE = 10

export function useRole() {
  const token = useSyncExternalStore(
    subscribeAuth,
    getTokenSnapshot,
    getServerTokenSnapshot
  )
  return token ? (getAuthUser()?.role ?? null) : null
}

export function useOrganization(enabled = true) {
  const [selected, setSelected] = useState('')
  const organizations = useOrganizations({ query: { enabled } })
  return {
    ...organizations,
    organizationId: selected || organizations.data?.[0]?.id || '',
    selected,
    setSelected,
  }
}

export function OrganizationField({
  value,
  onChange,
  options,
}: {
  value: string
  onChange: (value: string) => void
  options: { id: string; name: string }[]
}) {
  return (
    <Field className="max-w-xs">
      <FieldLabel htmlFor="hr-organization">Organization</FieldLabel>
      <NativeSelect
        id="hr-organization"
        className="w-full"
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        {options.map((option) => (
          <NativeSelectOption key={option.id} value={option.id}>
            {option.name}
          </NativeSelectOption>
        ))}
      </NativeSelect>
    </Field>
  )
}

export function PageHeading({
  title,
  description,
}: {
  title: string
  description: string
}) {
  return (
    <div className="flex flex-col gap-1">
      <h1 className="text-2xl font-semibold">{title}</h1>
      <p className="text-sm text-muted-foreground">{description}</p>
    </div>
  )
}

export function ListCard({
  title,
  description,
  children,
}: {
  title: string
  description?: string
  children: React.ReactNode
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardContent className="flex flex-col gap-4">{children}</CardContent>
    </Card>
  )
}

export function LoadingRows() {
  return (
    <div className="flex flex-col gap-3">
      {Array.from({ length: 5 }, (_, index) => (
        <Skeleton key={index} className="h-10 w-full" />
      ))}
    </div>
  )
}

export function LoadError({ onRetry }: { onRetry: () => void }) {
  return (
    <div className="flex items-center justify-between gap-3 py-6">
      <p className="text-sm text-muted-foreground">Unable to load records.</p>
      <Button variant="outline" onClick={onRetry}>
        Try again
      </Button>
    </div>
  )
}

export function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number
  totalPages: number
  onChange: (page: number) => void
}) {
  return (
    <div className="flex items-center justify-between gap-3 text-sm text-muted-foreground">
      <span>
        Page {totalPages ? page + 1 : 0} of {totalPages}
      </span>
      <div className="flex gap-2">
        <Button
          variant="outline"
          disabled={page <= 0}
          onClick={() => onChange(page - 1)}
        >
          Previous
        </Button>
        <Button
          variant="outline"
          disabled={page + 1 >= totalPages}
          onClick={() => onChange(page + 1)}
        >
          Next
        </Button>
      </div>
    </div>
  )
}

export function formatDate(value?: string) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(date)
}

export function useDateRange() {
  const [from, setFrom] = useState(() => {
    const current = new Date()
    const first = new Date(current.getFullYear(), current.getMonth(), 1)
    return localDate(first)
  })
  const [to, setTo] = useState(() => {
    const current = new Date()
    return localDate(new Date(current.getFullYear(), current.getMonth() + 1, 0))
  })
  return { from, to, setFrom, setTo }
}

function localDate(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

export function FormError({ error }: { error: unknown }) {
  if (!error) return null
  return (
    <p role="alert" className="text-sm text-destructive">
      The request could not be saved. Check the fields and try again.
    </p>
  )
}
