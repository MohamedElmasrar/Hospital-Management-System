'use client'

import { useGetMyProfile1 } from '@/app/api/generated/hr/employee-controller/employee-controller'
import {
  getListLeaveQueryKey,
  useDecideLeave,
  useListLeave,
  useRequestLeave,
} from '@/app/api/generated/hr/workforce-controller/workforce-controller'
import type { LeaveResponse } from '@/app/api/generated/hr/model'
import { Badge } from '@/components/ui/badge'
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

function LeaveTable({
  records,
  onDecide,
}: {
  records: LeaveResponse[]
  onDecide?: (record: LeaveResponse) => void
}) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Employee</TableHead>
          <TableHead>Dates</TableHead>
          <TableHead>Reason</TableHead>
          <TableHead>Status</TableHead>
          {onDecide && <TableHead></TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {records.length ? (
          records.map((record) => (
            <TableRow key={record.id}>
              <TableCell className="font-medium">
                {record.employeeName || record.employeeId}
              </TableCell>
              <TableCell>
                {record.startsOn} – {record.endsOn}
              </TableCell>
              <TableCell className="max-w-64 truncate">
                {record.reason}
              </TableCell>
              <TableCell>
                <Badge variant="secondary">{record.status}</Badge>
              </TableCell>
              {onDecide && (
                <TableCell>
                  {record.status === 'PENDING' && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onDecide(record)}
                    >
                      Review
                    </Button>
                  )}
                </TableCell>
              )}
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              colSpan={onDecide ? 5 : 4}
              className="h-24 text-center text-muted-foreground"
            >
              No leave requests found.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}

function HrLeave() {
  const organizations = useOrganization()
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState('')
  const [review, setReview] = useState<LeaveResponse | null>(null)
  const [reason, setReason] = useState('')
  const query = useListLeave(
    {
      organizationId: organizations.organizationId,
      status: status || undefined,
      page,
      size: PAGE_SIZE,
    },
    {
      query: {
        enabled: !!organizations.organizationId,
        placeholderData: keepPreviousData,
      },
    }
  )
  const decide = useDecideLeave()
  const queryClient = useQueryClient()
  async function decision(value: 'APPROVED' | 'REJECTED') {
    if (!review?.id) return
    await decide.mutateAsync({
      id: review.id,
      data: { status: value, decisionReason: reason || undefined },
    })
    await queryClient.invalidateQueries({ queryKey: getListLeaveQueryKey() })
    setReview(null)
    setReason('')
  }
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Leave Requests"
        description="Review and decide employee leave requests."
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
            options={organizations.data}
            onChange={(value) => {
              organizations.setSelected(value)
              setPage(0)
            }}
          />
          <ListCard
            title="Requests"
            description={`${query.data?.totalElements || 0} requests`}
          >
            <Field className="max-w-48">
              <FieldLabel htmlFor="leave-status">Status</FieldLabel>
              <NativeSelect
                id="leave-status"
                value={status}
                onChange={(event) => {
                  setStatus(event.target.value)
                  setPage(0)
                }}
              >
                <NativeSelectOption value="">All statuses</NativeSelectOption>
                <NativeSelectOption value="PENDING">Pending</NativeSelectOption>
                <NativeSelectOption value="APPROVED">
                  Approved
                </NativeSelectOption>
                <NativeSelectOption value="REJECTED">
                  Rejected
                </NativeSelectOption>
              </NativeSelect>
            </Field>
            {query.isPending ? (
              <LoadingRows />
            ) : query.isError ? (
              <LoadError
                onRetry={() => {
                  void query.refetch()
                }}
              />
            ) : (
              <LeaveTable
                records={query.data?.content || []}
                onDecide={setReview}
              />
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
        open={!!review}
        onOpenChange={(open) => {
          if (!open) setReview(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Review leave request</DialogTitle>
            <DialogDescription>
              {review?.employeeName} · {review?.startsOn} to {review?.endsOn}
            </DialogDescription>
          </DialogHeader>
          <p className="text-sm">{review?.reason}</p>
          <Field>
            <FieldLabel htmlFor="leave-decision-reason">
              Decision reason (optional)
            </FieldLabel>
            <Input
              id="leave-decision-reason"
              value={reason}
              onChange={(event) => setReason(event.target.value)}
            />
          </Field>
          <FormError error={decide.error} />
          <div className="flex justify-end gap-2">
            <Button
              variant="outline"
              disabled={decide.isPending}
              onClick={() => {
                void decision('REJECTED').catch(() => {})
              }}
            >
              Reject
            </Button>
            <Button
              disabled={decide.isPending}
              onClick={() => {
                void decision('APPROVED').catch(() => {})
              }}
            >
              Approve
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}

function RequestLeave({
  employeeId,
  onClose,
}: {
  employeeId: string
  onClose: () => void
}) {
  const mutation = useRequestLeave()
  const queryClient = useQueryClient()
  async function submit(event: React.SubmitEvent<HTMLFormElement>) {
    event.preventDefault()
    const form = new FormData(event.currentTarget)
    await mutation.mutateAsync({
      data: {
        employeeId,
        startsOn: String(form.get('from')),
        endsOn: String(form.get('to')),
        reason: String(form.get('reason')).trim(),
      },
    })
    await queryClient.invalidateQueries({ queryKey: getListLeaveQueryKey() })
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
          <FieldLabel htmlFor="leave-from">From</FieldLabel>
          <Input id="leave-from" name="from" type="date" required />
        </Field>
        <Field>
          <FieldLabel htmlFor="leave-to">To</FieldLabel>
          <Input id="leave-to" name="to" type="date" required />
        </Field>
        <Field>
          <FieldLabel htmlFor="leave-reason">Reason</FieldLabel>
          <Input id="leave-reason" name="reason" required />
        </Field>
      </FieldGroup>
      <FormError error={mutation.error} />
      <Button type="submit" disabled={mutation.isPending}>
        Submit request
      </Button>
    </form>
  )
}

function MyLeave() {
  const profile = useGetMyProfile1()
  const [page, setPage] = useState(0)
  const [creating, setCreating] = useState(false)
  const query = useListLeave(
    { page, size: PAGE_SIZE },
    {
      query: { enabled: !!profile.data?.id, placeholderData: keepPreviousData },
    }
  )
  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Leave Requests"
        description="View and submit your leave requests."
      />
      <ListCard title="My requests">
        <div className="flex justify-end">
          <Button
            disabled={!profile.data?.id}
            onClick={() => setCreating(true)}
          >
            Request leave
          </Button>
        </div>
        {profile.isPending || query.isPending ? (
          <LoadingRows />
        ) : profile.isError || query.isError ? (
          <LoadError
            onRetry={() => {
              void profile.refetch()
              void query.refetch()
            }}
          />
        ) : (
          <LeaveTable records={query.data?.content || []} />
        )}
        <Pagination
          page={page}
          totalPages={query.data?.totalPages || 0}
          onChange={setPage}
        />
      </ListCard>
      <Dialog open={creating} onOpenChange={setCreating}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Request leave</DialogTitle>
            <DialogDescription>
              Choose dates and provide a reason.
            </DialogDescription>
          </DialogHeader>
          {profile.data?.id && (
            <RequestLeave
              employeeId={profile.data.id}
              onClose={() => setCreating(false)}
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}

export function LeavePage() {
  const role = useRole()
  return role === 'HR_HEAD' || role === 'ADMIN' ? (
    <HrLeave />
  ) : role === 'EMPLOYEE' ? (
    <MyLeave />
  ) : (
    <PageHeading
      title="Leave Requests"
      description="This page is available to employees and HR staff."
    />
  )
}
