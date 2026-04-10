import axiosInstance from './axiosInstance';

export const getMonthlyStatementPdf = (accountId, period) =>
  axiosInstance.get(`/accounts/${accountId}/statements/${period}`, {
    responseType: 'blob',
  });
