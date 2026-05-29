export function ProfileDetail({
  label,
  value,
}: {
  label: string
  value?: React.ReactNode
}) {
  return (
    <div className="flex flex-col gap-1">
      <dt className="text-sm text-muted-foreground">{label}</dt>
      <dd className="text-sm font-medium">{value ?? 'Not provided'}</dd>
    </div>
  )
}

export function formatProfileDate(value: string | null | undefined) {
  if (!value) return 'Not provided'
  return new Intl.DateTimeFormat('en', { dateStyle: 'medium' }).format(
    new Date(value)
  )
}
