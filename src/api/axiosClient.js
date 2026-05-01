import axios from 'axios';
import { readStoredAuthState } from '../auth/authState';

const AUTH_STORAGE_KEY = 'banking-app-auth';
const CUSTOMER_CONTEXT_KEY = 'banking-app-customer-contexts';

function attachSessionExpiredInterceptor(client) {
  client.interceptors.response.use(
    (response) => response,
    (error) => {
      const status = error?.response?.status;
      // Only clear the session on 401 (Unauthorized = token missing/expired/invalid).
      // 403 (Forbidden) means the user IS authenticated but lacks permission — do not log them out.
      if (status === 401) {
        window.localStorage.removeItem(AUTH_STORAGE_KEY);
        window.localStorage.removeItem(CUSTOMER_CONTEXT_KEY);
        // Only redirect if not already on the login page to avoid redirect loops
        if (!window.location.pathname.startsWith('/login')) {
          window.location.replace('/login?error=session_expired');
        }
      }
      return Promise.reject(error);
    }
  );
  return client;
}

const mergedBackendBaseUrl =
  import.meta.env.VITE_GROUP123_BACKEND_BASE_URL ||
  import.meta.env.VITE_BANKING_API_BASE_URL ||
  import.meta.env.VITE_LOGIN_API_BASE_URL ||
  import.meta.env.VITE_ACCOUNT_SERVICE_BASE_URL ||
  (import.meta.env.DEV ? '/' : '/');

function attachAuthInterceptor(client) {
  client.interceptors.request.use((config) => {
    const authState = readStoredAuthState();
    const headers = config.headers ?? {};

    if (authState.accessToken) {
      headers.Authorization = `Bearer ${authState.accessToken}`;
    }

    config.headers = headers;
    return config;
  });

  return client;
}

export const loginApiClient = attachSessionExpiredInterceptor(attachAuthInterceptor(axios.create({
  baseURL: mergedBackendBaseUrl
})));

export const accountApiClient = attachSessionExpiredInterceptor(attachAuthInterceptor(axios.create({
  baseURL: mergedBackendBaseUrl
})));

function firstValidationError(errors) {
  if (!Array.isArray(errors) || errors.length === 0) {
    return null;
  }

  const [firstError] = errors;
  if (typeof firstError === 'string') {
    return firstError;
  }

  return firstError.defaultMessage || firstError.message || null;
}

export function mapAxiosError(error) {
  const response = error?.response;
  const data = response?.data;
  const validationMessage = firstValidationError(data?.errors);

  if (data?.code || data?.message) {
    return {
      code: data.code || `HTTP_${response?.status || 'UNKNOWN'}`,
      message: data.message || validationMessage || `Request failed with status ${response?.status}`,
      field: data.field ?? null
    };
  }

  if (validationMessage) {
    return {
      code: `HTTP_${response?.status || 422}`,
      message: validationMessage,
      field: null
    };
  }

  if (typeof data === 'string' && data.trim().length > 0) {
    return {
      code: `HTTP_${response?.status || 'UNKNOWN'}`,
      message: data,
      field: null
    };
  }

  return {
    code: 'UNKNOWN_ERROR',
    message: response?.status ? `Request failed with status ${response.status}` : 'Request failed',
    field: null
  };
}
