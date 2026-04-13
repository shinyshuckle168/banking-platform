import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listStandingOrders, createStandingOrder, cancelStandingOrder } from '../api/standingOrderApi';

export const useStandingOrders = (accountId) =>
  useQuery({
    queryKey: ['standingOrders', accountId],
    queryFn: () => listStandingOrders(accountId).then((r) => r.data),
    enabled: !!accountId,
  });

export const useCreateStandingOrder = (accountId) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => createStandingOrder(accountId, payload).then((r) => r.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['standingOrders', accountId] }),
  });
};

export const useCancelStandingOrder = (accountId) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (standingOrderId) => cancelStandingOrder(standingOrderId).then((r) => r.data),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['standingOrders', accountId] }),
  });
};
