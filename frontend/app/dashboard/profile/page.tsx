import { ProfileContent } from '@/components/profile/profile-content'
import { ProfileSkeleton } from '@/components/profile/profile-skeleton'
import { Suspense } from 'react'

export default function ProfileRoute() {
  return (
    <Suspense fallback={<ProfileSkeleton />}>
      <ProfileContent />
    </Suspense>
  )
}
