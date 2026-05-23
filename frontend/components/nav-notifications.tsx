'use client'

import { BellIcon } from 'lucide-react'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

type Notification = {
  id: string
  avatar: string
  fallback: string
  text: string
  time: string
}

export function NotificationsPopover({
  notifications,
}: {
  notifications: Notification[]
}) {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <Button
            aria-label="Open notifications"
            className="rounded-full"
            size="icon"
            variant="ghost"
          />
        }
      >
        <BellIcon className="size-5" />
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align="end"
        className="w-80 max-w-[calc(100vw-2rem)]"
        side="bottom"
        sideOffset={8}
      >
        <DropdownMenuGroup>
          <DropdownMenuLabel>Notifications</DropdownMenuLabel>
          <DropdownMenuSeparator />
          {notifications.map(({ id, avatar, fallback, text, time }) => (
            <DropdownMenuItem className="flex items-start gap-3" key={id}>
              <Avatar className="size-8">
                <AvatarImage alt="Avatar" src={avatar} />
                <AvatarFallback>{fallback}</AvatarFallback>
              </Avatar>
              <div className="flex flex-col">
                <span className="font-medium text-sm">{text}</span>
                <span className="text-muted-foreground text-xs">{time}</span>
              </div>
            </DropdownMenuItem>
          ))}
        </DropdownMenuGroup>
        <DropdownMenuItem className="justify-center text-muted-foreground text-sm hover:text-primary">
          View all notifications
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
