import { useMutation } from '@tanstack/react-query';
import { getMonthlyStatementPdf } from '../api/statementApi';

export const useMonthlyStatement = (accountId) =>
  useMutation({
    mutationFn: (period) => getMonthlyStatementPdf(accountId, period),
    onSuccess: (response, period) => {
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `statement-${accountId}-${period}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    },
  });
