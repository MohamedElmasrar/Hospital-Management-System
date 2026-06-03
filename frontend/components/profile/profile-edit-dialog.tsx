'use client'

import {
  getGetMyProfile1QueryKey,
  useUpdateAddress,
  useUpdateEmployee,
} from '@/app/api/generated/hr/employee-controller/employee-controller'
import type { EmployeeProfile, Profile } from '@/lib/profile'
import {
  UpdateEmployeeRequestDTOGender,
  type UpdateEmployeeRequestDTO,
} from '@/app/api/generated/hr/model'
import {
  PostalAddressDTOAddressType,
  type PostalAddressDTO,
} from '@/app/api/generated/hr/model'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'

function dateInputValue(value: string | null | undefined) {
  return value ? value.slice(0, 10) : ''
}

function addressFormValue(employee: EmployeeProfile) {
  return {
    streetAddress: employee.postalAddress?.streetAddress ?? '',
    addressLocality: employee.postalAddress?.addressLocality ?? '',
    addressRegion: employee.postalAddress?.addressRegion ?? '',
    postalCode: employee.postalAddress?.postalCode ?? '',
    addressCountry: employee.postalAddress?.addressCountry ?? '',
  }
}

export function ProfileEditDialog({
  profile,
  open,
  onOpenChange,
}: {
  profile: Profile
  open: boolean
  onOpenChange: (open: boolean) => void
}) {
  const employee = profile.employee!
  const queryClient = useQueryClient()
  const updateEmployee = useUpdateEmployee()
  const updateAddress = useUpdateAddress()
  const [form, setForm] = useState(() => ({
    givenName: employee.givenName ?? '',
    additionalName: employee.additionalName ?? '',
    familyName: employee.familyName ?? '',
    honorificPrefix: employee.honorificPrefix ?? '',
    birthDate: dateInputValue(employee.birthDate),
    gender: employee.gender ?? '',
    ...addressFormValue(employee),
  }))
  const [error, setError] = useState<string>()

  const setField = (field: keyof typeof form, value: string) =>
    setForm((current) => ({ ...current, [field]: value }))

  async function save() {
    setError(undefined)
    const employeeData: UpdateEmployeeRequestDTO = {
      givenName: form.givenName.trim(),
      additionalName: form.additionalName.trim() || undefined,
      familyName: form.familyName.trim(),
      honorificPrefix: form.honorificPrefix.trim() || undefined,
      birthDate: form.birthDate || undefined,
      gender: form.gender
        ? (form.gender as UpdateEmployeeRequestDTO['gender'])
        : undefined,
      employmentStatus: employee.employmentStatus ?? undefined,
    }

    if (!employeeData.givenName || !employeeData.familyName) {
      setError('Given name and family name are required.')
      return
    }

    const addressValues = [
      form.streetAddress,
      form.addressLocality,
      form.addressRegion,
      form.postalCode,
      form.addressCountry,
    ]
    const hasAddress = addressValues.some((value) => value.trim())
    if (hasAddress && addressValues.some((value) => !value.trim())) {
      setError('Complete every address field or leave the address unchanged.')
      return
    }

    try {
      await updateEmployee.mutateAsync({ id: employee.id!, data: employeeData })
      if (hasAddress) {
        const address: PostalAddressDTO = {
          id: employee.postalAddress?.id ?? undefined,
          addressType:
            employee.postalAddress?.addressType ??
            PostalAddressDTOAddressType.Home,
          streetAddress: form.streetAddress.trim(),
          addressLocality: form.addressLocality.trim(),
          addressRegion: form.addressRegion.trim(),
          postalCode: form.postalCode.trim(),
          addressCountry: form.addressCountry.trim(),
        }
        await updateAddress.mutateAsync({ id: employee.id!, data: address })
      }
      await queryClient.invalidateQueries({
        queryKey: getGetMyProfile1QueryKey(),
      })
      onOpenChange(false)
    } catch {
      setError('We could not save your profile. Please try again.')
    }
  }

  const saving = updateEmployee.isPending || updateAddress.isPending

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Edit profile</DialogTitle>
          <DialogDescription>
            Update your personal information and postal address. Employment
            details are managed by hospital administrators.
          </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-2">
          <div className="grid gap-2 sm:grid-cols-2">
            <Field
              label="Given name"
              value={form.givenName}
              onChange={(value) => setField('givenName', value)}
            />
            <Field
              label="Family name"
              value={form.familyName}
              onChange={(value) => setField('familyName', value)}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Additional name"
              value={form.additionalName}
              onChange={(value) => setField('additionalName', value)}
            />
            <Field
              label="Honorific prefix"
              value={form.honorificPrefix}
              onChange={(value) => setField('honorificPrefix', value)}
            />
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field
              label="Date of birth"
              type="date"
              value={form.birthDate}
              onChange={(value) => setField('birthDate', value)}
            />
            <div className="grid gap-2">
              <Label htmlFor="profile-gender">Gender</Label>
              <select
                id="profile-gender"
                value={form.gender}
                onChange={(event) => setField('gender', event.target.value)}
                className="h-9 rounded-md border border-input bg-background px-3 text-sm"
              >
                <option value="">Not specified</option>
                {Object.values(UpdateEmployeeRequestDTOGender).map((gender) => (
                  <option key={gender} value={gender}>
                    {gender}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="border-t pt-4">
            <p className="mb-3 text-sm font-medium">Postal address</p>
            <div className="grid gap-4">
              <Field
                label="Street"
                value={form.streetAddress}
                onChange={(value) => setField('streetAddress', value)}
              />
              <div className="grid gap-4 sm:grid-cols-2">
                <Field
                  label="City"
                  value={form.addressLocality}
                  onChange={(value) => setField('addressLocality', value)}
                />
                <Field
                  label="Region"
                  value={form.addressRegion}
                  onChange={(value) => setField('addressRegion', value)}
                />
                <Field
                  label="Postal code"
                  value={form.postalCode}
                  onChange={(value) => setField('postalCode', value)}
                />
                <Field
                  label="Country"
                  value={form.addressCountry}
                  onChange={(value) => setField('addressCountry', value)}
                />
              </div>
            </div>
          </div>
          {error && <p className="text-sm text-destructive">{error}</p>}
        </div>
        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={saving}
          >
            Cancel
          </Button>
          <Button onClick={save} disabled={saving}>
            {saving ? 'Saving...' : 'Save changes'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

function Field({
  label,
  value,
  onChange,
  type = 'text',
}: {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
}) {
  const id = `profile-${label.toLowerCase().replaceAll(' ', '-')}`
  return (
    <div className="grid gap-2">
      <Label htmlFor={id}>{label}</Label>
      <Input
        id={id}
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
      />
    </div>
  )
}
