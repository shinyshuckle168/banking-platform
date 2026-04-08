import axios from 'axios';
import { readAuthState } from '../components/AuthPanel';

export const axiosClient = axios.create({
  baseURL: '/'
});

axiosClient.interceptors.request.use((config) => {
  const authState = readAuthState();
  const headers = config.headers ?? {};

  if (authState.token) {
    headers.Authorization = `Bearer ${authState.token}`;
  }
  if (authState.roles) {
    headers['X-Roles'] = authState.roles;
  }
  if (authState.customerId) {
    headers['X-Customer-Id'] = authState.customerId;
  }

  config.headers = headers;
  return config;
});

export function mapAxiosError(error) {
  const response = error?.response;
  if (response?.data?.message) {
    return response.data;
  }

  return {
    code: 'UNKNOWN_ERROR',
    message: response?.status ? `Request failed with status ${response.status}` : 'Request failed',
    field: null
  };
}
