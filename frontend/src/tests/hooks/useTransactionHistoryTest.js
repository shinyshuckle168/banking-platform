import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';
import { useTransactionHistory } from '../../hooks/useTransactionHistory';

vi.mock('../../api/transactionApi', () => ({
  getTransactionHistory: vi.fn(),
}));

import { getTransactionHistory } from '../../api/transactionApi';

const wrapper = ({ children }) =>
  React.createElement(QueryClientProvider,
    { client: new QueryClient({ defaultOptions: { queries: { retry: false } } }) },
    children
  );

describe('useTransactionHistory', () => {
  it('calls getTransactionHistory with correct query key', async () => {
    getTransactionHistory.mockResolvedValue({ data: { transactions: [] } });

    const { result } = renderHook(() => useTransactionHistory(1, '2026-01-01', '2026-01-31'), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));

    expect(getTransactionHistory).toHaveBeenCalledWith(1, '2026-01-01', '2026-01-31');
  });

  it('returns mocked data on success', async () => {
    const mockData = { transactions: [{ transactionId: 'tx-1' }] };
    getTransactionHistory.mockResolvedValue({ data: mockData });

    const { result } = renderHook(() => useTransactionHistory(1), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(mockData);
  });

  it('exposes error on 401 response', async () => {
    const error = { response: { status: 401, data: { code: 'UNAUTHORIZED' } } };
    getTransactionHistory.mockRejectedValue(error);

    const { result } = renderHook(() => useTransactionHistory(1), { wrapper });
    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error).toBe(error);
  });
});
