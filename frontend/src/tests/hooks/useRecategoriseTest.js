import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';

vi.mock('../../api/insightApi', () => ({
  getSpendingInsights: vi.fn(),
  recategoriseTransaction: vi.fn(),
}));

import { recategoriseTransaction, getSpendingInsights } from '../../api/insightApi';
import { useRecategorise } from '../../hooks/useSpendingInsights';

const makeWrapper = () => {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return ({ children }) => React.createElement(QueryClientProvider, { client: qc }, children);
};

describe('useRecategorise', () => {
  it('calls correct PUT endpoint with category', async () => {
    recategoriseTransaction.mockResolvedValue({ data: { updatedCategory: 'Transport' } });
    getSpendingInsights.mockResolvedValue({ data: {} });

    const { result } = renderHook(() => useRecategorise(1, 2026, 1), { wrapper: makeWrapper() });
    result.current.mutate({ transactionId: 'tx-1', category: 'Transport' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(recategoriseTransaction).toHaveBeenCalledWith(1, 'tx-1', 'Transport');
  });

  it('invalidates insight query on success', async () => {
    recategoriseTransaction.mockResolvedValue({ data: {} });
    getSpendingInsights.mockResolvedValue({ data: { categoryBreakdown: [] } });

    const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
    const wrapper = ({ children }) => React.createElement(QueryClientProvider, { client: qc }, children);

    // Pre-populate the cache
    qc.setQueryData(['insights', 1, 2026, 1], { categoryBreakdown: [] });

    const { result } = renderHook(() => useRecategorise(1, 2026, 1), { wrapper });
    result.current.mutate({ transactionId: 'tx-2', category: 'Shopping' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    // After success, the query should be invalidated (marked stale)
    const state = qc.getQueryState(['insights', 1, 2026, 1]);
    expect(state?.isInvalidated).toBe(true);
  });
});
