import { DashboardSidebar } from '@/components/app-sidebar'
import { DashboardNavbar } from '@/components/dashboard-navbar'
import { SidebarInset, SidebarProvider } from '@/components/ui/sidebar'
import type { ReactNode } from 'react'

export default function DashboardLayout({ children }: { children: ReactNode }) {
  return (
    <SidebarProvider>
      <DashboardSidebar />
      <SidebarInset>
        <DashboardNavbar />
        <div className="flex flex-1 flex-col p-4">{children}</div>
      </SidebarInset>
    </SidebarProvider>
  )
}
