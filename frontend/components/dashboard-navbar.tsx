'use client'

import { ChevronDownIcon } from 'lucide-react'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from '@/components/ui/breadcrumb'
import { Separator } from '@/components/ui/separator'
import { SidebarTrigger } from '@/components/ui/sidebar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { NotificationsPopover } from './nav-notifications'
import {
  clearAuth,
  getAuthUser,
  getServerTokenSnapshot,
  getTokenSnapshot,
  subscribeAuth,
} from '@/lib/auth'
import { usePathname, useRouter } from 'next/navigation'
import { useSyncExternalStore } from 'react'
import { useProfile } from '@/lib/profile'
import { useQueryClient } from '@tanstack/react-query'
import { useLogout } from '@/app/api/generated/auth/auth-controller/auth-controller'

const sampleNotifications = [
  {
    id: '1',
    avatar: '/avatars/01.png',
    fallback: 'OM',
    text: 'New appointment scheduled.',
    time: '10m ago',
  },
  {
    id: '2',
    avatar: '/avatars/02.png',
    fallback: 'JL',
    text: 'Patient record updated.',
    time: '1h ago',
  },
  {
    id: '3',
    avatar: '/avatars/03.png',
    fallback: 'HH',
    text: 'New staff member added.',
    time: '2h ago',
  },
]

export function DashboardNavbar() {
  const pathname = usePathname()
  const router = useRouter()
  const queryClient = useQueryClient()
  const { mutate: logout } = useLogout()
  const token = useSyncExternalStore(
    subscribeAuth,
    getTokenSnapshot,
    getServerTokenSnapshot
  )
  const user = token ? getAuthUser() : null
  const { data: profile } = useProfile()
  const displayName = profile?.displayName ?? user?.email ?? 'Signed-in user'
  const initials = profile?.initials ?? user?.role.slice(0, 2) ?? 'U'

  const pageName =
    pathname === '/dashboard'
      ? 'Dashboard'
      : (pathname
          .split('/')
          .filter(Boolean)
          .at(-1)
          ?.split('-')
          .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
          .join(' ') ?? 'Dashboard')

  function signOut() {
    logout(undefined, {
      onSettled: () => {
        clearAuth()
        queryClient.clear()
        router.replace('/login')
      },
    })
  }

  return (
    <header className="flex h-16 shrink-0 items-center justify-between gap-2 border-b px-4">
      <div className="flex min-w-0 items-center gap-2">
        <SidebarTrigger className="-ml-1" />
        <Separator className="mr-2 h-4" orientation="vertical" />
        <Breadcrumb>
          <BreadcrumbList>
            <BreadcrumbItem className="hidden md:inline-flex">
              <BreadcrumbLink href="#">Hospital Management</BreadcrumbLink>
            </BreadcrumbItem>
            <BreadcrumbSeparator className="hidden md:inline-flex" />
            <BreadcrumbItem>
              <BreadcrumbPage>{pageName}</BreadcrumbPage>
            </BreadcrumbItem>
          </BreadcrumbList>
        </Breadcrumb>
      </div>

      <div className="flex items-center gap-2">
        <NotificationsPopover notifications={sampleNotifications} />
        <DropdownMenu>
          <DropdownMenuTrigger
            aria-label="Open profile menu"
            render={
              <button className="flex items-center gap-2 rounded-md p-1.5 text-sm transition-colors hover:bg-accent" />
            }
          >
            <Avatar className="size-8">
              <AvatarFallback>{initials}</AvatarFallback>
            </Avatar>
            <span className="hidden font-medium sm:inline">{displayName}</span>
            <ChevronDownIcon className="hidden sm:inline" />
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuGroup>
              <DropdownMenuLabel>{displayName}</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={() => router.push('/dashboard/profile')}
              >
                Profile
              </DropdownMenuItem>
              <DropdownMenuItem>Settings</DropdownMenuItem>
              <DropdownMenuItem onClick={signOut}>Sign out</DropdownMenuItem>
            </DropdownMenuGroup>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
