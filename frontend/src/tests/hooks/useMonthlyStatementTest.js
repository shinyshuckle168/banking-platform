import React from 'react';
import { renderHook, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi } from 'vitest';

vi.mock('../../api/statementApi', () => ({
  getMonthlyStatementPdf: vi.fn(),
}));

import { getMonthlyStatementPdf } from '../../api/statementApi';
import { useMonthlyStatement } from '../../hooks/useMonthlyStatement';

const wrapper = ({ children }) =>
  React.createElement(QueryClientProvider,
    { client: new QueryClient({ defaultOptions: { queries: { retry: false } } }) },
    children
  );

describe('useMonthlyStatement', () => {
  it('calls getMonthlyStatementPdf with correct accountId and period', async () => {
    const fakeBlob = new Uint8Array([37, 80, 68, 70]);
    getMonthlyStatementPdf.mockResolvedValue({ data: fakeBlob });

    // Mock browser download APIs
    window.URL.createObjectURL = vi.fn(() => 'blob:test');
    window.URL.revokeObjectURL = vi.fn();

    const { result } = renderHook(() => useMonthlyStatement(1), { wrapper });
    await act(async () => {
      result.current.mutate('2026-01');
    });
    expect(getMonthlyStatementPdf).toHaveBeenCalledWith(1, '2026-01');
  });

  it('exposes error when API call fails', async () => {
    const err = { response: { status: 409, data: { code: 'ERR_FUTURE_MONTH', field: 'period' } } };
    getMonthlyStatementPdf.mockRejectedValue(err);

    const { result } = renderHook(() => useMonthlyStatement(1), { wrapper });
    await act(async () => {
      result.current.mutate('2099-12');
    });
    expect(result.current.isError).toBe(true);
  });
});

