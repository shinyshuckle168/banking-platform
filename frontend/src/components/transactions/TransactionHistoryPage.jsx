import { useState } from 'react';
import LoadingSpinner from '../shared/LoadingSpinner';
import ErrorMessage from '../shared/ErrorMessage';
import TransactionList from './TransactionList';
import ExportButton from './ExportButton';
import { useTransactionHistory } from '../../hooks/useTransactionHistory';

/**
 * Transaction History page with date pickers and results display. (T039)
 */
const TransactionHistoryPage = ({ accountId }) => {
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const datesSelected = !!(startDate && endDate);

  const { data, isLoading, isError, error } = useTransactionHistory(
    accountId,
    datesSelected ? startDate : undefined,
    datesSelected ? endDate : undefined,
    { enabled: datesSelected }
  );

  const serverError = error?.response?.data;

  return (
    <div>
      <div className="page-header">
        <h1 className="page-title">Transaction History</h1>
        {datesSelected && data && (
          <ExportButton accountId={accountId} startDate={startDate} endDate={endDate} />
        )}
      </div>

      <div className="filter-row">
        <div className="filter-group">
          <label>Start Date</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="filter-group">
          <label>End Date</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
      </div>

      {!datesSelected && (
        <div className="empty-state">
          <div className="empty-state-icon">📅</div>
          <p className="hint">Choose a start date and end date above to load your transaction history.</p>
        </div>
      )}

      {datesSelected && isLoading && <LoadingSpinner />}

      {datesSelected && isError && (
        <ErrorMessage
          code={serverError?.code}
          message={serverError?.message || 'Failed to load transactions.'}
          field={serverError?.field}
        />
      )}

      {datesSelected && data && (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <TransactionList transactions={data.transactions || []} />
        </div>
      )}
    </div>
  );
};

export default TransactionHistoryPage;
