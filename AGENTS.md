# Hospital Management System — Agent Guidelines

These rules apply to the entire repository. More specific `AGENTS.md` files may add framework-specific instructions for their directory.

## General principles

- Keep implementations simple, readable, and consistent with existing project patterns.
- Inspect nearby code before introducing a new abstraction, component, hook, or dependency.
- Reuse existing utilities and conventions instead of creating parallel implementations.
- Keep changes focused on the requested feature and avoid unrelated refactors.
- Preserve existing user changes and do not overwrite generated or customized code unnecessarily.

## Frontend data fetching and API clients

- Use TanStack React Query for server-state fetching, caching, mutations, and invalidation.
- API functions and React Query hooks must be generated with Orval from the backend OpenAPI specification.
- Do not create handwritten `useQuery`, `useMutation`, query-key, Axios, `fetch`, or `customInstance` wrappers for backend endpoints when Orval can generate them.
- Do not manually edit files under `frontend/app/api/generated/`.
- When adding or changing a backend endpoint:
  1. Update the backend controller, DTOs, service, and OpenAPI metadata as needed.
  2. Make the updated OpenAPI document available through the local API gateway.
  3. Run Orval for the affected project, for example:
     `npx orval --config orval.config.ts --project receptionService`
  4. Import and use the generated hook in the frontend.
- Use generated query keys when invalidating or prefetching generated queries.
- Use React Query state such as `isPending`, `isError`, `refetch`, and mutation state instead of duplicating request state locally.
- Keep UI-only state such as filters, search input, page number, and dialog state in React component state.

## UI and shadcn

- Use existing shadcn components before creating custom UI primitives.
- Before adding a component, inspect `frontend/components/ui/` and the existing application components.
- Adding official shadcn components and blocks is encouraged when the project does not already contain what the feature needs.
- This includes tables, login forms, application forms, dialogs, sheets, dropdowns, selects, calendars, pagination, data-display components, and complete shadcn blocks.
- If the needed shadcn component or block is not installed, add it with the shadcn CLI using the frontend package runner.
- Prefer adding a maintained shadcn component or block over hand-building an equivalent component from raw HTML and Tailwind classes.
- Before adding a block, confirm the intended registry when it is not explicitly identified. Do not guess a third-party registry.
- Check shadcn documentation for the installed project style and component API before using or adding a component.
- Compose pages from existing components such as `Card`, `Table`, `Button`, `Input`, `Badge`, `Tabs`, `Dialog`, `Skeleton`, and `Field`.
- Follow the existing component composition and accessibility patterns. Dialogs, sheets, and drawers must have titles; avatars must have fallbacks; form controls must have labels.
- Use `Skeleton` for loading states, existing button variants for actions, and existing badge variants for statuses.
- Do not recreate functionality already provided by a shadcn component.

## Visual design

- Prefer a simple, calm, functional interface.
- Use the existing neutral theme and semantic color tokens.
- Do not introduce unusual, decorative, or hard-coded colors.
- Avoid gradients, excessive shadows, oversized decoration, and unnecessary visual effects.
- Use clear typography, restrained spacing, and a straightforward information hierarchy.
- Use color only when it communicates a meaningful state, and prefer existing semantic variants.
- Every data view should handle loading, error, empty, and populated states.
- Tables and lists should remain readable and include pagination when the API is paginated.

## Backend persistence and queries

- Prefer Spring Data derived query methods for small, obvious queries.
- Use Spring Data JPA `Specification` and `JpaSpecificationExecutor` for dynamic filters, multi-condition searches, joins, reusable predicates, and longer queries.
- Do not add long JPQL strings in `@Query` annotations.
- Short, focused JPQL queries are acceptable when they are clearer than a Specification or derived method.
- Keep Specifications in the service's `specification` package and give them intention-revealing names.
- Compose reusable predicates instead of duplicating filtering logic across repositories or services.
- Keep controllers thin. Business rules belong in services, and query construction belongs in Specifications or repositories.
- Enforce authorization and user scoping on the backend; frontend filtering is not an access-control boundary.
- Use pageable repository operations for potentially large result sets.

## Backend API design

- Return DTOs rather than exposing JPA entities.
- Use MapStruct for entity-to-DTO, DTO-to-entity, and DTO update mappings.
- Keep MapStruct mapper interfaces in the service's `mapper` package and use `componentModel = "spring"` so they can be injected.
- Prefer explicit `@Mapping` declarations for renamed, nested, ignored, or computed fields.
- Do not write manual field-by-field mapping code when MapStruct can generate the mapping.
- Add mapper tests when mappings contain nested relationships, computed values, update behavior, or ignored fields.
- Follow existing endpoint naming, response wrappers, validation, and role authorization patterns.
- Use the existing `PageResponse<T>` shape for paginated endpoints.
- Patients are clinical records, not application users. Do not add patient accounts, patient roles, patient self-service routes, or auth-user links to patient records.
- Scope doctor and employee endpoints to the authenticated user's domain where possible.
- Keep OpenAPI output accurate so Orval generates correct parameter and response types.

## Formatting and verification

- Run `./gradlew spotlessApply` in every modified Java service before completing backend work.
- Run relevant Gradle tests for every modified service.
- For frontend changes, run Prettier, TypeScript type checking, and ESLint on the affected files.
- Run broader builds or tests when the change affects shared infrastructure or generated clients.
- Run `git diff --check` before completion.
- Do not claim a check passed unless it was actually run successfully.

## Generated code

- Treat Orval output, protobuf output, and other generated files as generated artifacts.
- Change their source schema or generator configuration instead of hand-editing generated files.
- Regenerate only the affected API project when possible to avoid unrelated churn.
- Review generated hooks and types after regeneration to confirm the endpoint shape is correct.
