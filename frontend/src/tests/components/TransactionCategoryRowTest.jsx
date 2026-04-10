import { render, screen, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';
import TransactionCategoryRow from '../../components/insights/TransactionCategoryRow';

vi.mock('../../hooks/useSpendingInsights', () => ({
  useRecategorise: vi.fn(),
}));

import { useRecategorise } from '../../hooks/useSpendingInsights';

const tx = { transactionId: 'tx-1', description: 'Starbucks', amount: 5.50, category: 'Food & Drink' };

describe('TransactionCategoryRow', () => {
  it('dropdown has exactly 8 options plus one uncategorised placeholder', () => {
    useRecategorise.mockReturnValue({ mutate: vi.fn(), isPending: false });

    render(<table><tbody><TransactionCategoryRow transaction={tx} accountId={1} year={2026} month={1} /></tbody></table>);
    const options = screen.getAllByRole('option');
    // 8 categories + 1 empty placeholder
    expect(options).toHaveLength(9);
  });

  it('selecting new category calls useRecategorise', () => {
    const mutate = vi.fn();
    useRecategorise.mockReturnValue({ mutate, isPending: false });

    render(<table><tbody><TransactionCategoryRow transaction={tx} accountId={1} year={2026} month={1} /></tbody></table>);
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'Transport' } });
    expect(mutate).toHaveBeenCalledWith({ transactionId: 'tx-1', category: 'Transport' });
  });

  it('shows loading state during mutation', () => {
    useRecategorise.mockReturnValue({ mutate: vi.fn(), isPending: true });

    render(<table><tbody><TransactionCategoryRow transaction={tx} accountId={1} year={2026} month={1} /></tbody></table>);
    expect(screen.getByText('Saving...')).toBeInTheDocument();
    expect(screen.getByRole('combobox')).toBeDisabled();
  });
});
