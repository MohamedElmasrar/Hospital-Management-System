'use client'

import {
  getListDepartmentsQueryKey,
  useCreateDepartment,
  useListDepartments,
  useUpdateDepartment,
} from '@/app/api/generated/hr/department-controller/department-controller'
import type { DepartmentResponseDTO } from '@/app/api/generated/hr/model'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { keepPreviousData, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import {
  FormError,
  ListCard,
  LoadingRows,
  LoadError,
  OrganizationField,
  PageHeading,
  Pagination,
  PAGE_SIZE,
  useOrganization,
  useRole,
} from './shared'

function DepartmentEditor({
  organizationId,
  department,
  onClose,
}: {
  organizationId: string
  department?: DepartmentResponseDTO
  onClose: () => void
}) {
  const create = useCreateDepartment()
  const update = useUpdateDepartment()
  const queryClient = useQueryClient()
  const parents = useListDepartments(
    { organizationId, page: 0, size: 200 },
    { query: { enabled: !!organizationId } }
  )
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    const data = {
      name: String(form.get('name')).trim(),
      description: String(form.get('description')).trim(),
      parentDepartmentId: String(form.get('parent') || '') || undefined,
    }
    if (department?.id) await update.mutateAsync({ id: department.id, data })
    else
      await create.mutateAsync({
        data: { ...data, medicalOrganizationId: organizationId },
      })
    await queryClient.invalidateQueries({
      queryKey: getListDepartmentsQueryKey({ organizationId }),
    })
    onClose()
  }
  return (
    <form
      className="flex flex-col gap-4"
      onSubmit={(event) => {
        void submit(event).catch(() => {})
      }}
    >
      <FieldGroup>
        <Field>
          <FieldLabel htmlFor="department-name">Name</FieldLabel>
          <Input
            id="department-name"
            name="name"
            defaultValue={department?.name}
            required
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="department-description">Description</FieldLabel>
          <Input
            id="department-description"
            name="description"
            defaultValue={department?.description}
          />
        </Field>
        <Field>
          <FieldLabel htmlFor="department-parent">Parent department</FieldLabel>
          <NativeSelect
            id="department-parent"
            name="parent"
            defaultValue={department?.parentDepartmentId || ''}
          >
            <NativeSelectOption value="">None</NativeSelectOption>
            {(parents.data?.content || [])
              .filter((item) => item.id !== department?.id)
              .map((item) => (
                <NativeSelectOption key={item.id} value={item.id}>
                  {item.name}
                </NativeSelectOption>
              ))}
          </NativeSelect>
        </Field>
      </FieldGroup>
      <FormError error={create.error || update.error} />
      <Button disabled={create.isPending || update.isPending} type="submit">
        Save department
      </Button>
    </form>
  )
}

export function DepartmentsPage() {
  const role = useRole()
  const organizations = useOrganization(role === 'HR_HEAD' || role === 'ADMIN')
  const [page, setPage] = useState(0)
  const [editing, setEditing] = useState<DepartmentResponseDTO | null>(null)
  const [creating, setCreating] = useState(false)
  const query = useListDepartments(
    {
      organizationId: organizations.organizationId,
      page,
      size: PAGE_SIZE,
      sort: ['name,asc'],
    },
    {
      query: {
        enabled: !!organizations.organizationId,
        placeholderData: keepPreviousData,
      },
    }
  )
  if (role !== 'HR_HEAD' && role !== 'ADMIN')
    return (
      <PageHeading
        title="Departments"
        description="This page is available to HR staff."
      />
    )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Departments"
        description="Review and manage departments in your organization."
      />
      {organizations.isPending ? (
        <LoadingRows />
      ) : organizations.isError ? (
        <LoadError
          onRetry={() => {
            void organizations.refetch()
          }}
        />
      ) : !organizations.data?.length ? (
        <p className="text-sm text-muted-foreground">
          No assigned organizations.
        </p>
      ) : (
        <>
          <OrganizationField
            value={organizations.organizationId}
            onChange={(value) => {
              organizations.setSelected(value)
              setPage(0)
            }}
            options={organizations.data}
          />
          <ListCard
            title="Departments"
            description={`${query.data?.totalElements || 0} departments`}
          >
            <div className="flex justify-end">
              <Button onClick={() => setCreating(true)}>Add department</Button>
            </div>
            {query.isPending ? (
              <LoadingRows />
            ) : query.isError ? (
              <LoadError
                onRetry={() => {
                  void query.refetch()
                }}
              />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Parent</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead></TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {query.data?.content?.length ? (
                    query.data.content.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell className="font-medium">
                          {item.name}
                        </TableCell>
                        <TableCell>
                          {item.parentDepartmentName || '—'}
                        </TableCell>
                        <TableCell>{item.description || '—'}</TableCell>
                        <TableCell>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => setEditing(item)}
                          >
                            Edit
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell
                        colSpan={4}
                        className="h-24 text-center text-muted-foreground"
                      >
                        No departments found.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            )}
            <Pagination
              page={page}
              totalPages={query.data?.totalPages || 0}
              onChange={setPage}
            />
          </ListCard>
        </>
      )}
      <Dialog
        open={creating || !!editing}
        onOpenChange={(open) => {
          if (!open) {
            setCreating(false)
            setEditing(null)
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editing ? 'Edit department' : 'Add department'}
            </DialogTitle>
            <DialogDescription>
              Department details for the selected organization.
            </DialogDescription>
          </DialogHeader>
          <DepartmentEditor
            key={editing?.id || 'new'}
            organizationId={organizations.organizationId}
            department={editing || undefined}
            onClose={() => {
              setCreating(false)
              setEditing(null)
            }}
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}
