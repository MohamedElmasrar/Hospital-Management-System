import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { formatRole, type Profile } from '@/lib/profile'

export function ProfileHeader({ profile }: { profile: Profile }) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex items-center gap-4">
        <Avatar className="size-16">
          <AvatarFallback className="text-lg">
            {profile.initials}
          </AvatarFallback>
        </Avatar>
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-semibold">{profile.displayName}</h1>
          <p className="text-sm text-muted-foreground">
            {profile.account.email}
          </p>
        </div>
      </div>
      <div className="flex gap-2">
        <Badge variant="outline">{formatRole(profile.account.role)}</Badge>
        <Badge variant="secondary">
          {profile.account.active ? 'Active' : 'Inactive'}
        </Badge>
      </div>
    </div>
  )
}
