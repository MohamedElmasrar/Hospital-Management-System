import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { formatRole, type Profile } from '@/lib/profile'
import { Pencil } from 'lucide-react'

export function ProfileHeader({
  profile,
  onEdit,
}: {
  profile: Profile
  onEdit: () => void
}) {
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
        <Button variant="outline" size="sm" onClick={onEdit}>
          <Pencil />
          Edit profile
        </Button>
        <Badge variant="outline">{formatRole(profile.account.role)}</Badge>
        <Badge variant="secondary">
          {profile.account.active ? 'Active' : 'Inactive'}
        </Badge>
      </div>
    </div>
  )
}
