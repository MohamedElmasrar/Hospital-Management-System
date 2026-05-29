import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import type { EmployeeProfile, PhysicianProfile, Profile } from '@/lib/profile'
import { ProfileCard } from './profile-card'
import { ProfileDetail, formatProfileDate } from './profile-detail'

export function EmployeeProfileTabs({
  profile,
  employee,
  physician,
}: {
  profile: Profile
  employee: EmployeeProfile
  physician: PhysicianProfile | null
}) {
  const assignment = employee.assignments?.find((item) => !item.endDate)
  const preferredContact = employee.contactPoints?.find(
    (item) => item.isPreferred
  )
  const primarySpecialty =
    physician?.specialties?.find((item) => item.isPrimary) ??
    physician?.specialties?.[0]

  return (
    <Tabs defaultValue="personal">
      <TabsList>
        <TabsTrigger value="personal">Personal</TabsTrigger>
        <TabsTrigger value="work">Work</TabsTrigger>
        <TabsTrigger value="contact">Contact</TabsTrigger>
      </TabsList>

      <TabsContent value="personal">
        <ProfileCard
          title="Personal information"
          description="Your identity and account details."
        >
          <ProfileDetail label="Full name" value={profile.displayName} />
          <ProfileDetail label="Email" value={profile.account.email} />
          <ProfileDetail
            label="Date of birth"
            value={formatProfileDate(employee.birthDate)}
          />
          <ProfileDetail label="Gender" value={employee.gender} />
          <ProfileDetail label="Phone" value={preferredContact?.telephone} />
          <ProfileDetail
            label="Account status"
            value={profile.account.active ? 'Active' : 'Inactive'}
          />
        </ProfileCard>
      </TabsContent>

      <TabsContent value="work">
        <ProfileCard
          title="Work information"
          description="Your current hospital assignment."
        >
          <ProfileDetail label="Employee ID" value={employee.identifier} />
          <ProfileDetail label="Status" value={employee.employmentStatus} />
          <ProfileDetail label="Job title" value={assignment?.jobTitle} />
          <ProfileDetail
            label="Department"
            value={assignment?.departmentName}
          />
          <ProfileDetail
            label="License number"
            value={physician?.licenseNumber}
          />
          <ProfileDetail
            label="Specialty"
            value={primarySpecialty?.specialtyName}
          />
        </ProfileCard>
      </TabsContent>

      <TabsContent value="contact">
        <ProfileCard
          title="Contact and address"
          description="Your preferred contact details and postal address."
        >
          <ProfileDetail label="Email" value={preferredContact?.email} />
          <ProfileDetail label="Phone" value={preferredContact?.telephone} />
          <ProfileDetail
            label="Street"
            value={employee.postalAddress?.streetAddress}
          />
          <ProfileDetail
            label="City"
            value={employee.postalAddress?.addressLocality}
          />
          <ProfileDetail
            label="Region"
            value={employee.postalAddress?.addressRegion}
          />
          <ProfileDetail
            label="Postal code"
            value={employee.postalAddress?.postalCode}
          />
          <ProfileDetail
            label="Country"
            value={employee.postalAddress?.addressCountry}
          />
        </ProfileCard>
      </TabsContent>
    </Tabs>
  )
}
