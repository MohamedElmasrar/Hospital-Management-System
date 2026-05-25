import { DashboardSidebar } from '@/components/app-sidebar'
import { DashboardNavbar } from '@/components/dashboard-navbar'
import { DashboardAuthGuard } from '@/components/dashboard-auth-guard'
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar'
import React from 'react'
import { Suspense } from 'react'

export default function dashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <Suspense fallback={null}>
      <DashboardAuthGuard>
        <SidebarProvider>
          <DashboardSidebar />
          <SidebarInset>
            <DashboardNavbar />
            <div className="flex flex-1 flex-col p-4">{children}</div>
          </SidebarInset>
        </SidebarProvider>
      </DashboardAuthGuard>
    </Suspense>
  )
}
