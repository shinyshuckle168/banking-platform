import { useMutation } from '@tanstack/react-query';
import { exportTransactionPdf } from '../api/transactionApi';

export const useTransactionExport = (accountId, startDate, endDate) =>
  useMutation({
    mutationFn: () => exportTransactionPdf(accountId, startDate, endDate),
    onSuccess: (response) => {
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `transactions-${accountId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    },
  });
