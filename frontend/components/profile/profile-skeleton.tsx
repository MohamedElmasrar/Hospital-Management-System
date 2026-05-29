import { Skeleton } from '@/components/ui/skeleton'

export function ProfileSkeleton() {
  return (
    <div className="mx-auto flex w-full max-w-4xl flex-col gap-6">
      <div className="flex items-center gap-4">
        <Skeleton className="size-16 rounded-full" />
        <div className="flex flex-col gap-2">
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-64" />
        </div>
      </div>
      <Skeleton className="h-8 w-72" />
      <Skeleton className="h-72" />
    </div>
  )
}
