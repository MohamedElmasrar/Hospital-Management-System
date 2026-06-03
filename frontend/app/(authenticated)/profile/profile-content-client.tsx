'use client'

import dynamic from 'next/dynamic'
import { ProfileSkeleton } from '@/components/profile/profile-skeleton'

const ProfileContent = dynamic(
  () =>
    import('@/components/profile/profile-content').then(
      (module) => module.ProfileContent
    ),
  { ssr: false, loading: () => <ProfileSkeleton /> }
)

export function ProfileContentClient() {
  return <ProfileContent />
}
