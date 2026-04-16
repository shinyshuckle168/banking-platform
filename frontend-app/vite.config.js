import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backendTarget =
    env.VITE_GROUP123_BACKEND_PROXY_TARGET ||
    env.VITE_BANKING_API_PROXY_TARGET ||
    env.VITE_ACCOUNT_SERVICE_PROXY_TARGET ||
    env.VITE_LOGIN_API_PROXY_TARGET ||
    'http://localhost:8080';

  return {
    plugins: [react()],
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
