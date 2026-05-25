const pageTitles: Record<string, string> = {
  'reception/patients': 'Patients',
  'reception/appointments': 'Appointments',
  'reception/queue': 'Waiting Queue',
  'reception/admissions': 'Admissions',
  'billing/invoices': 'Invoices',
  'billing/payments': 'Payments',
  'billing/insurance': 'Insurance',
  'billing/reports': 'Billing Reports',
  'hr/employees': 'Employees',
  'hr/departments': 'Departments',
  'hr/schedules': 'Schedules',
  'hr/leave': 'Leave Requests',
  'hr/attendance': 'Attendance',
  'hr/payroll': 'Payroll',
  'clinical/schedule': 'My Schedule',
  'clinical/patients': 'Patients',
  'clinical/consultations': 'Consultations',
  'patient/appointments': 'My Appointments',
  'patient/records': 'My Records',
  'patient/billing': 'My Billing',
  users: 'Users',
  'audit-logs': 'Audit Logs',
}

export default async function DashboardSectionPage({
  params,
}: {
  params: Promise<{ segments: string[] }>
}) {
  const { segments } = await params
  const key = segments.join('/')
  return <h1 className="text-2xl font-semibold">{pageTitles[key] ?? 'Page'}</h1>
}
