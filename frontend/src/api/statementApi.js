import axiosInstance from './axiosInstance';

export const getMonthlyStatement = (accountId, period, version) => {
  const params = {};
  if (version !== undefined && version !== null) params.version = version;
  return axiosInstance.get(`/accounts/${accountId}/statements/${period}`, { params });
};
