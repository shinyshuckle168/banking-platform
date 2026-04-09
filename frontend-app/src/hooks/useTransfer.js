import { useMutation } from '@tanstack/react-query';
import { transferFunds } from '../api/accounts';

export function useTransfer() {
  return useMutation({
    mutationFn: transferFunds,
    throwOnError: false
  });
}
