'use client'

import { useState, useSyncExternalStore } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { BellIcon } from 'lucide-react'
import {
  getGetMyUnreadNotificationCountQueryKey,
  getListMyNotificationsQueryKey,
  useGetMyUnreadNotificationCount,
  useListMyNotifications,
  useMarkAllMyNotificationsRead,
  useMarkMyNotificationRead,
} from '@/app/api/generated/notification/notifications/notifications'
import {
  getAuthUser,
  getServerTokenSnapshot,
  getTokenSnapshot,
  subscribeAuth,
} from '@/lib/auth'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyTitle,
} from '@/components/ui/empty'
import { Skeleton } from '@/components/ui/skeleton'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

export function NotificationsPopover() {
  const token = useSyncExternalStore(
    subscribeAuth,
    getTokenSnapshot,
    getServerTokenSnapshot
  )
  const user = token ? getAuthUser() : null
  if (!user)
    return (
      <Button aria-label="Notifications" size="icon" variant="ghost" disabled>
        <BellIcon />
      </Button>
    )
  return <NotificationInbox key={user.id} userId={user.id} />
}

function NotificationInbox({ userId }: { userId: string }) {
  const [open, setOpen] = useState(false)
  const [page, setPage] = useState(0)
  const queryClient = useQueryClient()
  const params = { page, size: 10 }
  const inbox = useListMyNotifications(params, {
    query: {
      queryKey: [...getListMyNotificationsQueryKey(params), userId],
      enabled: open,
      refetchInterval: 30_000,
    },
  })
  const unread = useGetMyUnreadNotificationCount({
    query: {
      queryKey: [...getGetMyUnreadNotificationCountQueryKey(), userId],
      refetchInterval: 30_000,
    },
  })
  const markRead = useMarkMyNotificationRead()
  const markAllRead = useMarkAllMyNotificationsRead()
  const busy = markRead.isPending || markAllRead.isPending
  const count = unread.data?.count ?? 0

  async function refresh() {
    await Promise.all([
      queryClient.invalidateQueries({
        queryKey: getListMyNotificationsQueryKey(),
      }),
      queryClient.invalidateQueries({
        queryKey: getGetMyUnreadNotificationCountQueryKey(),
      }),
    ])
  }

  function read(id: string) {
    markAllRead.reset()
    markRead.mutate({ id }, { onSuccess: refresh })
  }

  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger
        render={
          <Button
            aria-label={`Notifications, ${count} unread`}
            variant="ghost"
            size="sm"
          />
        }
      >
        <BellIcon data-icon="inline-start" />
        {count > 0 && (
          <Badge variant="secondary">{count > 99 ? '99+' : count}</Badge>
        )}
      </DropdownMenuTrigger>
      <DropdownMenuContent
        align="end"
        className="w-96 max-w-[calc(100vw-2rem)]"
        sideOffset={8}
      >
        <DropdownMenuGroup>
          <DropdownMenuLabel>Notifications</DropdownMenuLabel>
          <DropdownMenuItem
            closeOnClick={false}
            disabled={busy || count === 0}
            onClick={() => {
              markRead.reset()
              markAllRead.mutate(undefined, { onSuccess: refresh })
            }}
          >
            Mark all as read
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          {(markRead.isError || markAllRead.isError) && (
            <Alert variant="destructive">
              <AlertDescription>
                Could not update notifications. Please try again.
              </AlertDescription>
            </Alert>
          )}
          {inbox.isPending ? (
            <div className="flex flex-col gap-3 p-3">
              <Skeleton className="h-14 w-full" />
              <Skeleton className="h-14 w-full" />
            </div>
          ) : inbox.isError || unread.isError ? (
            <>
              <Alert variant="destructive">
                <AlertDescription>
                  Could not load notifications.
                </AlertDescription>
              </Alert>
              <DropdownMenuItem
                closeOnClick={false}
                onClick={() => {
                  void inbox.refetch()
                  void unread.refetch()
                }}
              >
                Try again
              </DropdownMenuItem>
            </>
          ) : !inbox.data?.content?.length ? (
            <Empty>
              <EmptyHeader>
                <EmptyTitle>No notifications</EmptyTitle>
                <EmptyDescription>
                  Your account and work updates will appear here.
                </EmptyDescription>
              </EmptyHeader>
            </Empty>
          ) : (
            inbox.data.content.map((notification) => (
              <DropdownMenuItem
                key={notification.id}
                closeOnClick={false}
                disabled={busy}
                className="items-start py-3"
                onClick={() => {
                  if (!notification.readAt && notification.id)
                    read(notification.id)
                }}
              >
                <div className="flex min-w-0 flex-1 flex-col gap-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-medium">{notification.title}</span>
                    {!notification.readAt && (
                      <Badge variant="secondary">Unread</Badge>
                    )}
                  </div>
                  <span className="whitespace-normal text-muted-foreground">
                    {notification.message}
                  </span>
                  <span className="text-xs text-muted-foreground">
                    {notification.createdAt?.replace('T', ' ').slice(0, 16)} UTC
                  </span>
                </div>
              </DropdownMenuItem>
            ))
          )}
          {(inbox.data?.totalPages ?? 0) > 1 && (
            <>
              <DropdownMenuSeparator />
              <DropdownMenuLabel>
                Page {page + 1} of {inbox.data?.totalPages}
              </DropdownMenuLabel>
              <DropdownMenuItem
                closeOnClick={false}
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
              >
                Previous page
              </DropdownMenuItem>
              <DropdownMenuItem
                closeOnClick={false}
                disabled={page + 1 >= (inbox.data?.totalPages ?? 0)}
                onClick={() => setPage(page + 1)}
              >
                Next page
              </DropdownMenuItem>
            </>
          )}
        </DropdownMenuGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
