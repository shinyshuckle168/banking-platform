import axiosInstance from './axiosInstance';

export const login = (username, password) =>
  axiosInstance.post('/api/auth/login', { username, password });

export const register = (username, password) =>
  axiosInstance.post('/api/auth/register', { username, password });
