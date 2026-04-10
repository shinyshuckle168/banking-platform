import axiosInstance from './axiosInstance';

export const getTransactionHistory = (accountId, startDate, endDate) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  return axiosInstance.get(`/accounts/${accountId}/transactions`, { params });
};

export const exportTransactionPdf = (accountId, startDate, endDate) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  return axiosInstance.get(`/accounts/${accountId}/transactions/export`, {
    params,
    responseType: 'blob',
  });
};
