'use client'

import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

export default function ProfileError({ retry }: { retry: () => void }) {
  return (
    <Card className="max-w-xl">
      <CardHeader>
        <CardTitle>Unable to load your profile</CardTitle>
        <CardDescription>
          Your personal information could not be retrieved. Please try again.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Button variant="outline" onClick={retry}>
          Try again
        </Button>
      </CardContent>
    </Card>
  )
}
