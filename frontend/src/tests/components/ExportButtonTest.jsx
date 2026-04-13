import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import ExportButton from '../../components/transactions/ExportButton';

vi.mock('../../hooks/useTransactionExport', () => ({
  useTransactionExport: vi.fn(),
}));

import { useTransactionExport } from '../../hooks/useTransactionExport';

describe('ExportButton', () => {
  it('calls mutation on click', () => {
    const mutate = vi.fn();
    useTransactionExport.mockReturnValue({ mutate, isPending: false, isError: false });

    render(<ExportButton accountId={1} />);
    fireEvent.click(screen.getByRole('button', { name: /export pdf/i }));
    expect(mutate).toHaveBeenCalledTimes(1);
  });

  it('shows loading text during pending state', () => {
    useTransactionExport.mockReturnValue({ mutate: vi.fn(), isPending: true, isError: false });

    render(<ExportButton accountId={1} />);
    expect(screen.getByRole('button')).toHaveTextContent('Exporting...');
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('shows error message on failure', () => {
    useTransactionExport.mockReturnValue({
      mutate: vi.fn(),
      isPending: false,
      isError: true,
      error: { response: { data: { message: 'Export failed.' } } },
    });

    render(<ExportButton accountId={1} />);
    expect(screen.getByText('Export failed.')).toBeInTheDocument();
  });
});
