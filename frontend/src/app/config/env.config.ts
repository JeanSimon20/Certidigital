export const env = {
  apiUrl: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  env: import.meta.env.VITE_APP_ENV || 'development',
  appName: import.meta.env.VITE_APP_NAME || 'CertiDigital',
  isDev: import.meta.env.DEV,
} as const;
