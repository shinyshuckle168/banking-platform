import { useQuery } from '@tanstack/react-query';
import { getTransactionHistory } from '../api/transactionApi';

export const useTransactionHistory = (accountId, startDate, endDate, options = {}) =>
  useQuery({
    queryKey: ['transactions', accountId, startDate, endDate],
    queryFn: () => getTransactionHistory(accountId, startDate, endDate).then((r) => r.data),
    enabled: !!accountId && (options.enabled !== false),
  });
