'use client'

import { useListDepartments } from '@/app/api/generated/hr/department-controller/department-controller'
import { useListMedicalOrganizations } from '@/app/api/generated/hr/medical-organization-controller/medical-organization-controller'
import { useSearchPhysicians } from '@/app/api/generated/hr/physician-controller/physician-controller'
import {
  getSearchPatientsQueryKey,
  useCreatePatient,
  useSearchPatients,
} from '@/app/api/generated/reception/patient-controller/patient-controller'
import {
  getSearchReservationsQueryKey,
  useCreateConsultationReservation,
} from '@/app/api/generated/reception/reservation-controller/reservation-controller'
import { useListRooms } from '@/app/api/generated/reception/room-controller/room-controller'
import type {
  PatientResponse,
  ReservationResponse,
} from '@/app/api/generated/reception/model'
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
import { useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import { CalendarPlus } from 'lucide-react'
import Link from 'next/link'
import { useState, type FormEvent } from 'react'

function errorMessage(error: unknown, fallback: string) {
  if (!axios.isAxiosError(error)) return fallback

  const response = error.response?.data
  if (!response || typeof response !== 'object') return fallback

  const messages = Object.values(response).filter(
    (value): value is string => typeof value === 'string'
  )
  return messages.join(' ') || fallback
}

export function CreateAppointmentDialog() {
  const queryClient = useQueryClient()
  const [open, setOpen] = useState(false)
  const [patientInput, setPatientInput] = useState('')
  const [patientQuery, setPatientQuery] = useState('')
  const [selectedPatient, setSelectedPatient] =
    useState<PatientResponse | null>(null)
  const [registerPatient, setRegisterPatient] = useState(false)
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [birthDate, setBirthDate] = useState('')
  const [gender, setGender] = useState('')
  const [phone, setPhone] = useState('')
  const [organizationId, setOrganizationId] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [physicianInput, setPhysicianInput] = useState('')
  const [physicianQuery, setPhysicianQuery] = useState('')
  const [physicianId, setPhysicianId] = useState('')
  const [roomId, setRoomId] = useState('')
  const [date, setDate] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [reason, setReason] = useState('')
  const [created, setCreated] = useState<ReservationResponse | null>(null)
  const [error, setError] = useState('')

  const patients = useSearchPatients(
    { query: patientQuery, pageable: { page: 0, size: 20 } },
    { query: { enabled: open && !!patientQuery && !selectedPatient } }
  )
  const organizations = useListMedicalOrganizations(
    { page: 0, size: 100 },
    { query: { enabled: open } }
  )
  const selectedOrganizationId =
    organizationId || organizations.data?.content?.[0]?.id || ''
  const departments = useListDepartments(
    { organizationId: selectedOrganizationId, page: 0, size: 100 },
    { query: { enabled: open && !!selectedOrganizationId } }
  )
  const physicians = useSearchPhysicians(
    { search: physicianQuery || undefined, page: 0, size: 100 },
    { query: { enabled: open } }
  )
  const rooms = useListRooms({ query: { enabled: open } })
  const availableRooms =
    rooms.data?.filter(
      (room) => room.isActive && room.departmentId === departmentId
    ) ?? []
  const createPatient = useCreatePatient()
  const createReservation = useCreateConsultationReservation()

  function handleOpenChange(nextOpen: boolean) {
    setOpen(nextOpen)
    if (nextOpen) {
      setDate(new Date().toLocaleDateString('sv-SE'))
      setCreated(null)
      setError('')
      setPatientInput('')
      setPatientQuery('')
      setSelectedPatient(null)
      setRegisterPatient(false)
      setDepartmentId('')
      setPhysicianId('')
      setRoomId('')
      setStartTime('')
      setEndTime('')
      setReason('')
    }
  }

  async function savePatient(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    try {
      const patient = await createPatient.mutateAsync({
        data: { firstName, lastName, dateOfBirth: birthDate, gender, phone },
      })
      setSelectedPatient(patient)
      setRegisterPatient(false)
      await queryClient.invalidateQueries({
        queryKey: getSearchPatientsQueryKey(),
      })
    } catch (error) {
      setError(
        errorMessage(error, 'Could not register the patient. Try again.')
      )
    }
  }

  async function saveAppointment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (
      !selectedPatient?.id ||
      !selectedOrganizationId ||
      !departmentId ||
      !physicianId
    ) {
      setError('Select a patient, department, and doctor.')
      return
    }
    if (endTime <= startTime) {
      setError('End time must be after start time.')
      return
    }
    setError('')
    try {
      const reservation = await createReservation.mutateAsync({
        data: {
          patientId: selectedPatient.id,
          departmentId,
          practitionerId: physicianId,
          roomId: roomId || undefined,
          reservationDate: date,
          startTime,
          endTime,
          reasonForVisit: reason.trim() || undefined,
        },
      })
      setCreated(reservation)
      await queryClient.invalidateQueries({
        queryKey: getSearchReservationsQueryKey(),
      })
    } catch (error) {
      setError(
        errorMessage(error, 'Could not create the appointment. Try again.')
      )
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <Button onClick={() => handleOpenChange(true)}>
        <CalendarPlus data-icon="inline-start" />
        New appointment
      </Button>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-xl">
        <DialogHeader>
          <DialogTitle>
            {created ? 'Appointment created' : 'New consultation appointment'}
          </DialogTitle>
          <DialogDescription>
            {created
              ? 'Give the patient the reference and direct them to billing before they visit the doctor.'
              : 'Find or register the patient, then choose a doctor and time.'}
          </DialogDescription>
        </DialogHeader>

        {created ? (
          <div className="space-y-4">
            <div className="rounded-lg border p-4 text-sm">
              <p className="font-medium">
                {created.reservationNumber ?? 'Appointment saved'}
              </p>
              <p className="text-muted-foreground">
                {created.patientName ??
                  `${selectedPatient?.firstName ?? ''} ${selectedPatient?.lastName ?? ''}`}
              </p>
              <p className="text-muted-foreground">
                {created.reservationDate} · {startTime}–{endTime}
              </p>
              <p className="text-muted-foreground">
                {created.roomName
                  ? `Room: ${created.roomName}`
                  : 'Room to be confirmed'}
              </p>
              <p className="mt-2 font-medium">Payment pending</p>
            </div>
            <ol className="list-inside list-decimal space-y-1 text-sm text-muted-foreground">
              <li>Send the patient to the billing desk with this reference.</li>
              <li>Billing records the payment.</li>
              <li>
                Once payment is confirmed, direct the patient to the assigned
                room.
              </li>
            </ol>
            <DialogFooter>
              <Button variant="outline" onClick={() => setOpen(false)}>
                Close
              </Button>
              <Button
                nativeButton={false}
                render={<Link href="/reception/appointments" />}
                onClick={() => setOpen(false)}
              >
                View appointments
              </Button>
            </DialogFooter>
          </div>
        ) : registerPatient ? (
          <form className="space-y-4" onSubmit={savePatient}>
            <div className="grid gap-3 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="patient-first-name">First name</FieldLabel>
                <Input
                  id="patient-first-name"
                  required
                  value={firstName}
                  onChange={(event) => setFirstName(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="patient-last-name">Last name</FieldLabel>
                <Input
                  id="patient-last-name"
                  required
                  value={lastName}
                  onChange={(event) => setLastName(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="patient-birth-date">
                  Date of birth
                </FieldLabel>
                <Input
                  id="patient-birth-date"
                  type="date"
                  required
                  value={birthDate}
                  onChange={(event) => setBirthDate(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="patient-gender">Gender</FieldLabel>
                <NativeSelect
                  id="patient-gender"
                  className="w-full"
                  required
                  value={gender}
                  onChange={(event) => setGender(event.target.value)}
                >
                  <NativeSelectOption value="">
                    Select gender
                  </NativeSelectOption>
                  <NativeSelectOption value="MALE">Male</NativeSelectOption>
                  <NativeSelectOption value="FEMALE">Female</NativeSelectOption>
                  <NativeSelectOption value="OTHER">Other</NativeSelectOption>
                </NativeSelect>
              </Field>
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="patient-phone">Phone</FieldLabel>
                <Input
                  id="patient-phone"
                  type="tel"
                  required
                  value={phone}
                  onChange={(event) => setPhone(event.target.value)}
                />
              </Field>
            </div>
            {error && (
              <p role="alert" className="text-sm text-destructive">
                {error}
              </p>
            )}
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setRegisterPatient(false)
                  setError('')
                }}
              >
                Back
              </Button>
              <Button type="submit" disabled={createPatient.isPending}>
                Register patient
              </Button>
            </DialogFooter>
          </form>
        ) : (
          <form className="space-y-4" onSubmit={saveAppointment}>
            <div className="space-y-2">
              <FieldLabel htmlFor="appointment-patient-search">
                Patient
              </FieldLabel>
              {selectedPatient ? (
                <div className="flex items-center justify-between rounded-lg border p-2 text-sm">
                  <span>
                    {selectedPatient.firstName} {selectedPatient.lastName} ·{' '}
                    {selectedPatient.mrn}
                  </span>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    onClick={() => setSelectedPatient(null)}
                  >
                    Change
                  </Button>
                </div>
              ) : (
                <>
                  <div className="flex gap-2">
                    <Input
                      id="appointment-patient-search"
                      placeholder="Name, MRN, or phone"
                      value={patientInput}
                      onChange={(event) => setPatientInput(event.target.value)}
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => setPatientQuery(patientInput.trim())}
                    >
                      Search
                    </Button>
                  </div>
                  {patientQuery && (
                    <div className="max-h-32 overflow-y-auto rounded-lg border p-1 text-sm">
                      {patients.isPending ? (
                        <p className="p-2 text-muted-foreground">
                          Searching patients…
                        </p>
                      ) : patients.isError ? (
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={() => void patients.refetch()}
                        >
                          Retry patient search
                        </Button>
                      ) : patients.data?.content?.length ? (
                        patients.data.content.map((patient) => (
                          <button
                            key={patient.id}
                            type="button"
                            className="block w-full rounded p-2 text-left hover:bg-accent"
                            onClick={() => setSelectedPatient(patient)}
                          >
                            {patient.firstName} {patient.lastName} ·{' '}
                            {patient.mrn}
                          </button>
                        ))
                      ) : (
                        <p className="p-2 text-muted-foreground">
                          No matching patients.
                        </p>
                      )}
                    </div>
                  )}
                  <Button
                    type="button"
                    variant="link"
                    className="px-0"
                    onClick={() => {
                      setRegisterPatient(true)
                      setError('')
                    }}
                  >
                    Register a new patient
                  </Button>
                </>
              )}
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="appointment-organization">
                  Hospital
                </FieldLabel>
                <NativeSelect
                  id="appointment-organization"
                  className="w-full"
                  required
                  value={selectedOrganizationId}
                  onChange={(event) => {
                    setOrganizationId(event.target.value)
                    setDepartmentId('')
                    setRoomId('')
                  }}
                >
                  <NativeSelectOption value="">
                    Select hospital
                  </NativeSelectOption>
                  {organizations.data?.content?.map((item) => (
                    <NativeSelectOption key={item.id} value={item.id}>
                      {item.name}
                    </NativeSelectOption>
                  ))}
                </NativeSelect>
              </Field>
              <Field>
                <FieldLabel htmlFor="appointment-department">
                  Department
                </FieldLabel>
                <NativeSelect
                  id="appointment-department"
                  className="w-full"
                  required
                  value={departmentId}
                  onChange={(event) => {
                    setDepartmentId(event.target.value)
                    setRoomId('')
                  }}
                >
                  <NativeSelectOption value="">
                    Select department
                  </NativeSelectOption>
                  {departments.data?.content?.map(
                    (item) =>
                      item.id && (
                        <NativeSelectOption key={item.id} value={item.id}>
                          {item.name}
                        </NativeSelectOption>
                      )
                  )}
                </NativeSelect>
              </Field>
            </div>
            <Field>
              <FieldLabel htmlFor="appointment-room">
                Room (if assigned)
              </FieldLabel>
              <NativeSelect
                id="appointment-room"
                className="w-full"
                value={roomId}
                onChange={(event) => setRoomId(event.target.value)}
              >
                <NativeSelectOption value="">Assign later</NativeSelectOption>
                {availableRooms.map(
                  (room) =>
                    room.id && (
                      <NativeSelectOption key={room.id} value={room.id}>
                        {room.name} ({room.roomCode})
                      </NativeSelectOption>
                    )
                )}
              </NativeSelect>
            </Field>
            <div className="space-y-2">
              <FieldLabel htmlFor="appointment-doctor-search">
                Doctor
              </FieldLabel>
              <div className="flex gap-2">
                <Input
                  id="appointment-doctor-search"
                  placeholder="Search doctor"
                  value={physicianInput}
                  onChange={(event) => setPhysicianInput(event.target.value)}
                />
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setPhysicianQuery(physicianInput.trim())}
                >
                  Search
                </Button>
              </div>
              <NativeSelect
                className="w-full"
                aria-label="Select doctor"
                required
                value={physicianId}
                onChange={(event) => setPhysicianId(event.target.value)}
              >
                <NativeSelectOption value="">Select doctor</NativeSelectOption>
                {physicians.data?.content?.map(
                  (item) =>
                    item.id && (
                      <NativeSelectOption key={item.id} value={item.id}>
                        {item.employeeName ??
                          item.employeeIdentifier ??
                          item.id}
                      </NativeSelectOption>
                    )
                )}
              </NativeSelect>
            </div>
            <div className="grid gap-3 sm:grid-cols-3">
              <Field>
                <FieldLabel htmlFor="appointment-date">Date</FieldLabel>
                <Input
                  id="appointment-date"
                  type="date"
                  required
                  value={date}
                  onChange={(event) => setDate(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="appointment-start">Start</FieldLabel>
                <Input
                  id="appointment-start"
                  type="time"
                  required
                  value={startTime}
                  onChange={(event) => setStartTime(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="appointment-end">End</FieldLabel>
                <Input
                  id="appointment-end"
                  type="time"
                  required
                  value={endTime}
                  onChange={(event) => setEndTime(event.target.value)}
                />
              </Field>
            </div>
            <Field>
              <FieldLabel htmlFor="appointment-reason">
                Reason for visit (optional)
              </FieldLabel>
              <Input
                id="appointment-reason"
                value={reason}
                onChange={(event) => setReason(event.target.value)}
              />
            </Field>
            {(organizations.isError ||
              departments.isError ||
              physicians.isError ||
              rooms.isError) && (
              <p role="alert" className="text-sm text-destructive">
                Could not load appointment options. Close and reopen this form
                to retry.
              </p>
            )}
            {error && (
              <p role="alert" className="text-sm text-destructive">
                {error}
              </p>
            )}
            <DialogFooter>
              <Button
                type="submit"
                disabled={
                  createReservation.isPending ||
                  !selectedPatient?.id ||
                  organizations.isPending ||
                  departments.isPending ||
                  physicians.isPending
                }
              >
                Create appointment
              </Button>
            </DialogFooter>
          </form>
        )}
      </DialogContent>
    </Dialog>
  )
}
