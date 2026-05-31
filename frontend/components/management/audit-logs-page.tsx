'use client'

import { useAuditLogs } from '@/app/api/generated/auth/admin-user-controller/admin-user-controller'
import { Input } from '@/components/ui/input'
import { TableCell, TableRow } from '@/components/ui/table'
import { useState } from 'react'
import { DataCard, displayDate, PAGE_SIZE, PageHeading } from './shared'

export function AuditLogsPageContent() {
  const logs = useAuditLogs()
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const filtered = (logs.data ?? [])
    .filter((item) =>
      (item.action ?? '').toLowerCase().includes(search.toLowerCase().trim())
    )
    .sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''))
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE)
  const rows = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Audit logs"
        description="Review account administration actions."
      />
      <DataCard
        title="Recent actions"
        description={String(filtered.length) + ' actions'}
        headers={['When', 'Action', 'Actor ID', 'Target ID']}
        empty={rows.length === 0}
        pending={logs.isPending}
        error={logs.isError}
        retry={() => void logs.refetch()}
        page={page}
        totalPages={totalPages}
        previous={() => setPage((value) => value - 1)}
        next={() => setPage((value) => value + 1)}
        filters={
          <Input
            aria-label="Search audit actions"
            placeholder="Search by action"
            className="max-w-sm"
            value={search}
            onChange={(event) => {
              setSearch(event.target.value)
              setPage(0)
            }}
          />
        }
      >
        {rows.map((item) => (
          <TableRow key={item.id}>
            <TableCell>{displayDate(item.createdAt)}</TableCell>
            <TableCell>{item.action?.replaceAll('_', ' ') ?? '—'}</TableCell>
            <TableCell className="font-mono text-xs">
              {item.actorUserId ?? '—'}
            </TableCell>
            <TableCell className="font-mono text-xs">
              {item.targetUserId ?? '—'}
            </TableCell>
          </TableRow>
        ))}
      </DataCard>
    </div>
  )
}
