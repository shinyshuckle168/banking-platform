import { useQuery } from '@tanstack/react-query';
import { getMonthlyStatement } from '../api/statementApi';

export const useMonthlyStatement = (accountId, period, version) =>
  useQuery({
    queryKey: ['statement', accountId, period, version],
    queryFn: () => getMonthlyStatement(accountId, period, version).then((r) => r.data),
    enabled: !!accountId && !!period,
  });
