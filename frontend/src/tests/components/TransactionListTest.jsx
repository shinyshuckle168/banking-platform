import { render, screen } from '@testing-library/react';
import TransactionList from '../../components/transactions/TransactionList';

const makeTx = (id, status = 'SUCCESS') => ({
  transactionId: id,
  amount: 10,
  type: 'WITHDRAW',
  status,
  timestamp: '2026-01-01T10:00:00',
  description: `Desc ${id}`,
  idempotencyKey: null,
});

describe('TransactionList', () => {
  it('renders transactions in order received from props', () => {
    const txs = [makeTx('tx-1'), makeTx('tx-2'), makeTx('tx-3')];
    render(<TransactionList transactions={txs} />);
    const ids = screen.getAllByRole('row').slice(1).map((r) => r.cells[0].textContent);
    expect(ids).toEqual(['tx-1', 'tx-2', 'tx-3']);
  });

  it('renders PENDING status with amber badge', () => {
    render(<TransactionList transactions={[makeTx('tx-p', 'PENDING')]} />);
    expect(screen.getByText('Pending')).toHaveClass('badge-amber');
  });

  it('renders SUCCESS status with green badge', () => {
    render(<TransactionList transactions={[makeTx('tx-s', 'SUCCESS')]} />);
    expect(screen.getByText('Success')).toHaveClass('badge-green');
  });

  it('renders empty state message when array is empty', () => {
    render(<TransactionList transactions={[]} />);
    expect(screen.getByText(/no transactions found/i)).toBeInTheDocument();
  });

  it('renders correct transaction count', () => {
    const txs = [makeTx('a'), makeTx('b')];
    render(<TransactionList transactions={txs} />);
    // 2 data rows + 1 header row
    expect(screen.getAllByRole('row')).toHaveLength(3);
  });
});
