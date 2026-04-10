import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';

vi.mock('../../api/standingOrderApi', () => ({
  listStandingOrders: vi.fn(),
  createStandingOrder: vi.fn(),
  cancelStandingOrder: vi.fn(),
}));

import { listStandingOrders, createStandingOrder, cancelStandingOrder } from '../../api/standingOrderApi';
import { useCreateStandingOrder, useCancelStandingOrder } from '../../hooks/useStandingOrders';

const makeWrapper = () => {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return ({ children }) => React.createElement(QueryClientProvider, { client: qc }, children);
};

describe('useCreateStandingOrder', () => {
  it('calls correct endpoint and invalidates on success', async () => {
    createStandingOrder.mockResolvedValue({ data: { standingOrderId: 'so-new' } });
    listStandingOrders.mockResolvedValue({ data: { standingOrders: [] } });

    const { result } = renderHook(() => useCreateStandingOrder(1), { wrapper: makeWrapper() });
    result.current.mutate({ payeeAccount: 'GB82WEST12345698765432', frequency: 'MONTHLY', amount: 100, startDate: '2026-12-01T10:00:00' });

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(createStandingOrder).toHaveBeenCalledWith(1, expect.any(Object));
  });
});

describe('useCancelStandingOrder', () => {
  it('calls correct DELETE endpoint and invalidates on success', async () => {
    cancelStandingOrder.mockResolvedValue({ data: {} });
    listStandingOrders.mockResolvedValue({ data: { standingOrders: [] } });

    const { result } = renderHook(() => useCancelStandingOrder(1), { wrapper: makeWrapper() });
    result.current.mutate('so-1');

    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(cancelStandingOrder).toHaveBeenCalledWith('so-1');
  });
});
