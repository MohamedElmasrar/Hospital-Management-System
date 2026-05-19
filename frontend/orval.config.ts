import { defineConfig } from 'orval';

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
      baseUrl: 'hr',
      override: {
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
      baseUrl: 'billing',
      override: {
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
      baseUrl: 'reception',
      override: {
        mutator: {
          path: './lib/axios-instance.ts',
          name: 'customInstance',
        },
      },
    },
  },
});