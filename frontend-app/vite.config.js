import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const loginApiTarget = env.VITE_LOGIN_API_PROXY_TARGET || 'http://localhost:8081';
  const accountServiceTarget = env.VITE_ACCOUNT_SERVICE_PROXY_TARGET || 'http://localhost:8080';

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/login-api': {
          target: loginApiTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/login-api/, '')
        },
        '/account-api': {
          target: accountServiceTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/account-api/, '')
        }
      }
    }
  };
});
