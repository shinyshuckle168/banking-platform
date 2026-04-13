import axiosInstance from './axiosInstance';

export const getSpendingInsights = (accountId, year, month) =>
  axiosInstance.get(`/accounts/${accountId}/insights`, { params: { year, month } });

export const recategoriseTransaction = (accountId, transactionId, category) =>
  axiosInstance.put(`/accounts/${accountId}/transactions/${transactionId}/category`, { category });
