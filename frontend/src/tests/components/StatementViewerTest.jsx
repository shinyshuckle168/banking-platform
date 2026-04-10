import { render, screen } from '@testing-library/react';
import StatementViewer from '../../components/statements/StatementViewer';

const baseStatement = {
  accountId: 1,
  period: '2026-01',
  openingBalance: 1000,
  closingBalance: 900,
  totalMoneyIn: 500,
  totalMoneyOut: 600,
  transactions: [
    { transactionId: 'tx-1', description: 'Rent', type: 'WITHDRAW', amount: 100, status: 'SUCCESS', timestamp: '2026-01-01T10:00:00' },
    { transactionId: 'tx-2', description: 'Salary', type: 'DEPOSIT', amount: 2000, status: 'SUCCESS', timestamp: '2026-01-05T08:00:00' },
  ],
  versionNumber: 1,
  correctionSummary: null,
  generatedAt: '2026-02-01T00:00:00',
};

describe('StatementViewer', () => {
  it('renders all fields', () => {
    render(<StatementViewer statement={baseStatement} />);
    expect(screen.getByText(/Statement.*2026-01/)).toBeInTheDocument();
    expect(screen.getByText(/1,000\.00|1000/)).toBeInTheDocument();
    expect(screen.getByText(/900\.00|900/)).toBeInTheDocument();
    expect(screen.getByText(/Version 1/i)).toBeInTheDocument();
  });

  it('renders correction summary when versionNumber > 1 and correctionSummary is set', () => {
    const stmt = { ...baseStatement, versionNumber: 2, correctionSummary: 'Duplicate entry removed' };
    render(<StatementViewer statement={stmt} />);
    expect(screen.getByText(/Duplicate entry removed/)).toBeInTheDocument();
  });

  it('hides correction summary on version 1', () => {
    render(<StatementViewer statement={baseStatement} />);
    expect(screen.queryByText(/Correction Summary/)).not.toBeInTheDocument();
  });

  it('transactions table renders correct row count', () => {
    render(<StatementViewer statement={baseStatement} />);
    // 2 data rows + 1 header row
    expect(screen.getAllByRole('row')).toHaveLength(3);
  });
});
