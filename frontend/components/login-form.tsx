'use client'
import { cn } from 'cn'

import { Button } from '@/components/ui/button'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { useState } from 'react'
import { Eye } from 'lucide-react'
import { useLogin } from '@/app/api/generated/auth/auth-controller/auth-controller'
import { LoginResponseDTO } from '@/app/api/generated/auth/model'
import { AxiosError } from 'axios'
import { useRouter, useSearchParams } from 'next/navigation'
import { useQueryClient } from '@tanstack/react-query'

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<'form'>) {
  const searchParams = useSearchParams()
  const router = useRouter()
  const queryClient = useQueryClient()

  const requestedCallback = searchParams.get('callBackUrl')
  const callBackUrl =
    requestedCallback?.startsWith('/') && !requestedCallback.startsWith('//')
      ? requestedCallback
      : '/dashboard'

  const [passwordVisible, setPasswordVisible] = useState(false)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { mutate: login, isPending } = useLogin()

  function handleLoginSubmit(e: React.SubmitEvent) {
    e.preventDefault()

    login(
      {
        data: {
          email: email,
          password: password,
        },
      },
      {
        onError: (err) => {
          const axiosError = err as AxiosError<{ error: string }>
          if (axiosError.response) {
            setError(axiosError.response.data?.error ?? 'Invalid credentials')
          } else {
            setError('Unexpected Error Try Again')
          }
        },
        onSuccess: (data: LoginResponseDTO) => {
          if (data.accessToken) {
            queryClient.clear()
            localStorage.setItem('token', data.accessToken)
            window.dispatchEvent(new Event('hms-auth'))
            router.push(callBackUrl)
          } else {
            setError('Something goes wrong! Try again')
          }
        },
      }
    )
  }

  return (
    <form
      className={cn('flex flex-col gap-6', className)}
      onSubmit={handleLoginSubmit}
      {...props}
    >
      <FieldGroup>
        <div className="flex flex-col items-center gap-1 text-center">
          <h1 className="text-2xl font-bold">Login to your account</h1>
          <p className="text-sm text-balance text-muted-foreground">
            Enter your email and password below to login to your account
          </p>
        </div>
        <Field>
          <div>{error && <div className="text-red-500">{error}</div>}</div>
        </Field>
        <Field>
          <FieldLabel htmlFor="email" aria-invalid={error !== ''}>
            Email
          </FieldLabel>
          <Input
            id="email"
            type="email"
            placeholder="m@example.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            aria-invalid={error !== ''}
            required
          />
        </Field>
        <Field>
          <div className="flex items-center">
            <FieldLabel htmlFor="password" aria-invalid={error !== ''}>
              Password
            </FieldLabel>
          </div>
          <div className="relative">
            <Input
              id="password"
              type={passwordVisible ? 'text' : 'password'}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              aria-invalid={error !== ''}
              required
            />
            <button
              className="absolute right-3 top-1/2 transform -translate-y-1/2"
              onClick={() => setPasswordVisible(!passwordVisible)}
              type="button"
            >
              <Eye />
            </button>
          </div>
        </Field>
        <Field>
          <Button type="submit" disabled={isPending}>
            Login
          </Button>
        </Field>
      </FieldGroup>
    </form>
  )
}
