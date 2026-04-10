import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

vi.mock('../../hooks/useMonthlyStatement', () => ({
  useMonthlyStatement: vi.fn(),
}));

import { useMonthlyStatement } from '../../hooks/useMonthlyStatement';
import MonthlyStatementPage from '../../components/statements/MonthlyStatementPage';

const wrapper = ({ children }) =>
  React.createElement(QueryClientProvider,
    { client: new QueryClient({ defaultOptions: { queries: { retry: false } } }) },
    children
  );

describe('MonthlyStatementPage', () => {
  it('renders month and year selectors and download button', () => {
    useMonthlyStatement.mockReturnValue({ mutate: vi.fn(), isPending: false, isError: false, isSuccess: false });
    render(<MonthlyStatementPage accountId={1} />, { wrapper });
    expect(screen.getByText(/Download PDF/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Month/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Year/i)).toBeInTheDocument();
  });

  it('shows loading state while generating', () => {
    useMonthlyStatement.mockReturnValue({ mutate: vi.fn(), isPending: true, isError: false, isSuccess: false });
    render(<MonthlyStatementPage accountId={1} />, { wrapper });
    expect(screen.getByText(/Generating/i)).toBeInTheDocument();
  });

  it('shows error message on failure', () => {
    const error = { response: { data: { code: 'ERR_FUTURE_MONTH', message: 'Future month requested', field: 'period' } } };
    useMonthlyStatement.mockReturnValue({ mutate: vi.fn(), isPending: false, isError: true, error, isSuccess: false });
    render(<MonthlyStatementPage accountId={1} />, { wrapper });
    expect(screen.getByText(/Future month requested/i)).toBeInTheDocument();
  });

  it('shows success message after download', () => {
    useMonthlyStatement.mockReturnValue({ mutate: vi.fn(), isPending: false, isError: false, isSuccess: true });
    render(<MonthlyStatementPage accountId={1} />, { wrapper });
    expect(screen.getByText(/downloaded successfully/i)).toBeInTheDocument();
  });

  it('calls mutate with correct period on button click', () => {
    const mutate = vi.fn();
    useMonthlyStatement.mockReturnValue({ mutate, isPending: false, isError: false, isSuccess: false });
    render(<MonthlyStatementPage accountId={1} />, { wrapper });
    fireEvent.click(screen.getByText(/Download PDF/i));
    expect(mutate).toHaveBeenCalledWith(expect.stringMatching(/^\d{4}-\d{2}$/));
  });
});

