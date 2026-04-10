import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getSpendingInsights, recategoriseTransaction } from '../api/insightApi';

export const useSpendingInsights = (accountId, year, month) =>
  useQuery({
    queryKey: ['insights', accountId, year, month],
    queryFn: () => getSpendingInsights(accountId, year, month).then((r) => r.data),
    enabled: !!accountId && !!year && !!month,
  });

export const useRecategorise = (accountId, year, month) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ transactionId, category }) =>
      recategoriseTransaction(accountId, transactionId, category).then((r) => r.data),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ['insights', accountId, year, month] }),
  });
};
