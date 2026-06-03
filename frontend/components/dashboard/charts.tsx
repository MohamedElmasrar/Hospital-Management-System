'use client'

import type { ReactNode } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  XAxis,
  YAxis,
} from 'recharts'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Empty, EmptyHeader, EmptyTitle } from '@/components/ui/empty'
import { Alert, AlertDescription } from '@/components/ui/alert'

const chartConfig = {
  count: { label: 'Records', color: 'var(--chart-2)' },
} satisfies ChartConfig

export function titleCase(value?: string) {
  return (value || 'Unspecified')
    .toLowerCase()
    .replaceAll('_', ' ')
    .replace(/\b\w/g, (letter) => letter.toUpperCase())
}

export function ChartCard({
  title,
  description,
  values,
  isPending,
  isError,
  retry,
}: {
  title: string
  description: string
  values: (string | undefined)[]
  isPending: boolean
  isError: boolean
  retry: () => void
}) {
  const counts = values.reduce<Record<string, number>>((result, value) => {
    const name = titleCase(value)
    result[name] = (result[name] ?? 0) + 1
    return result
  }, {})
  const data = Object.entries(counts)
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)

  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent>
        {isPending ? (
          <Skeleton className="h-56 w-full" />
        ) : isError ? (
          <div className="flex h-56 flex-col items-center justify-center gap-3 text-sm text-muted-foreground">
            <Alert variant="destructive">
              <AlertDescription>Could not load this chart.</AlertDescription>
            </Alert>
            <Button variant="outline" size="sm" onClick={retry}>
              Retry
            </Button>
          </div>
        ) : data.length === 0 ? (
          <Empty className="h-56">
            <EmptyHeader>
              <EmptyTitle>No records in this period</EmptyTitle>
            </EmptyHeader>
          </Empty>
        ) : (
          <ChartContainer config={chartConfig} className="h-56 w-full">
            <BarChart
              accessibilityLayer
              data={data}
              layout="vertical"
              margin={{ left: 4, right: 16 }}
            >
              <CartesianGrid horizontal={false} />
              <XAxis
                type="number"
                allowDecimals={false}
                tickLine={false}
                axisLine={false}
              />
              <YAxis
                type="category"
                dataKey="name"
                width={112}
                tickLine={false}
                axisLine={false}
              />
              <ChartTooltip content={<ChartTooltipContent />} />
              <Bar dataKey="count" fill="var(--color-count)" radius={4} />
            </BarChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}

export function ChartGrid({ children }: { children: ReactNode }) {
  return <div className="grid gap-4 lg:grid-cols-2">{children}</div>
}

export function TrendChart({
  title,
  description,
  data,
  label,
  currency,
  isPending,
  isError,
  retry,
  children,
}: {
  title: string
  description: string
  data: { name: string; value: number }[]
  label: string
  currency?: string
  isPending: boolean
  isError: boolean
  retry: () => void
  children?: ReactNode
}) {
  const config = {
    value: { label, color: 'var(--chart-2)' },
  } satisfies ChartConfig
  const formatValue = (value: number) =>
    new Intl.NumberFormat(
      'en',
      currency ? { style: 'currency', currency } : { maximumFractionDigits: 2 }
    ).format(value)
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-3">
        {children}
        {isPending ? (
          <Skeleton className="h-56 w-full" />
        ) : isError ? (
          <div className="flex h-56 flex-col items-center justify-center gap-3 text-sm text-muted-foreground">
            <Alert variant="destructive">
              <AlertDescription>Could not load this chart.</AlertDescription>
            </Alert>
            <Button variant="outline" onClick={retry}>
              Try again
            </Button>
          </div>
        ) : !data.length ||
          (!currency && data.every((point) => point.value === 0)) ? (
          <Empty className="h-56">
            <EmptyHeader>
              <EmptyTitle>No records in this period</EmptyTitle>
            </EmptyHeader>
          </Empty>
        ) : (
          <ChartContainer config={config} className="h-56 w-full">
            <LineChart
              accessibilityLayer
              data={data}
              margin={{ left: 8, right: 16 }}
            >
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="name"
                tickLine={false}
                axisLine={false}
                minTickGap={24}
              />
              <YAxis
                tickLine={false}
                axisLine={false}
                allowDecimals={!!currency}
                width={64}
                tickFormatter={(value) =>
                  new Intl.NumberFormat('en', { notation: 'compact' }).format(
                    Number(value)
                  )
                }
              />
              <ChartTooltip
                content={
                  <ChartTooltipContent
                    formatter={(value) => (
                      <span>
                        {label}: {formatValue(Number(value))}
                      </span>
                    )}
                  />
                }
              />
              <Line
                type="linear"
                dataKey="value"
                stroke="var(--color-value)"
                strokeWidth={2}
                dot={{ r: 3 }}
              />
            </LineChart>
          </ChartContainer>
        )}
      </CardContent>
    </Card>
  )
}
