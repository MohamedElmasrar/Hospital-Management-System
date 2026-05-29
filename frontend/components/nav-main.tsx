'use client'

import { ChevronDown, ChevronUp } from 'lucide-react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import type React from 'react'
import { useState } from 'react'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarMenuSubButton,
  SidebarMenuItem as SidebarMenuSubItem,
  useSidebar,
} from '@/components/ui/sidebar'
import { cn } from '@/lib/utils'

export type Route = {
  id: string
  title: string
  icon?: React.ReactNode
  link: string
  subs?: {
    title: string
    link: string
    icon?: React.ReactNode
  }[]
}

export default function DashboardNavigation({ routes }: { routes: Route[] }) {
  const pathname = usePathname()
  const { state } = useSidebar()
  const isCollapsed = state === 'collapsed'
  const [openCollapsible, setOpenCollapsible] = useState<string | null>(null)

  return (
    <SidebarMenu>
      {routes.map((route) => {
        const isRouteActive =
          pathname === route.link ||
          (route.link !== '/dashboard' &&
            pathname.startsWith(`${route.link}/`)) ||
          route.subs?.some(
            (subRoute) =>
              pathname === subRoute.link ||
              pathname.startsWith(`${subRoute.link}/`)
          ) === true
        const isOpen = !isCollapsed && openCollapsible === route.id
        const hasSubRoutes = !!route.subs?.length

        return (
          <SidebarMenuItem key={route.id}>
            {hasSubRoutes ? (
              <Collapsible
                className="w-full"
                onOpenChange={(open) =>
                  setOpenCollapsible(open ? route.id : null)
                }
                open={isOpen}
              >
                <CollapsibleTrigger
                  render={
                    <SidebarMenuButton
                      isActive={isRouteActive}
                      className={cn(
                        'flex w-full items-center rounded-lg px-2 transition-colors',
                        isCollapsed && 'justify-center'
                      )}
                    />
                  }
                >
                  {route.icon}
                  {!isCollapsed && (
                    <span className="ml-2 flex-1 font-medium text-sm">
                      {route.title}
                    </span>
                  )}
                  {!isCollapsed && hasSubRoutes && (
                    <span className="ml-auto">
                      {isOpen ? (
                        <ChevronUp className="size-4" />
                      ) : (
                        <ChevronDown className="size-4" />
                      )}
                    </span>
                  )}
                </CollapsibleTrigger>

                {!isCollapsed && (
                  <CollapsibleContent>
                    <SidebarMenuSub className="my-1 ml-3.5 ">
                      {route.subs?.map((subRoute) => (
                        <SidebarMenuSubItem
                          className="h-auto"
                          key={`${route.id}-${subRoute.title}`}
                        >
                          <SidebarMenuSubButton
                            isActive={
                              pathname === subRoute.link ||
                              pathname.startsWith(`${subRoute.link}/`)
                            }
                            render={
                              <Link
                                className="flex items-center rounded-md px-4 py-1.5 font-medium text-sm"
                                href={subRoute.link}
                                prefetch={true}
                              />
                            }
                          >
                            {subRoute.title}
                          </SidebarMenuSubButton>
                        </SidebarMenuSubItem>
                      ))}
                    </SidebarMenuSub>
                  </CollapsibleContent>
                )}
              </Collapsible>
            ) : (
              <SidebarMenuButton
                isActive={isRouteActive}
                render={
                  <Link
                    className={cn(
                      'flex items-center rounded-lg px-2 transition-colors',
                      isCollapsed && 'justify-center'
                    )}
                    href={route.link}
                    prefetch={true}
                  />
                }
                tooltip={route.title}
              >
                {route.icon}
                {!isCollapsed && (
                  <span className="ml-2 font-medium text-sm">
                    {route.title}
                  </span>
                )}
              </SidebarMenuButton>
            )}
          </SidebarMenuItem>
        )
      })}
    </SidebarMenu>
  )
}
