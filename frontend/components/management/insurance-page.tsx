'use client'

import {
  getGetPoliciesByPatientQueryKey,
  getListClaimsQueryKey,
  getListProvidersQueryKey,
  useCreatePolicy,
  useCreateProvider,
  useGetPoliciesByPatient,
  useListClaims,
  useListProviders,
  useSubmitClaim,
  useUpdateClaimStatus,
} from '@/app/api/generated/billing/insurance-claims/insurance-claims'
import { getListInvoicesQueryKey } from '@/app/api/generated/billing/invoices/invoices'
import { getUnpaidInvoicesQueryKey } from '@/app/api/generated/billing/billing-report-controller/billing-report-controller'
import type {
  InsuranceClaimResponseDTO,
  ListClaimsStatus,
  UpdateClaimStatusRequestDTOStatus,
} from '@/app/api/generated/billing/model'
import { useRole } from '@/components/hr/shared'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Field, FieldGroup, FieldLabel } from '@/components/ui/field'
import { Input } from '@/components/ui/input'
import { NativeSelect, NativeSelectOption } from '@/components/ui/native-select'
import { TableCell, TableRow } from '@/components/ui/table'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import {
  DataCard,
  displayDate,
  displayMoney,
  FormError,
  PAGE_SIZE,
  PageHeading,
} from './shared'

const claimStatuses = ['SUBMITTED', 'APPROVED', 'REJECTED', 'SETTLED'] as const

