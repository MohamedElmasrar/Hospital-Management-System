import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  /* config options here */
  experimental: {
    agentFeedback: true,
  },
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      '*.css': {
        loaders: ['@tailwindcss/turbopack'],
        as: '*.css',
      },
    },
  },
  async redirects() {
    return ['reception', 'billing', 'hr', 'clinical', 'patient']
      .map((section) => ({
        source: `/dashboard/${section}/:path*`,
        destination: `/${section}/:path*`,
        permanent: true,
      }))
      .concat([
        {
          source: '/dashboard/profile',
          destination: '/profile',
          permanent: true,
        },
        {
          source: '/dashboard/users',
          destination: '/users',
          permanent: true,
        },
        {
          source: '/dashboard/audit-logs',
          destination: '/audit-logs',
          permanent: true,
        },
      ])
  },
}

export default nextConfig
