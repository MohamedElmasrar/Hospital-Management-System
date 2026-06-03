'use client'

import { useGetMyProfile } from '@/app/api/generated/hr/physician-controller/physician-controller'
import type {
  AvailableSlot,
  Response as Schedule,
} from '@/app/api/generated/reception/model'
import {
  useGetAvailableSlots,
  useGetSchedulesByPractitioner,
} from '@/app/api/generated/reception/schedule-controller/schedule-controller'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Field, FieldLabel } from '@/components/ui/field'
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
import { useState } from 'react'

const DAYS = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
] as const

function today() {
  const date = new Date()
  const offset = date.getTimezoneOffset() * 60_000
  return new Date(date.getTime() - offset).toISOString().slice(0, 10)
}

function formatTime(value?: string) {
  return value ? value.slice(0, 5) : '—'
}

function formatDay(value?: string) {
  if (!value) return '—'
  return value.charAt(0) + value.slice(1).toLowerCase()
}

function scheduleOrder(schedule: Schedule) {
  const dayIndex = DAYS.indexOf(schedule.dayOfWeek as (typeof DAYS)[number])
  return dayIndex === -1 ? DAYS.length : dayIndex
}

function LoadingRows() {
  return (
    <div className="flex flex-col gap-3 py-2">
      {Array.from({ length: 5 }, (_, index) => (
        <Skeleton className="h-10 w-full" key={index} />
      ))}
    </div>
  )
}

function WeeklyScheduleTable({ schedules }: { schedules: Schedule[] }) {
  const sortedSchedules = [...schedules].sort(
    (first, second) => scheduleOrder(first) - scheduleOrder(second)
  )

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Day</TableHead>
          <TableHead>Working hours</TableHead>
          <TableHead>Room</TableHead>
          <TableHead>Slot length</TableHead>
          <TableHead>Status</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {sortedSchedules.length ? (
          sortedSchedules.map((schedule) => (
            <TableRow key={schedule.id}>
              <TableCell className="font-medium">
                {formatDay(schedule.dayOfWeek)}
              </TableCell>
              <TableCell>
                {formatTime(schedule.startTime)}–{formatTime(schedule.endTime)}
              </TableCell>
              <TableCell>{schedule.roomName ?? '—'}</TableCell>
              <TableCell>
                {schedule.slotDurationMinutes
                  ? `${schedule.slotDurationMinutes} minutes`
                  : '—'}
              </TableCell>
              <TableCell>
                <Badge variant={schedule.isActive ? 'secondary' : 'outline'}>
                  {schedule.isActive ? 'Active' : 'Inactive'}
                </Badge>
              </TableCell>
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              className="h-24 text-center text-muted-foreground"
              colSpan={5}
            >
              No weekly schedule has been assigned.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}

function SlotsTable({ slots }: { slots: AvailableSlot[] }) {
  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Time</TableHead>
          <TableHead>Room</TableHead>
          <TableHead>Availability</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {slots.length ? (
          slots.map((slot) => (
            <TableRow
              key={[
                slot.date,
                slot.startTime,
                slot.endTime,
                slot.roomId,
                slot.practitionerId,
              ].join('|')}
            >
              <TableCell className="font-medium">
                {formatTime(slot.startTime)}–{formatTime(slot.endTime)}
              </TableCell>
              <TableCell>{slot.roomName ?? '—'}</TableCell>
              <TableCell>
                <Badge variant={slot.isAvailable ? 'secondary' : 'outline'}>
                  {slot.isAvailable ? 'Available' : 'Booked'}
                </Badge>
              </TableCell>
            </TableRow>
          ))
        ) : (
          <TableRow>
            <TableCell
              className="h-24 text-center text-muted-foreground"
              colSpan={3}
            >
              No working slots for this date.
            </TableCell>
          </TableRow>
        )}
      </TableBody>
    </Table>
  )
}

export function DoctorSchedule() {
  const [date, setDate] = useState(today)
  const physician = useGetMyProfile()
  const practitionerId = physician.data?.id ?? ''
  const schedules = useGetSchedulesByPractitioner(practitionerId)
  const slots = useGetAvailableSlots(
    { practitionerId: practitionerId || undefined, date },
    { query: { enabled: Boolean(practitionerId && date) } }
  )

  const schedulePending = physician.isPending || schedules.isPending
  const scheduleError = physician.isError || schedules.isError
  const slotsPending = physician.isPending || slots.isPending
  const slotsError = physician.isError || slots.isError

  function retrySchedule() {
    if (physician.isError) {
      void physician.refetch()
      return
    }
    void schedules.refetch()
  }

  function retrySlots() {
    if (physician.isError) {
      void physician.refetch()
      return
    }
    void slots.refetch()
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="flex flex-col gap-1">
        <h1 className="text-2xl font-semibold">My Schedule</h1>
        <p className="text-sm text-muted-foreground">
          Review your weekly working hours and appointment availability.
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Weekly schedule</CardTitle>
          <CardDescription>
            Your recurring hours, assigned rooms, and appointment duration.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {schedulePending ? (
            <LoadingRows />
          ) : scheduleError ? (
            <div className="flex items-center justify-between gap-4 py-8">
              <p className="text-sm text-muted-foreground">
                Unable to load your weekly schedule.
              </p>
              <Button variant="outline" onClick={retrySchedule}>
                Try again
              </Button>
            </div>
          ) : (
            <WeeklyScheduleTable schedules={schedules.data ?? []} />
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Daily availability</CardTitle>
          <CardDescription>
            See which appointment slots are available or already booked.
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <Field className="max-w-56">
            <FieldLabel htmlFor="schedule-date">Date</FieldLabel>
            <Input
              id="schedule-date"
              type="date"
              value={date}
              onChange={(event) => setDate(event.target.value)}
            />
          </Field>

          {slotsPending ? (
            <LoadingRows />
          ) : slotsError ? (
            <div className="flex items-center justify-between gap-4 py-8">
              <p className="text-sm text-muted-foreground">
                Unable to load availability for this date.
              </p>
              <Button variant="outline" onClick={retrySlots}>
                Try again
              </Button>
            </div>
          ) : (
            <SlotsTable slots={slots.data ?? []} />
          )}
        </CardContent>
      </Card>
    </div>
  )
}
