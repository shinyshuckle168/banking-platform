import { fileURLToPath } from 'node:url';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

const testSetupFile = fileURLToPath(new URL('./src/test/setup.js', import.meta.url));

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backendTarget =
    env.VITE_GROUP123_BACKEND_PROXY_TARGET ||
    env.VITE_BANKING_API_PROXY_TARGET ||
    env.VITE_ACCOUNT_SERVICE_PROXY_TARGET ||
    env.VITE_LOGIN_API_PROXY_TARGET ||
    'https://digital-banking-service-524103119199.northamerica-northeast1.run.app';

  return {
    plugins: [react()],
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: testSetupFile
    },
    preview: {
      allowedHosts: ['frontend-524103119199.northamerica-northeast2.run.app'],
      host: '0.0.0.0',
      port: 8080
    },   
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/accounts': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/customers': {
          target: backendTarget,
          changeOrigin: true,
        },
        '/standing-orders': {
          target: backendTarget,
          changeOrigin: true,
        }
      }
    }
  };
});
