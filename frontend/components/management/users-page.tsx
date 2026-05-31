'use client'

import {
  getAuditLogsQueryKey,
  getListUsersQueryKey,
  useListUsers,
  useUpdateActive,
  useUpdateRole,
} from '@/app/api/generated/auth/admin-user-controller/admin-user-controller'
import { useMe } from '@/app/api/generated/auth/auth-controller/auth-controller'
import type {
  UpdateUserRoleRequestRole,
  UserResponse,
} from '@/app/api/generated/auth/model'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Field, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { TableCell, TableRow } from '@/components/ui/table'
import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { DataCard, FormError, PAGE_SIZE, PageHeading } from './shared'

const roles = [
  'ADMIN',
  'DOCTOR',
  'EMPLOYEE',
  'RECEPTIONIST',
  'HR_HEAD',
] as const

export function UsersPageContent() {
  const users = useListUsers()
  const me = useMe()
  const updateRole = useUpdateRole()
  const updateActive = useUpdateActive()
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<UserResponse | null>(null)
  const [role, setRole] = useState<UpdateUserRoleRequestRole>('EMPLOYEE')
  const [error, setError] = useState(false)
  const filtered = (users.data ?? []).filter((user) =>
    (user.email ?? '').toLowerCase().includes(search.toLowerCase().trim())
  )
  const totalPages = Math.ceil(filtered.length / PAGE_SIZE)
  const rows = filtered.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE)

  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: getListUsersQueryKey() }),
      queryClient.invalidateQueries({ queryKey: getAuditLogsQueryKey() }),
    ])
  }

  async function saveRole() {
    if (!editing?.id) return
    setError(false)
    try {
      await updateRole.mutateAsync({ id: editing.id, data: { role } })
      await refresh()
      setEditing(null)
    } catch {
      setError(true)
    }
  }

  async function toggleActive(user: UserResponse) {
    if (!user.id) return
    setError(false)
    try {
      await updateActive.mutateAsync({
        id: user.id,
        params: { active: !user.active },
      })
      await refresh()
    } catch {
      setError(true)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Users"
        description="Manage staff access and application roles."
      />
      <DataCard
        title="Application accounts"
        description={String(filtered.length) + ' accounts'}
        headers={['Email', 'Role', 'Status', 'Actions']}
        empty={rows.length === 0}
        pending={users.isPending}
        error={users.isError}
        retry={() => void users.refetch()}
        page={page}
        totalPages={totalPages}
        previous={() => setPage((value) => value - 1)}
        next={() => setPage((value) => value + 1)}
        filters={
          <Input
            aria-label="Search users"
            placeholder="Search by email"
            className="max-w-sm"
            value={search}
            onChange={(event) => {
              setSearch(event.target.value)
              setPage(0)
            }}
          />
        }
      >
        {rows.map((user) => (
          <TableRow key={user.id}>
            <TableCell>{user.email ?? '—'}</TableCell>
            <TableCell>{user.role?.replaceAll('_', ' ') ?? '—'}</TableCell>
            <TableCell>
              <Badge variant={user.active ? 'secondary' : 'outline'}>
                {user.active ? 'Active' : 'Inactive'}
              </Badge>
            </TableCell>
            <TableCell>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setEditing(user)
                    setRole(
                      user.role === 'DISABLED'
                        ? 'EMPLOYEE'
                        : (user.role ?? 'EMPLOYEE')
                    )
                    setError(false)
                  }}
                >
                  Change role
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={updateActive.isPending || user.id === me.data?.id}
                  onClick={() => void toggleActive(user)}
                >
                  {user.active ? 'Deactivate' : 'Activate'}
                </Button>
              </div>
            </TableCell>
          </TableRow>
        ))}
      </DataCard>
      <FormError visible={error && !editing} />
      <Dialog
        open={!!editing}
        onOpenChange={(open) => {
          if (!open) setEditing(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Change user role</DialogTitle>
            <DialogDescription>{editing?.email}</DialogDescription>
          </DialogHeader>
          <Field>
            <FieldLabel htmlFor="user-role">Role</FieldLabel>
            <NativeSelect
              id="user-role"
              value={role}
              onChange={(event) =>
                setRole(event.target.value as UpdateUserRoleRequestRole)
              }
            >
              {roles.map((value) => (
                <NativeSelectOption key={value} value={value}>
                  {value.replaceAll('_', ' ')}
                </NativeSelectOption>
              ))}
            </NativeSelect>
          </Field>
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button
              disabled={updateRole.isPending || role === editing?.role}
              onClick={() => void saveRole()}
            >
              Save role
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
