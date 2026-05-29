import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import type { PatientProfile, Profile } from '@/lib/profile'
import { ProfileCard } from './profile-card'
import { ProfileDetail, formatProfileDate } from './profile-detail'

export function PatientProfileTabs({
  profile,
  patient,
}: {
  profile: Profile
  patient: PatientProfile
}) {
  return (
    <Tabs defaultValue="personal">
      <TabsList>
        <TabsTrigger value="personal">Personal</TabsTrigger>
        <TabsTrigger value="medical">Medical</TabsTrigger>
        <TabsTrigger value="emergency">Emergency contact</TabsTrigger>
      </TabsList>

      <TabsContent value="personal">
        <ProfileCard
          title="Personal information"
          description="Your identity and contact details."
        >
          <ProfileDetail label="Full name" value={profile.displayName} />
          <ProfileDetail label="Email" value={profile.account.email} />
          <ProfileDetail
            label="Date of birth"
            value={formatProfileDate(patient.dateOfBirth)}
          />
          <ProfileDetail label="Gender" value={patient.gender} />
          <ProfileDetail label="Phone" value={patient.phone} />
          <ProfileDetail label="National ID" value={patient.nationalId} />
        </ProfileCard>
      </TabsContent>

      <TabsContent value="medical">
        <ProfileCard
          title="Medical information"
          description="The key details from your patient record."
        >
          <ProfileDetail label="Medical record number" value={patient.mrn} />
          <ProfileDetail label="Blood group" value={patient.bloodGroup} />
          <ProfileDetail
            label="Allergies"
            value={patient.allergies || 'None recorded'}
          />
          <ProfileDetail
            label="Chronic conditions"
            value={patient.chronicConditions || 'None recorded'}
          />
        </ProfileCard>
      </TabsContent>

      <TabsContent value="emergency">
        <ProfileCard
          title="Emergency contact"
          description="The person to contact in an emergency."
        >
          <ProfileDetail label="Name" value={patient.emergencyContactName} />
          <ProfileDetail label="Phone" value={patient.emergencyContactPhone} />
        </ProfileCard>
      </TabsContent>
    </Tabs>
  )
}
