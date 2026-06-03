import { defineConfig } from 'orval'

export default defineConfig({
  authService: {
    input: 'http://localhost:8000/api-docs/auth',
    output: {
      mode: 'tags-split',
      target: 'app/api/generated/auth',
      schemas: 'app/api/generated/auth/model',
      client: 'react-query',
      baseUrl: 'auth',
      override: {
        query: {
          useSuspenseQuery: true,
        },
        mutator: {
          path: './lib/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },

  hrService: {
    input: 'http://localhost:8000/api-docs/hr',
    output: {
      mode: 'tags-split',
      target: 'app/api/generated/hr',
      schemas: 'app/api/generated/hr/model',
      client: 'react-query',
      baseUrl: 'api/hr',
      override: {
        query: {
          useSuspenseQuery: true,
        },
        mutator: {
          path: './lib/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },

  billingService: {
    input: 'http://localhost:8000/api-docs/billing',
    output: {
      mode: 'tags-split',
      target: 'app/api/generated/billing',
      schemas: 'app/api/generated/billing/model',
      client: 'react-query',
      baseUrl: 'api/billing',
      override: {
        query: {
          useSuspenseQuery: true,
        },
        mutator: {
          path: './lib/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },

  receptionService: {
    input: 'http://localhost:8000/api-docs/reception',
    output: {
      mode: 'tags-split',
      target: 'app/api/generated/reception',
      schemas: 'app/api/generated/reception/model',
      client: 'react-query',
      baseUrl: '',
      override: {
        query: {
          useSuspenseQuery: true,
        },
        mutator: {
          path: './lib/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },

  notificationService: {
    input: 'http://localhost:8000/api-docs/notification',
    output: {
      mode: 'tags-split',
      target: 'app/api/generated/notification',
      schemas: 'app/api/generated/notification/model',
      client: 'react-query',
      baseUrl: '',
      override: {
        mutator: { path: './lib/axios-instance.ts', name: 'customInstance' },
      },
    },
  },

  analyticsService: {
    input: 'http://localhost:8000/api-docs/analytics',
    output: {
      mode: 'tags-split',
      target: 'app/api/generated/analytics',
      schemas: 'app/api/generated/analytics/model',
      client: 'react-query',
      baseUrl: '',
      override: {
        query: {
          useSuspenseQuery: true,
        },
        mutator: {
          path: './lib/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
})