export function InsurancePageContent() {
  const role = useRole()
  const queryClient = useQueryClient()
  const [tab, setTab] = useState('claims')
  const [claimStatus, setClaimStatus] = useState<ListClaimsStatus>('SUBMITTED')
  const [claimPage, setClaimPage] = useState(0)
  const [providerPage, setProviderPage] = useState(0)
  const [policyPage, setPolicyPage] = useState(0)
  const [patientInput, setPatientInput] = useState('')
  const [patientId, setPatientId] = useState('')
  const [dialog, setDialog] = useState<'provider' | 'policy' | 'claim' | null>(
    null
  )
  const [error, setError] = useState(false)
  const [providerName, setProviderName] = useState('')
  const [providerCode, setProviderCode] = useState('')
  const [policyNumber, setPolicyNumber] = useState('')
  const [providerId, setProviderId] = useState('')
  const [policyPatientId, setPolicyPatientId] = useState('')
  const [coverage, setCoverage] = useState('')
  const [claimInvoiceId, setClaimInvoiceId] = useState('')
  const [claimPolicyId, setClaimPolicyId] = useState('')
  const [claimAmount, setClaimAmount] = useState('')
  const [review, setReview] = useState<InsuranceClaimResponseDTO | null>(null)
  const [decision, setDecision] =
    useState<UpdateClaimStatusRequestDTOStatus>('APPROVED')
  const [approvedAmount, setApprovedAmount] = useState('')
  const providers = useListProviders()
  const policies = useGetPoliciesByPatient(patientId, {
    query: { enabled: !!patientId },
  })
  const claims = useListClaims({
    status: claimStatus,
    page: claimPage,
    size: PAGE_SIZE,
  })
  const createProvider = useCreateProvider()
  const createPolicy = useCreatePolicy()
  const submitClaim = useSubmitClaim()
  const updateClaim = useUpdateClaimStatus()
  const providerRows = (providers.data ?? []).slice(
    providerPage * PAGE_SIZE,
    (providerPage + 1) * PAGE_SIZE
  )
  const policyRows = (policies.data ?? []).slice(
    policyPage * PAGE_SIZE,
    (policyPage + 1) * PAGE_SIZE
  )

  async function saveProvider() {
    setError(false)
    try {
      await createProvider.mutateAsync({
        data: { name: providerName.trim(), code: providerCode.trim() },
      })
      await queryClient.invalidateQueries({
        queryKey: getListProvidersQueryKey(),
      })
      setDialog(null)
      setProviderName('')
      setProviderCode('')
    } catch {
      setError(true)
    }
  }

  async function savePolicy() {
    setError(false)
    try {
      await createPolicy.mutateAsync({
        data: {
          policyNumber: policyNumber.trim(),
          insuranceProviderId: providerId,
          patientId: policyPatientId.trim(),
          coverageRate: Number(coverage) / 100,
        },
      })
      if (patientId)
        await queryClient.invalidateQueries({
          queryKey: getGetPoliciesByPatientQueryKey(patientId),
        })
      setDialog(null)
      setPolicyNumber('')
      setPolicyPatientId('')
      setCoverage('')
    } catch {
      setError(true)
    }
  }

  async function saveClaim() {
    setError(false)
    try {
      await submitClaim.mutateAsync({
        data: {
          invoiceId: claimInvoiceId.trim(),
          policyId: claimPolicyId.trim(),
          claimedAmount: Number(claimAmount),
        },
      })
      await queryClient.invalidateQueries({ queryKey: getListClaimsQueryKey() })
      setDialog(null)
      setClaimInvoiceId('')
      setClaimPolicyId('')
      setClaimAmount('')
    } catch {
      setError(true)
    }
  }

  async function saveDecision() {
    if (!review?.id) return
    setError(false)
    try {
      await updateClaim.mutateAsync({
        id: review.id,
        data: {
          status: decision,
          approvedAmount:
            decision === 'REJECTED'
              ? undefined
              : approvedAmount
                ? Number(approvedAmount)
                : undefined,
        },
      })
      await queryClient.invalidateQueries({ queryKey: getListClaimsQueryKey() })
      setReview(null)
      await queryClient.invalidateQueries({
        queryKey: getListInvoicesQueryKey(),
      })
      await queryClient.invalidateQueries({
        queryKey: getUnpaidInvoicesQueryKey(),
      })
    } catch {
      setError(true)
    }
  }

  return (
    <div className="flex flex-col gap-6">
      <PageHeading
        title="Insurance"
        description="Review claims, providers, and patient policies."
      />
      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="claims">Claims</TabsTrigger>
          <TabsTrigger value="providers">Providers</TabsTrigger>
          <TabsTrigger value="policies">Policies</TabsTrigger>
        </TabsList>
        <TabsContent value="claims" className="flex flex-col gap-4">
          <div className="flex justify-end">
            <Button
              onClick={() => {
                setError(false)
                setDialog('claim')
              }}
            >
              Submit claim
            </Button>
          </div>
          <DataCard
            title="Insurance claims"
            description={String(claims.data?.totalElements ?? 0) + ' claims'}
            headers={[
              'Invoice',
              'Policy',
              'Provider',
              'Claimed',
              'Approved',
              'Submitted',
              '',
            ]}
            empty={!claims.data?.content?.length}
            pending={claims.isPending}
            error={claims.isError}
            retry={() => void claims.refetch()}
            page={claimPage}
            totalPages={claims.data?.totalPages ?? 0}
            previous={() => setClaimPage((value) => value - 1)}
            next={() => setClaimPage((value) => value + 1)}
            filters={
              <Field className="max-w-48">
                <FieldLabel htmlFor="claim-status">Status</FieldLabel>
                <NativeSelect
                  id="claim-status"
                  value={claimStatus}
                  onChange={(event) => {
                    setClaimStatus(event.target.value as ListClaimsStatus)
                    setClaimPage(0)
                  }}
                >
                  {claimStatuses.map((value) => (
                    <NativeSelectOption key={value} value={value}>
                      {value}
                    </NativeSelectOption>
                  ))}
                </NativeSelect>
              </Field>
            }
          >
            {claims.data?.content?.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.invoiceNumber ?? '—'}</TableCell>
                <TableCell>{item.policyNumber ?? '—'}</TableCell>
                <TableCell>{item.insuranceProviderName ?? '—'}</TableCell>
                <TableCell>{displayMoney(item.claimedAmount)}</TableCell>
                <TableCell>{displayMoney(item.approvedAmount)}</TableCell>
                <TableCell>{displayDate(item.submittedAt)}</TableCell>
                <TableCell>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => {
                      setReview(item)
                      setApprovedAmount(item.approvedAmount?.toString() ?? '')
                      setError(false)
                    }}
                  >
                    Review
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </DataCard>
        </TabsContent>
        <TabsContent value="providers" className="flex flex-col gap-4">
          {role === 'ADMIN' && (
            <div className="flex justify-end">
              <Button
                onClick={() => {
                  setError(false)
                  setDialog('provider')
                }}
              >
                Add provider
              </Button>
            </div>
          )}
          <DataCard
            title="Insurance providers"
            description={String(providers.data?.length ?? 0) + ' providers'}
            headers={['Code', 'Name', 'Email', 'Phone', 'Status']}
            empty={providerRows.length === 0}
            pending={providers.isPending}
            error={providers.isError}
            retry={() => void providers.refetch()}
            page={providerPage}
            totalPages={Math.ceil((providers.data?.length ?? 0) / PAGE_SIZE)}
            previous={() => setProviderPage((value) => value - 1)}
            next={() => setProviderPage((value) => value + 1)}
          >
            {providerRows.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.code ?? '—'}</TableCell>
                <TableCell>{item.name ?? '—'}</TableCell>
                <TableCell>{item.contactEmail ?? '—'}</TableCell>
                <TableCell>{item.contactPhone ?? '—'}</TableCell>
                <TableCell>
                  <Badge variant="outline">
                    {item.isActive ? 'Active' : 'Inactive'}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </DataCard>
        </TabsContent>
        <TabsContent value="policies" className="flex flex-col gap-4">
          <div className="flex justify-end">
            <Button
              onClick={() => {
                setError(false)
                setDialog('policy')
              }}
            >
              Register policy
            </Button>
          </div>
          <DataCard
            title="Patient policies"
            description="Search by patient ID"
            headers={['Policy', 'Provider', 'Coverage', 'Status']}
            empty={policyRows.length === 0}
            pending={!!patientId && policies.isPending}
            error={policies.isError}
            retry={() => void policies.refetch()}
            page={policyPage}
            totalPages={Math.ceil((policies.data?.length ?? 0) / PAGE_SIZE)}
            previous={() => setPolicyPage((value) => value - 1)}
            next={() => setPolicyPage((value) => value + 1)}
            filters={
              <form
                className="flex gap-2"
                onSubmit={(event) => {
                  event.preventDefault()
                  setPatientId(patientInput.trim())
                  setPolicyPage(0)
                }}
              >
                <Input
                  aria-label="Patient ID"
                  placeholder="Patient UUID"
                  className="max-w-sm"
                  value={patientInput}
                  onChange={(event) => setPatientInput(event.target.value)}
                />
                <Button type="submit" variant="outline">
                  Search
                </Button>
              </form>
            }
          >
            {policyRows.map((item) => (
              <TableRow key={item.id}>
                <TableCell>{item.policyNumber ?? '—'}</TableCell>
                <TableCell>{item.insuranceProviderName ?? '—'}</TableCell>
                <TableCell>
                  {item.coverageRate === undefined
                    ? '—'
                    : Math.round(item.coverageRate * 100) + '%'}
                </TableCell>
                <TableCell>
                  <Badge variant="outline">
                    {item.isActive ? 'Active' : 'Inactive'}
                  </Badge>
                </TableCell>
              </TableRow>
            ))}
          </DataCard>
        </TabsContent>
      </Tabs>
      <Dialog
        open={!!dialog}
        onOpenChange={(open) => {
          if (!open) setDialog(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {dialog === 'provider'
                ? 'Add provider'
                : dialog === 'policy'
                  ? 'Register policy'
                  : 'Submit claim'}
            </DialogTitle>
            <DialogDescription>
              Enter the required insurance details.
            </DialogDescription>
          </DialogHeader>
          {dialog === 'provider' ? (
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="provider-name">Name</FieldLabel>
                <Input
                  id="provider-name"
                  value={providerName}
                  onChange={(event) => setProviderName(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="provider-code">Code</FieldLabel>
                <Input
                  id="provider-code"
                  value={providerCode}
                  onChange={(event) => setProviderCode(event.target.value)}
                />
              </Field>
            </FieldGroup>
          ) : dialog === 'policy' ? (
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="policy-number">Policy number</FieldLabel>
                <Input
                  id="policy-number"
                  value={policyNumber}
                  onChange={(event) => setPolicyNumber(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="policy-patient">Patient ID</FieldLabel>
                <Input
                  id="policy-patient"
                  value={policyPatientId}
                  onChange={(event) => setPolicyPatientId(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="policy-provider">Provider</FieldLabel>
                <NativeSelect
                  id="policy-provider"
                  value={providerId}
                  onChange={(event) => setProviderId(event.target.value)}
                >
                  <NativeSelectOption value="">
                    Select provider
                  </NativeSelectOption>
                  {providers.data
                    ?.filter((item) => item.id && item.isActive)
                    .map((item) => (
                      <NativeSelectOption key={item.id} value={item.id}>
                        {item.name}
                      </NativeSelectOption>
                    ))}
                </NativeSelect>
              </Field>
              <Field>
                <FieldLabel htmlFor="policy-coverage">Coverage (%)</FieldLabel>
                <Input
                  id="policy-coverage"
                  type="number"
                  min="0"
                  max="100"
                  value={coverage}
                  onChange={(event) => setCoverage(event.target.value)}
                />
              </Field>
            </FieldGroup>
          ) : (
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="claim-invoice">Invoice ID</FieldLabel>
                <Input
                  id="claim-invoice"
                  value={claimInvoiceId}
                  onChange={(event) => setClaimInvoiceId(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="claim-policy">Policy ID</FieldLabel>
                <Input
                  id="claim-policy"
                  value={claimPolicyId}
                  onChange={(event) => setClaimPolicyId(event.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="claim-amount">Claimed amount</FieldLabel>
                <Input
                  id="claim-amount"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={claimAmount}
                  onChange={(event) => setClaimAmount(event.target.value)}
                />
              </Field>
            </FieldGroup>
          )}
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog(null)}>
              Cancel
            </Button>
            <Button
              disabled={
                dialog === 'provider'
                  ? !providerName.trim() ||
                    !providerCode.trim() ||
                    createProvider.isPending
                  : dialog === 'policy'
                    ? !policyNumber.trim() ||
                      !policyPatientId.trim() ||
                      !providerId ||
                      !coverage ||
                      Number(coverage) < 0 ||
                      Number(coverage) > 100 ||
                      createPolicy.isPending
                    : !claimInvoiceId.trim() ||
                      !claimPolicyId.trim() ||
                      Number(claimAmount) <= 0 ||
                      submitClaim.isPending
              }
              onClick={() =>
                void (dialog === 'provider'
                  ? saveProvider()
                  : dialog === 'policy'
                    ? savePolicy()
                    : saveClaim())
              }
            >
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
      <Dialog
        open={!!review}
        onOpenChange={(open) => {
          if (!open) setReview(null)
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Review claim</DialogTitle>
            <DialogDescription>
              {review?.invoiceNumber} · {displayMoney(review?.claimedAmount)}{' '}
              claimed
            </DialogDescription>
          </DialogHeader>
          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="claim-decision">Decision</FieldLabel>
              <NativeSelect
                id="claim-decision"
                value={decision}
                onChange={(event) =>
                  setDecision(
                    event.target.value as UpdateClaimStatusRequestDTOStatus
                  )
                }
              >
                <NativeSelectOption value="APPROVED">
                  Approve
                </NativeSelectOption>
                <NativeSelectOption value="REJECTED">Reject</NativeSelectOption>
                <NativeSelectOption value="SETTLED">Settle</NativeSelectOption>
              </NativeSelect>
            </Field>
            {decision !== 'REJECTED' && (
              <Field>
                <FieldLabel htmlFor="approved-amount">
                  Approved amount (optional)
                </FieldLabel>
                <Input
                  id="approved-amount"
                  type="number"
                  min="0"
                  step="0.01"
                  value={approvedAmount}
                  onChange={(event) => setApprovedAmount(event.target.value)}
                />
              </Field>
            )}
          </FieldGroup>
          <FormError visible={error} />
          <DialogFooter>
            <Button variant="outline" onClick={() => setReview(null)}>
              Cancel
            </Button>
            <Button
              disabled={updateClaim.isPending}
              onClick={() => void saveDecision()}
            >
              Save decision
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
