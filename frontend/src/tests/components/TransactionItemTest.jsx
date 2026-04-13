import { render, screen } from '@testing-library/react';
import TransactionItem from '../../components/transactions/TransactionItem';

const baseTx = {
  transactionId: 'tx-1',
  amount: 50.00,
  type: 'WITHDRAW',
  status: 'SUCCESS',
  timestamp: '2026-01-15T10:00:00',
  description: 'Coffee shop',
  idempotencyKey: null,
};

describe('TransactionItem', () => {
  it('renders all transaction fields', () => {
    render(<table><tbody><TransactionItem transaction={baseTx} /></tbody></table>);
    expect(screen.getByText('tx-1')).toBeInTheDocument();
    expect(screen.getByText('Coffee shop')).toBeInTheDocument();
    expect(screen.getByText('WITHDRAW')).toBeInTheDocument();
    expect(screen.getByText('Success')).toBeInTheDocument();
    // timestamp is formatted by Intl.DateTimeFormat — just verify it's present in the row
    expect(screen.getByText(/15 Jan 2026|Jan 15, 2026/i)).toBeInTheDocument();
  });

  it('shows idempotency key when present', () => {
    const tx = { ...baseTx, idempotencyKey: 'idem-abc' };
    render(<table><tbody><TransactionItem transaction={tx} /></tbody></table>);
    expect(screen.getByText('idem-abc')).toBeInTheDocument();
  });

  it('hides idempotency key when null', () => {
    render(<table><tbody><TransactionItem transaction={baseTx} /></tbody></table>);
    expect(screen.queryByText('idem-abc')).not.toBeInTheDocument();
  });

  it('renders PENDING status with amber badge class', () => {
    const tx = { ...baseTx, status: 'PENDING' };
    render(<table><tbody><TransactionItem transaction={tx} /></tbody></table>);
    const badge = screen.getByText('Pending');
    expect(badge).toHaveClass('badge-amber');
  });

  it('renders FAILED status with red badge class', () => {
    const tx = { ...baseTx, status: 'FAILED' };
    render(<table><tbody><TransactionItem transaction={tx} /></tbody></table>);
    const badge = screen.getByText('Failed');
    expect(badge).toHaveClass('badge-red');
  });
});
