import React from 'react';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';

vi.mock('../../api/statementApi', () => ({
  getMonthlyStatement: vi.fn(),
}));

import { getMonthlyStatement } from '../../api/statementApi';
import { useMonthlyStatement } from '../../hooks/useMonthlyStatement';

const wrapper = ({ children }) =>
  React.createElement(QueryClientProvider,
    { client: new QueryClient({ defaultOptions: { queries: { retry: false } } }) },
    children
  );

describe('useMonthlyStatement', () => {
  it('uses correct query key including version', async () => {
    getMonthlyStatement.mockResolvedValue({ data: { period: '2026-01', versionNumber: 2 } });

    const { result } = renderHook(() => useMonthlyStatement(1, '2026-01', 2), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(getMonthlyStatement).toHaveBeenCalledWith(1, '2026-01', 2);
  });

  it('returns data on success', async () => {
    const mockData = { period: '2026-01', versionNumber: 1, totalMoneyIn: 500 };
    getMonthlyStatement.mockResolvedValue({ data: mockData });

    const { result } = renderHook(() => useMonthlyStatement(1, '2026-01', undefined), { wrapper });
    await waitFor(() => expect(result.current.isSuccess).toBe(true));
    expect(result.current.data).toEqual(mockData);
  });

  it('exposes error on 410 response', async () => {
    const err = { response: { status: 410, data: { code: 'ERR_RETENTION_WINDOW' } } };
    getMonthlyStatement.mockRejectedValue(err);

    const { result } = renderHook(() => useMonthlyStatement(1, '2018-01', undefined), { wrapper });
    await waitFor(() => expect(result.current.isError).toBe(true));
    expect(result.current.error?.response?.status).toBe(410);
  });
});
